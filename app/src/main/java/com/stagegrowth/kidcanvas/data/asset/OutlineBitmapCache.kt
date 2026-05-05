package com.stagegrowth.kidcanvas.data.asset

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.stagegrowth.kidcanvas.domain.util.OUTLINE_THRESHOLD
import com.stagegrowth.kidcanvas.domain.util.erodeMask
import com.stagegrowth.kidcanvas.domain.util.floodFillRegion
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * bbox 계산용 알파 임계값. 외곽선 본체(검은 선)만 잡고 안티앨리어싱 회색은 제외해서
 * 캐릭터 진짜 윤곽 박스를 얻는다. flood fill 의 [OUTLINE_THRESHOLD] 보다 엄격(100).
 */
private const val BBOX_OUTLINE_THRESHOLD: Int = 100

/**
 * bbox 여유 픽셀. 외곽선 살짝 안쪽까지 영역 인식을 인정해 자녀가 외곽선 바로 옆을 터치해도
 * 자유 드로잉으로 폴백되지 않도록 한다.
 */
private const val BBOX_MARGIN: Int = 20

/**
 * 외곽선 PNG 의 알파 채널을 한 번만 디코딩해 보관하고, seed → 영역 마스크 결과를 메모이제이션.
 *
 * Spring 비유: @Service + ConcurrentHashMap 캐시. 같은 영역 안의 다른 seed 가 들어와도
 * 이미 계산된 마스크가 그 픽셀을 포함하면 바로 그 마스크를 재사용.
 *
 * 메모리 정책: 새로운 캐릭터(다른 assetPath) 가 들어오면 이전 캐릭터 캐시는 비워서
 * 21 캐릭터 모두 캐시된 채 OOM 나는 상황을 방지. 한 번에 한 캐릭터분만 메모리에.
 */
@Singleton
class OutlineBitmapCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * 외곽선 비트맵의 알파 배열 + 캐릭터 외곽 박스(방어 + 진단용).
     * bbox 는 alpha ≥ [BBOX_OUTLINE_THRESHOLD] 인 픽셀들의 min/max + [BBOX_MARGIN] 여유.
     * 캐릭터가 없는 빈 영역이면 left/top 이 right/bottom 보다 큰 채로 남아 박스 검사가 항상 false.
     */
    private data class OutlineData(
        val width: Int,
        val height: Int,
        val alpha: IntArray,
        val bboxLeft: Int,
        val bboxTop: Int,
        val bboxRight: Int,
        val bboxBottom: Int,
    )

    /**
     * 한 영역. mask=null 이면 외곽선 위에서 시작한 경우(=영역 클리핑 없음, 자유 드로잉).
     * bitmap 은 BlendMode.DstIn 합성에 쓸 ImageBitmap (마스크 ≠ null 일 때만 생성).
     */
    data class CachedRegion(
        val mask: BooleanArray?,
        val width: Int,
        val height: Int,
        val bitmap: ImageBitmap?,
        val maskedPixelCount: Int,
    ) {
        fun contains(px: Int, py: Int): Boolean {
            val m = mask ?: return false
            if (px !in 0 until width || py !in 0 until height) return false
            return m[py * width + px]
        }
    }

    /** Flood Fill 진단 정보 (마스크 폭주 버그 추적용). */
    data class FloodFillDiagnostic(
        val bitmapX: Int,
        val bitmapY: Int,
        val seedAlpha: Int,
        val outlineThreshold: Int,
        val maskedPixelCount: Int,
        val totalPixels: Int,
        val bboxLeft: Int,
        val bboxTop: Int,
        val bboxRight: Int,
        val bboxBottom: Int,
    )

    data class RegionResult(
        val region: CachedRegion,
        val diagnostic: FloodFillDiagnostic,
    )

    private val mutex = Mutex()
    private var currentAssetPath: String? = null
    private var currentOutline: OutlineData? = null
    private val regions: MutableList<CachedRegion> = mutableListOf()

    /**
     * 정규화 좌표(0~1) seed → 그 픽셀이 속한 영역의 CachedRegion + 진단 정보.
     * 다음 세 경우엔 region.mask/bitmap 모두 null (자유 드로잉 폴백):
     *   - 캐릭터 외곽 박스 밖 터치 (BFS 자체를 생략, 가장 큰 누수 원인 차단)
     *   - 외곽선 위에서 시작 ([floodFillRegion] 이 null 반환)
     *   - BFS 결과 마스크가 전체의 50% 초과 ([floodFillRegion] 이 null 반환)
     *
     * 호출 흐름:
     *   1) assetPath 가 바뀌었으면 이전 캐릭터 캐시 비움 (메모리 절약)
     *   2) 외곽선 PNG 알파 배열 + bbox 로드 (캐시)
     *   3) 박스 밖 터치면 즉시 null 마스크 반환
     *   4) 같은 영역에 이미 계산된 마스크가 있으면 그걸 반환
     *   5) 없으면 BFS Flood Fill + 1 px 침식, 결과를 캐시에 추가
     */
    suspend fun regionFor(
        assetPath: String,
        seedNormX: Float,
        seedNormY: Float,
    ): RegionResult {
        val outline = loadOutlineMaybeSwitchCharacter(assetPath)
        val sx = (seedNormX * outline.width).toInt().coerceIn(0, outline.width - 1)
        val sy = (seedNormY * outline.height).toInt().coerceIn(0, outline.height - 1)
        val seedAlpha = outline.alpha[sy * outline.width + sx]
        val totalPixels = outline.width * outline.height

        // 방어: 캐릭터 외곽 박스 밖을 터치한 경우엔 BFS 자체를 생략하고 자유 드로잉 폴백.
        // 외부 흰 공간 시작 → 거대한 마스크가 만들어지는 가장 큰 원인을 차단.
        val isInsideBbox = sx in outline.bboxLeft..outline.bboxRight &&
            sy in outline.bboxTop..outline.bboxBottom
        if (!isInsideBbox) {
            return RegionResult(
                region = CachedRegion(
                    mask = null,
                    width = outline.width,
                    height = outline.height,
                    bitmap = null,
                    maskedPixelCount = 0,
                ),
                diagnostic = buildDiagnostic(sx, sy, seedAlpha, 0, totalPixels, outline),
            )
        }

        // 캐시 검색 — 이 seed 가 이미 계산된 어느 마스크에 포함되나
        mutex.withLock {
            regions.firstOrNull { it.contains(sx, sy) }?.let { cached ->
                return RegionResult(
                    region = cached,
                    diagnostic = buildDiagnostic(sx, sy, seedAlpha, cached.maskedPixelCount, totalPixels, outline),
                )
            }
        }

        // 새로 계산 (락 없이 무거운 작업)
        val computed = withContext(Dispatchers.Default) {
            val raw = floodFillRegion(outline.alpha, outline.width, outline.height, sx, sy, OUTLINE_THRESHOLD)
            val eroded = raw?.let { erodeMask(it, outline.width, outline.height, iterations = 1) }
            val count = eroded?.count { it } ?: 0
            CachedRegion(
                mask = eroded,
                width = outline.width,
                height = outline.height,
                bitmap = eroded?.let { maskToImageBitmap(it, outline.width, outline.height) },
                maskedPixelCount = count,
            )
        }

        mutex.withLock {
            // 같은 캐릭터 컨텍스트인 경우만 추가 (도중 다른 캐릭터로 전환된 경우 무시)
            if (currentAssetPath == assetPath) {
                regions.add(computed)
            }
        }
        return RegionResult(
            region = computed,
            diagnostic = buildDiagnostic(sx, sy, seedAlpha, computed.maskedPixelCount, totalPixels, outline),
        )
    }

    private fun buildDiagnostic(
        sx: Int,
        sy: Int,
        seedAlpha: Int,
        maskedPixelCount: Int,
        totalPixels: Int,
        outline: OutlineData,
    ): FloodFillDiagnostic = FloodFillDiagnostic(
        bitmapX = sx,
        bitmapY = sy,
        seedAlpha = seedAlpha,
        outlineThreshold = OUTLINE_THRESHOLD,
        maskedPixelCount = maskedPixelCount,
        totalPixels = totalPixels,
        bboxLeft = outline.bboxLeft,
        bboxTop = outline.bboxTop,
        bboxRight = outline.bboxRight,
        bboxBottom = outline.bboxBottom,
    )

    private suspend fun loadOutlineMaybeSwitchCharacter(assetPath: String): OutlineData {
        mutex.withLock {
            if (currentAssetPath == assetPath && currentOutline != null) {
                return currentOutline!!
            }
        }
        // 캐릭터가 바뀌었거나 아직 로드 안 됨 → 새 비트맵 디코딩
        val data = withContext(Dispatchers.IO) {
            context.assets.open(assetPath).use { stream ->
                val bitmap = BitmapFactory.decodeStream(stream)
                val w = bitmap.width
                val h = bitmap.height
                val pixels = IntArray(w * h)
                bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
                bitmap.recycle()
                // 알파 채널만 (검은 선 = 알파 255, 흰 영역 = 알파 0).
                // ARGB 의 상위 8 비트 추출.
                val alpha = IntArray(w * h) { i -> (pixels[i] ushr 24) and 0xFF }
                // 캐릭터 외곽 박스 — 외곽선 본체(α ≥ 100)만으로 계산해 안티앨리어싱 영향 제거.
                // 진단 로그용 + 박스 밖 터치 방어용. 여유 [BBOX_MARGIN] px 더해 외곽선 옆 터치 허용.
                var bl = w; var bt = h; var br = -1; var bb = -1
                for (y in 0 until h) {
                    val rowOff = y * w
                    for (x in 0 until w) {
                        if (alpha[rowOff + x] >= BBOX_OUTLINE_THRESHOLD) {
                            if (x < bl) bl = x
                            if (x > br) br = x
                            if (y < bt) bt = y
                            if (y > bb) bb = y
                        }
                    }
                }
                val bboxLeft = (bl - BBOX_MARGIN).coerceAtLeast(0)
                val bboxTop = (bt - BBOX_MARGIN).coerceAtLeast(0)
                val bboxRight = (br + BBOX_MARGIN).coerceAtMost(w - 1)
                val bboxBottom = (bb + BBOX_MARGIN).coerceAtMost(h - 1)
                OutlineData(w, h, alpha, bboxLeft, bboxTop, bboxRight, bboxBottom)
            }
        }
        mutex.withLock {
            // 다른 캐릭터로의 전환이면 이전 마스크 캐시 비움
            if (currentAssetPath != assetPath) {
                regions.clear()
                currentAssetPath = assetPath
            }
            currentOutline = data
        }
        return data
    }

    private fun maskToImageBitmap(mask: BooleanArray, width: Int, height: Int): ImageBitmap {
        // 마스크 true → 흰색 불투명, false → 완전 투명.
        // BlendMode.DstIn 으로 합성 시 destination(stroke) 픽셀이 mask alpha > 0 인 곳에서만 보존.
        val pixels = IntArray(width * height) { i ->
            if (mask[i]) 0xFFFFFFFF.toInt() else 0
        }
        val bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bm.setPixels(pixels, 0, width, 0, 0, width, height)
        return bm.asImageBitmap()
    }
}
