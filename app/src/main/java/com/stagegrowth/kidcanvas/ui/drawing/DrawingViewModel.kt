package com.stagegrowth.kidcanvas.ui.drawing

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stagegrowth.kidcanvas.data.asset.OutlineBitmapCache
import com.stagegrowth.kidcanvas.data.repository.ColoringRepository
import com.stagegrowth.kidcanvas.domain.model.DrawingState
import com.stagegrowth.kidcanvas.domain.model.NormalizedPoint
import com.stagegrowth.kidcanvas.domain.model.Stroke
import com.stagegrowth.kidcanvas.domain.model.Tool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 드로잉 화면 상태 보유 + 사용자 입력 처리 + 자동 저장/복원 + 영역 인식 자유 드로잉.
 *
 * Spring 비유:
 *   - @Service. StateFlow 가 응답 모델, on*() 가 핸들러.
 *   - viewModelScope.launch 는 ViewModel 라이프사이클에 묶인 @Async 컨텍스트 (화면 종료 시 자동 취소).
 *   - SavedStateHandle 의 KEY_TARGET_ID 는 NavGraph 의 route "drawing/{targetId}" 에 의해 자동 채워짐.
 *
 * 영역 인식 자유 드로잉 (M3.5):
 *   - 첫 터치(BRUSH) 위치를 seed 로 보관, 비동기로 외곽선 BFS Flood Fill → 영역 마스크 생성.
 *   - 드래그 중에는 currentStroke 점들이 그대로 들어가지만, 렌더 시 마스크로 클리핑되어
 *     색이 영역 밖으로 새지 않음.
 *   - ERASER, 외곽선 위 시작은 마스크 없이 자유 드로잉 유지.
 */
@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val repository: ColoringRepository,
    private val outlineCache: OutlineBitmapCache,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val targetId: String = savedStateHandle.get<String>(KEY_TARGET_ID).orEmpty()

    private val _uiState = MutableStateFlow(DrawingUiState(targetId = targetId))
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    init {
        if (targetId.isNotBlank()) {
            viewModelScope.launch {
                // 1) 메타데이터(이름, 외곽선 path) 해석
                val target = repository.getCategories().first()
                    .flatMap { it.targets }
                    .firstOrNull { it.id == targetId }
                if (target != null) {
                    _uiState.update {
                        it.copy(targetName = target.name, outlinePath = target.outline)
                    }
                }
                // 2) 저장된 strokes 복원
                val saved = repository.getDrawingState(targetId).first()
                if (saved != null && saved.strokes.isNotEmpty()) {
                    _uiState.update { it.copy(strokes = saved.strokes) }
                    // 3) 복원된 stroke 들이 쓴 unique seed 들의 마스크를 백그라운드 워밍.
                    //    워밍 끝나기 전엔 잠시 자유 드로잉처럼 보이지만, 보통 200~600ms 안에 보정됨.
                    val outlinePath = target?.outline
                    if (outlinePath != null) {
                        warmMasks(outlinePath, saved.strokes.mapNotNull { it.seed }.distinct())
                    }
                }
            }
        }
    }

    fun onDragStart(offset: Offset, canvasSize: IntSize) {
        val point = offset.toNormalized(canvasSize)
        val state = _uiState.value
        val outlinePath = state.outlinePath

        // 지우개는 영역 인식 안 함 (기존 동작 유지). 외곽선 path 모르면 자유 드로잉.
        val needRegion = state.currentTool == Tool.BRUSH && outlinePath != null

        _uiState.update { st ->
            st.copy(
                currentStroke = Stroke(
                    color = st.currentColor,
                    widthDp = st.currentWidthDp,
                    tool = st.currentTool,
                    points = listOf(point),
                    seed = if (needRegion) point else null,
                ),
                activeMask = null, // 영역 마스크 계산 전엔 자유 드로잉처럼 보이다가 도착 시 클리핑됨
            )
        }

        if (needRegion && outlinePath != null) {
            val touchX = offset.x
            val touchY = offset.y
            val canvasW = canvasSize.width
            val canvasH = canvasSize.height
            viewModelScope.launch {
                val result = outlineCache.regionFor(outlinePath, point.x, point.y)
                val bitmap = result.region.bitmap
                val diag = result.diagnostic
                val regionW = result.region.width
                val regionH = result.region.height

                // ── 기존 FloodFillDebug 로그 유지 ─────────────────────────────────
                val totalPixels = diag.totalPixels.coerceAtLeast(1)
                val maskRatioPct = diag.maskedPixelCount * 100 / totalPixels
                val isInsideBbox = diag.bitmapX in diag.bboxLeft..diag.bboxRight &&
                    diag.bitmapY in diag.bboxTop..diag.bboxBottom
                Log.d(
                    "FloodFillDebug",
                    """
                        === Flood Fill Result ===
                        캐릭터 ID: $targetId
                        터치 좌표 (Compose px): ($touchX, $touchY)
                        비트맵 좌표 (px): (${diag.bitmapX}, ${diag.bitmapY})
                        시작 픽셀 알파: ${diag.seedAlpha}
                        임계값: ${diag.outlineThreshold}
                        마스크 픽셀 수: ${diag.maskedPixelCount} / ${diag.totalPixels}
                        마스크 비율: ${maskRatioPct}%
                        캐릭터 외곽 박스: x=${diag.bboxLeft}~${diag.bboxRight}, y=${diag.bboxTop}~${diag.bboxBottom}
                        터치가 외곽 박스 안: $isInsideBbox
                        =========================
                    """.trimIndent(),
                )
                if (bitmap == null) {
                    Log.d("FloodFillDebug", "마스크 생성 실패 — 자유 드로잉 모드로 폴백")
                }

                // ── DrawDebug: 첫 터치 단계 ─────────────────────────────────
                // 좌표 변환·bbox 캐싱 검증.
                val bboxValid = diag.bboxLeft <= diag.bboxRight && diag.bboxTop <= diag.bboxBottom
                val passedBboxCheck = bboxValid && isInsideBbox
                Log.d(
                    "DrawDebug",
                    """
                        === 첫 터치 ===
                        캐릭터 ID: $targetId
                        Compose 터치 좌표: ($touchX, $touchY)
                        캔버스 크기: $canvasW x $canvasH
                        정규화 좌표: (${point.x}, ${point.y})
                        비트맵 좌표: (${diag.bitmapX}, ${diag.bitmapY})
                        비트맵 크기: $regionW x $regionH
                        시드 픽셀 알파: ${diag.seedAlpha}
                        임계값(외곽선 판정): ${diag.outlineThreshold}
                        bbox 캐싱됨?: $bboxValid
                        bbox 값: x=${diag.bboxLeft}~${diag.bboxRight}, y=${diag.bboxTop}~${diag.bboxBottom}
                        bbox 안인가?: $isInsideBbox
                        bbox 검증 통과?: $passedBboxCheck
                    """.trimIndent(),
                )

                // ── DrawDebug: 마스크 결과 단계 ─────────────────────────────────
                // 35% 검증·최종 activeMask 결정 사유.
                val ratio = if (diag.totalPixels == 0) 0f else
                    diag.maskedPixelCount.toFloat() / diag.totalPixels
                val passed35 = bitmap != null  // null=폴백, 정상 마스크는 35% 이하만 통과
                val decision = when {
                    !passedBboxCheck -> "bbox 외부 → 폴백 (자유 드로잉)"
                    diag.seedAlpha >= diag.outlineThreshold -> "외곽선 위 시작 → 폴백 (자유 드로잉)"
                    bitmap == null -> "마스크 35% 초과 → 폴백 (자유 드로잉)"
                    else -> "정상 마스크 생성"
                }
                Log.d(
                    "DrawDebug",
                    """
                        === 마스크 결과 ===
                        마스크 픽셀 수: ${diag.maskedPixelCount} / ${diag.totalPixels}
                        마스크 비율: ${(ratio * 100f).toInt()}%
                        35% 검증 통과?: $passed35
                        최종 activeMask null?: ${bitmap == null}
                        결정 사유: $decision
                    """.trimIndent(),
                )

                _uiState.update { st ->
                    val cur = st.currentStroke

                    // 케이스 1: 진행 중 stroke 의 seed 와 일치 — 정상 흐름.
                    //          activeMask + maskBySeed 동시 등록.
                    if (cur != null && cur.seed == point) {
                        if (bitmap == null) {
                            // 폴백 (bbox 외부 / 외곽선 위 / 35% 초과) — 자유 드로잉 유지
                            st
                        } else {
                            st.copy(
                                activeMask = bitmap,
                                // 종료 후 즉시 활용되도록 maskBySeed 도 미리 채움
                                maskBySeed = st.maskBySeed + (point to bitmap),
                            )
                        }
                    }
                    // 케이스 2: race — 마스크 계산이 onDragEnd 보다 늦음.
                    //          이미 strokes 로 옮겨진 stroke 중 같은 seed 인 게 있으면 사후 등록.
                    //          maskBySeed 가 StateFlow 의 일부라 변경 시 Compose 가 자동 재합성 →
                    //          DrawingCanvas 의 stroke.seed → maskBySeed[seed] lookup 이 갱신돼
                    //          다음 프레임에 마스크 클리핑이 적용됨 (외곽선 침범이 사라짐).
                    else {
                        val matched = st.strokes.lastOrNull { it.seed == point }
                        if (matched != null && bitmap != null) {
                            Log.d("DrawDebug", "race 복구: 종료된 stroke 에 마스크 사후 등록 (seed=$point)")
                            st.copy(maskBySeed = st.maskBySeed + (point to bitmap))
                        } else {
                            // 매칭 stroke 없음: undo/reset 으로 사라졌거나 무관한 호출
                            Log.d("DrawDebug", "race: 매칭 stroke 없음 → 마스크 폐기")
                            st
                        }
                    }
                }
            }
        }
    }

    fun onDrag(offset: Offset, canvasSize: IntSize) {
        val point = offset.toNormalized(canvasSize)
        _uiState.update { state ->
            val cur = state.currentStroke ?: return@update state
            state.copy(currentStroke = cur.copy(points = cur.points + point))
        }
    }

    fun onDragEnd() {
        _uiState.update { state ->
            val cur = state.currentStroke ?: return@update state
            // 진단: 종료 시점에 stroke.seed 가 maskBySeed 에 등록됐는지 확인.
            // race 케이스(마스크 도착 전 종료) 면 자유 드로잉으로 그려져 외곽선을 침범할 수 있음.
            val seed = cur.seed
            val hasMask = seed != null && state.maskBySeed.containsKey(seed)
            Log.d(
                "DrawDebug",
                """
                    === stroke 종료 ===
                    stroke seed: $seed
                    maskBySeed 에 등록됨?: $hasMask
                    stroke 점 개수: ${cur.points.size}
                    도구: ${cur.tool}
                """.trimIndent(),
            )
            state.copy(
                strokes = state.strokes + cur,
                currentStroke = null,
                activeMask = null,
            )
        }
        persistCurrent()
    }

    /** 마지막 stroke 한 개 제거. 비어 있으면 무시. */
    fun undo() {
        var changed = false
        _uiState.update { state ->
            if (state.strokes.isEmpty()) {
                state
            } else {
                changed = true
                state.copy(strokes = state.strokes.dropLast(1))
            }
        }
        if (changed) persistCurrent()
    }

    /** 전체 초기화. 다이얼로그 확인 후에만 호출되어야 함. DB 의 저장 행도 제거. */
    fun reset() {
        _uiState.update {
            it.copy(
                strokes = emptyList(),
                currentStroke = null,
                activeMask = null,
                // 마스크 캐시는 유지 — 같은 캐릭터에 다시 그릴 때 즉시 재사용.
            )
        }
        viewModelScope.launch {
            repository.resetDrawing(targetId)
        }
    }

    fun changeColor(color: Long) {
        _uiState.update { it.copy(currentColor = color) }
    }

    fun changeWidthDp(dp: Float) {
        _uiState.update { it.copy(currentWidthDp = dp) }
    }

    fun changeTool(tool: Tool) {
        _uiState.update { it.copy(currentTool = tool) }
    }

    /** 현재 strokes 를 DB 에 저장. 진행 중인 currentStroke 는 저장 대상 아님. */
    private fun persistCurrent() {
        if (targetId.isBlank()) return
        val snapshot = _uiState.value
        viewModelScope.launch {
            repository.saveDrawingState(
                DrawingState(
                    targetId = snapshot.targetId,
                    strokes = snapshot.strokes,
                    updatedAt = System.currentTimeMillis(),
                )
            )
        }
    }

    /** 복원된 stroke 들의 unique seed 마스크를 미리 계산해 maskBySeed 에 채움. */
    private suspend fun warmMasks(outlinePath: String, seeds: List<NormalizedPoint>) {
        for (seed in seeds) {
            val result = outlineCache.regionFor(outlinePath, seed.x, seed.y)
            val bm = result.region.bitmap ?: continue
            _uiState.update { it.copy(maskBySeed = it.maskBySeed + (seed to bm)) }
        }
    }

    companion object {
        const val KEY_TARGET_ID = "targetId"
    }
}

/** Offset(픽셀) → NormalizedPoint(0~1). 화면 크기·회전과 무관한 좌표로 저장. */
private fun Offset.toNormalized(size: IntSize): NormalizedPoint {
    val w = size.width.coerceAtLeast(1).toFloat()
    val h = size.height.coerceAtLeast(1).toFloat()
    return NormalizedPoint(x = x / w, y = y / h)
}
