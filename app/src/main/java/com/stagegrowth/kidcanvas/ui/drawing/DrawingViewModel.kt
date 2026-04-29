package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 * 드로잉 화면 상태 보유 + 사용자 입력 처리 + 자동 저장/복원.
 *
 * Spring 비유:
 *   - @Service. StateFlow 가 응답 모델, on*() 가 핸들러.
 *   - viewModelScope.launch 는 ViewModel 라이프사이클에 묶인 @Async 컨텍스트 (화면 종료 시 자동 취소).
 *
 * targetId 는 setTargetId() 로 화면 진입 시 한 번 주입.
 * (M7 NavGraph 도입 시 SavedStateHandle 로 자동 주입되도록 교체 예정.)
 */
@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val repository: ColoringRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    private var initialized: Boolean = false

    /**
     * 화면이 알려준 targetId 로 ViewModel 을 활성화.
     * idempotent 하게 — 같은 id 로 재호출되면 무시 (LaunchedEffect 가 재구성될 때 안전).
     */
    fun setTargetId(targetId: String) {
        if (initialized && _uiState.value.targetId == targetId) return
        initialized = true
        _uiState.update {
            it.copy(
                targetId = targetId,
                targetName = "",
                outlinePath = null,
                strokes = emptyList(),
                currentStroke = null,
            )
        }
        viewModelScope.launch {
            // 1) 메타데이터(이름, 외곽선) 해석
            val target = repository.getCategories().first()
                .flatMap { it.targets }
                .firstOrNull { it.id == targetId }
            if (target != null && _uiState.value.targetId == targetId) {
                _uiState.update {
                    it.copy(targetName = target.name, outlinePath = target.outline)
                }
            }
            // 2) 저장된 strokes 복원
            val saved = repository.getDrawingState(targetId).first()
            if (saved != null && saved.strokes.isNotEmpty()
                && _uiState.value.targetId == targetId
            ) {
                _uiState.update { it.copy(strokes = saved.strokes) }
            }
        }
    }

    fun onDragStart(offset: Offset, canvasSize: IntSize) {
        val point = offset.toNormalized(canvasSize)
        _uiState.update { state ->
            state.copy(
                currentStroke = Stroke(
                    color = state.currentColor,
                    widthDp = state.currentWidthDp,
                    tool = state.currentTool,
                    points = listOf(point),
                )
            )
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
        val targetId = _uiState.value.targetId
        _uiState.update { it.copy(strokes = emptyList(), currentStroke = null) }
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
}

/** Offset(픽셀) → NormalizedPoint(0~1). 화면 크기·회전과 무관한 좌표로 저장. */
private fun Offset.toNormalized(size: IntSize): NormalizedPoint {
    val w = size.width.coerceAtLeast(1).toFloat()
    val h = size.height.coerceAtLeast(1).toFloat()
    return NormalizedPoint(x = x / w, y = y / h)
}
