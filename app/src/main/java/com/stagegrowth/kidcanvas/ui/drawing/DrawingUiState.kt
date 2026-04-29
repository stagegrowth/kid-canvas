package com.stagegrowth.kidcanvas.ui.drawing

import com.stagegrowth.kidcanvas.domain.model.Stroke
import com.stagegrowth.kidcanvas.domain.model.Tool

/**
 * DrawingScreen 의 단일 UI 상태.
 * - strokes: 완료된 획들
 * - currentStroke: 손가락이 떨어지기 전 진행 중인 획 (null 이면 비활성)
 * - currentColor / currentWidthDp / currentTool: 사용자가 토글로 선택한 현재 도구 상태.
 *
 * 기본 색은 새 색상 띠(DefaultStripColors) 의 빨강 (#FF5252) 와 일치하도록 맞춤 —
 * 그래야 처음 진입 시 인디케이터(▶) 가 빨강 위치에 정렬.
 */
data class DrawingUiState(
    val targetId: String = "test_target",
    val targetName: String = "테스트",
    val outlinePath: String? = null,
    val strokes: List<Stroke> = emptyList(),
    val currentStroke: Stroke? = null,
    val currentColor: Long = 0xFFFF5252L, // 빨강 — DefaultStripColors[4]
    val currentWidthDp: Float = 16f,
    val currentTool: Tool = Tool.BRUSH,
)
