package com.stagegrowth.kidcanvas.data.asset

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
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

    /** 외곽선 비트맵의 알파 배열만 추출해 보관. 색 정보는 영역 계산에 불필요. */
    private data class OutlineData(
        val width: Int,
        val height: Int,
        val alpha: IntArray,
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
    ) {
        fun contains(px: Int, py: Int): Boolean {
            val m = mask ?: return false
            if (px !in 0 until width || py !in 0 until height) return false
            return m[py * width + px]
        }
    }

    private val mutex = Mutex()
    private var currentAssetPath: String? = null
    private var currentOutline: OutlineData? = null
    private val regions: MutableList<CachedRegion> = mutableListOf()

    /**
     * 정규화 좌표(0~1) seed → 그 픽셀이 속한 영역의 CachedRegion.
     * 외곽선 위에서 시작한 경우엔 mask=null 반환 (자유 드로잉).
     *
     * 호출 흐름:
     *   1) assetPath 가 바뀌었으면 이전 캐릭터 캐시 비움 (메모리 절약)
     *   2) 외곽선 PNG 알파 배열 로드 (캐시)
     *   3) 같은 영역에 이미 계산된 마스크가 있으면 그걸 반환
     *   4) 없으면 BFS Flood Fill + 1 px 침식, 결과를 캐시에 추가
     */
    suspend fun regionFor(
        assetPath: String,
        seedNormX: Float,
        seedNormY: Float,
    ): CachedRegion {
        val outline = loadOutlineMaybeSwitchCharacter(assetPath)
        val sx = (seedNormX * outline.width).toInt().coerceIn(0, outline.width - 1)
        val sy = (seedNormY * outline.height).toInt().coerceIn(0, outline.height - 1)

        // 캐시 검색 — 이 seed 가 이미 계산된 어느 마스크에 포함되나
        mutex.withLock {
            regions.firstOrNull { it.contains(sx, sy) }?.let { return@regionFor it }
        }

        // 새로 계산 (락 없이 무거운 작업)
        val computed = withContext(Dispatchers.Default) {
            val raw = floodFillRegion(outline.alpha, outline.width, outline.height, sx, sy)
            val eroded = raw?.let { erodeMask(it, outline.width, outline.height, iterations = 1) }
            CachedRegion(
                mask = eroded,
                width = outline.width,
                height = outline.height,
                bitmap = eroded?.let { maskToImageBitmap(it, outline.width, outline.height) },
            )
        }

        mutex.withLock {
            // 같은 캐릭터 컨텍스트인 경우만 추가 (도중 다른 캐릭터로 전환된 경우 무시)
            if (currentAssetPath == assetPath) {
                regions.add(computed)
            }
        }
        return computed
    }

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
                OutlineData(w, h, alpha)
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
