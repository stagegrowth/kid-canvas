package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.ViewModel
import com.stagegrowth.kidcanvas.domain.model.NormalizedPoint
import com.stagegrowth.kidcanvas.domain.model.Stroke
import com.stagegrowth.kidcanvas.domain.model.Tool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * 드로잉 화면 상태 보유 + 사용자 입력 처리.
 * Spring 비유: @Controller 또는 @Service. StateFlow 가 응답 모델, on*() 가 핸들러.
 *
 * M4: 색·굵기·도구 선택 + Undo/Reset 추가. (자동 저장은 M5)
 */
@HiltViewModel
class DrawingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(DrawingUiState())
    val uiState: StateFlow<DrawingUiState> = _uiState.asStateFlow()

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
    }

    /** 마지막 stroke 한 개 제거. 비어 있으면 무시. */
    fun undo() {
        _uiState.update { state ->
            if (state.strokes.isEmpty()) state
            else state.copy(strokes = state.strokes.dropLast(1))
        }
    }

    /** 전체 초기화. 다이얼로그 확인 후에만 호출되어야 함. */
    fun reset() {
        _uiState.update { it.copy(strokes = emptyList(), currentStroke = null) }
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
}

/** Offset(픽셀) → NormalizedPoint(0~1). 화면 크기·회전과 무관한 좌표로 저장. */
private fun Offset.toNormalized(size: IntSize): NormalizedPoint {
    val w = size.width.coerceAtLeast(1).toFloat()
    val h = size.height.coerceAtLeast(1).toFloat()
    return NormalizedPoint(x = x / w, y = y / h)
}
