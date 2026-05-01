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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.ui.util.darkerColor

/**
 * 색연필 아이콘 (그리기 모드)
 * - 선택 색이 색연필 몸통에 반영됨
 * - 활성 시: 하늘색 둥근 배경 + 살짝 그림자
 * - 비활성 시: 배경 없음 + opacity 0.4
 *
 * Spring 비유: @Component 같은 재사용 가능한 UI 조각
 */
@Composable
fun ColorPencilIcon(
    currentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFBBDEFB) else Color.Transparent
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

            // -40도 회전 적용
            rotate(degrees = -40f, pivot = Offset(w / 2, h / 2)) {
                // 색연필 몸통 (선택 색 반영)
                val bodyLeft = w * 0.35f
                val bodyTop = h * 0.15f
                val bodyWidth = w * 0.30f
                val bodyHeight = h * 0.55f

                drawRect(
                    color = currentColor,
                    topLeft = Offset(bodyLeft, bodyTop),
                    size = Size(bodyWidth, bodyHeight)
                )

                // 위쪽 진한 띠 (선택 색의 어두운 버전)
                drawRect(
                    color = darkerColor(currentColor),
                    topLeft = Offset(bodyLeft, bodyTop),
                    size = Size(bodyWidth, h * 0.075f)
                )

                // 몸통 외곽선 (검정)
                drawRect(
                    color = Color(0xFF1A0E12),
                    topLeft = Offset(bodyLeft, bodyTop),
                    size = Size(bodyWidth, bodyHeight),
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // 깎인 끝 (살구색 나무)
                val tipPath = Path().apply {
                    moveTo(bodyLeft, bodyTop + bodyHeight)
                    lineTo(bodyLeft + bodyWidth, bodyTop + bodyHeight)
                    lineTo(bodyLeft + bodyWidth * 0.67f, bodyTop + bodyHeight + h * 0.15f)
                    lineTo(bodyLeft + bodyWidth * 0.33f, bodyTop + bodyHeight + h * 0.15f)
                    close()
                }
                drawPath(tipPath, color = Color(0xFFFFD8B5))
                drawPath(tipPath, color = Color(0xFF1A0E12), style = Stroke(width = 1.5.dp.toPx()))

                // 검정 심 (펜촉)
                val nibPath = Path().apply {
                    moveTo(bodyLeft + bodyWidth * 0.25f, bodyTop + bodyHeight + h * 0.10f)
                    lineTo(bodyLeft + bodyWidth * 0.75f, bodyTop + bodyHeight + h * 0.10f)
                    lineTo(bodyLeft + bodyWidth * 0.5f, bodyTop + bodyHeight + h * 0.20f)
                    close()
                }
                drawPath(nibPath, color = Color(0xFF1A0E12))
            }
        }
    }
}
