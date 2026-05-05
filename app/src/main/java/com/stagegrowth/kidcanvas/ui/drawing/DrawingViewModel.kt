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
            viewModelScope.launch {
                val result = outlineCache.regionFor(outlinePath, point.x, point.y)
                val bitmap = result.region.bitmap
                val diag = result.diagnostic

                // 진단 로그 — 마스크가 화면 전체로 펼쳐지는 버그 추적용
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

                _uiState.update { st ->
                    // 사용자가 이 stroke 끝낸 직후라면 무시 (race)
                    val cur = st.currentStroke
                    if (cur == null || cur.seed != point) st
                    else if (bitmap == null) st // 외곽선 위 시작 → 자유 드로잉 (마스크 없음)
                    else st.copy(
                        activeMask = bitmap,
                        // 완료 시점에 maskBySeed 도 미리 채워둬서 onDragEnd 후 즉시 활용
                        maskBySeed = st.maskBySeed + (point to bitmap),
                    )
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
