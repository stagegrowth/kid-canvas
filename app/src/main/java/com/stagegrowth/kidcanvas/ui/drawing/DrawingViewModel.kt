package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.SavedStateHandle
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
 *   - SavedStateHandle 의 KEY_TARGET_ID 는 NavGraph 의 route "drawing/{targetId}" 에 의해 자동 채워짐.
 */
@HiltViewModel
class DrawingViewModel @Inject constructor(
    private val repository: ColoringRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val targetId: String = savedStateHandle.get<String>(KEY_TARGET_ID).orEmpty()

    private val _uiState = MutableStateFlow(DrawingUiState(targetId = targetId))
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

    init {
        if (targetId.isNotBlank()) {
            viewModelScope.launch {
                // 1) 메타데이터(이름, 외곽선) 해석
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
                }
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
