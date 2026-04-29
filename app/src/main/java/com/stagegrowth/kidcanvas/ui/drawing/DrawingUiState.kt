package com.stagegrowth.kidcanvas.ui.drawing

import com.stagegrowth.kidcanvas.domain.model.Stroke
import com.stagegrowth.kidcanvas.domain.model.Tool

/**
 * DrawingScreen 의 단일 UI 상태.
 * - strokes: 완료된 획들
 * - currentStroke: 손가락이 떨어지기 전 진행 중인 획 (null 이면 비활성)
 * - currentColor / currentWidthDp / currentTool: M3 PoC 단계에선 고정값.
 *   M4 에서 팔레트·굵기 슬라이더로 바꿈.
 */
data class DrawingUiState(
    val targetId: String = "preview",
    val targetName: String = "테스트",
    val outlinePath: String? = null,
    val strokes: List<Stroke> = emptyList(),
    val currentStroke: Stroke? = null,
    val currentColor: Long = 0xFFFF0000L, // ARGB: 빨강
    val currentWidthDp: Float = 16f,
    val currentTool: Tool = Tool.BRUSH,
)
