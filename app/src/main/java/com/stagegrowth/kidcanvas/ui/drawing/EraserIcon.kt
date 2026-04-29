package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * 직접 그린 지우개 모양 (Material Icons 에 적당한 게 없어 자체 그래픽).
 * monochrome — tint 색 하나로 외곽선 + 중앙 띠.
 *
 * TopActionBar 의 도구 토글, 향후 다른 도구 UI 등에서 재사용.
 */
@Composable
fun EraserIcon(
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padX = w * 0.12f
        val top = h * 0.30f
        val bottom = h * 0.70f
        val midY = (top + bottom) / 2f
        val strokeW = size.minDimension * 0.08f
        val corner = CornerRadius(size.minDimension * 0.12f)

        // 본체 둥근 사각형 (외곽선만)
        drawRoundRect(
            color = tint,
            topLeft = Offset(padX, top),
            size = Size(w - padX * 2, bottom - top),
            cornerRadius = corner,
            style = Stroke(width = strokeW),
        )
        // 중앙 가로 띠 — 지우개의 분리 표시
        drawLine(
            color = tint,
            start = Offset(padX + strokeW, midY),
            end = Offset(w - padX - strokeW, midY),
            strokeWidth = strokeW,
        )
    }
}
