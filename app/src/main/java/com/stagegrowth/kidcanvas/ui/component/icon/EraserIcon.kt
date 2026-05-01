package com.stagegrowth.kidcanvas.ui.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

/**
 * 지우개 아이콘 (지우기 모드)
 * - 흰 몸통 + 파란 종이 라벨 (학교 지우개 모양)
 * - 활성 시: 분홍 둥근 배경 + 살짝 그림자
 * - 비활성 시: 배경 없음 + opacity 0.4
 */
@Composable
fun EraserIcon(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFFFE0EC) else Color.Transparent
    val iconAlpha = if (isSelected) 1f else 0.4f

    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .alpha(iconAlpha),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val w = size.width
            val h = size.height

            // +15도 회전 (색연필과 반대 방향)
            rotate(degrees = 15f, pivot = Offset(w / 2, h / 2)) {
                val bodyLeft = w * 0.15f
                val bodyTop = h * 0.325f
                val bodyWidth = w * 0.70f
                val bodyHeight = h * 0.35f

                // 흰 몸통
                drawRect(
                    color = Color.White,
                    topLeft = Offset(bodyLeft, bodyTop),
                    size = Size(bodyWidth, bodyHeight)
                )

                // 파란 종이 라벨 (가운데 가로로)
                val labelTop = bodyTop + bodyHeight * 0.30f
                val labelHeight = bodyHeight * 0.43f
                drawRect(
                    color = Color(0xFF1976D2),
                    topLeft = Offset(bodyLeft, labelTop),
                    size = Size(bodyWidth, labelHeight)
                )

                // 라벨 위·아래 검정 선
                drawLine(
                    color = Color(0xFF1A0E12),
                    start = Offset(bodyLeft, labelTop),
                    end = Offset(bodyLeft + bodyWidth, labelTop),
                    strokeWidth = 1.4.dp.toPx()
                )
                drawLine(
                    color = Color(0xFF1A0E12),
                    start = Offset(bodyLeft, labelTop + labelHeight),
                    end = Offset(bodyLeft + bodyWidth, labelTop + labelHeight),
                    strokeWidth = 1.4.dp.toPx()
                )

                // 몸통 전체 외곽선
                drawRect(
                    color = Color(0xFF1A0E12),
                    topLeft = Offset(bodyLeft, bodyTop),
                    size = Size(bodyWidth, bodyHeight),
                    style = Stroke(width = 1.6.dp.toPx())
                )
            }
        }
    }
}
