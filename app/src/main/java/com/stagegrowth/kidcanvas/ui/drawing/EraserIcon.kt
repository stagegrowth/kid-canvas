package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect

/**
 * 컬러풀 지우개 아이콘 — 5살이 즉시 "지우개" 로 인식.
 * 분홍 본체(아래) + 푸른 보라 띠(위) + 진한 분홍 외곽선.
 *
 * 흔한 학용품 지우개의 두 톤(고무 + 캡) 패턴을 단순화. tint 파라미터 없음 —
 * 선택/비선택 차이는 호출처(TopActionBar)에서 배경 원으로 표현.
 */
@Composable
fun EraserIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padX = w * 0.08f
        val top = h * 0.22f
        val bottom = h * 0.78f
        val bodyW = w - padX * 2f
        val bodyH = bottom - top
        val stripeBottom = top + bodyH * 0.40f // 위쪽 40% 가 푸른 띠
        val corner = CornerRadius(w * 0.12f)
        val outlineW = w * 0.04f

        // 1) 본체 분홍
        drawRoundRect(
            color = Color(0xFFFFB6C1),
            topLeft = Offset(padX, top),
            size = Size(bodyW, bodyH),
            cornerRadius = corner,
        )
        // 2) 위쪽 푸른 띠 — 본체 둥근 모서리를 따라가도록 clipRect 안에서 같은 둥근 사각 다시 그림
        clipRect(
            left = padX,
            top = top,
            right = padX + bodyW,
            bottom = stripeBottom,
        ) {
            drawRoundRect(
                color = Color(0xFF7986CB),
                topLeft = Offset(padX, top),
                size = Size(bodyW, bodyH),
                cornerRadius = corner,
            )
        }
        // 3) 분리선 (띠와 본체 경계)
        drawLine(
            color = Color(0xFF333333),
            start = Offset(padX, stripeBottom),
            end = Offset(padX + bodyW, stripeBottom),
            strokeWidth = outlineW * 0.5f,
        )
        // 4) 본체 외곽선 (진한 분홍)
        drawRoundRect(
            color = Color(0xFFD81B60),
            topLeft = Offset(padX, top),
            size = Size(bodyW, bodyH),
            cornerRadius = corner,
            style = Stroke(width = outlineW),
        )
    }
}
