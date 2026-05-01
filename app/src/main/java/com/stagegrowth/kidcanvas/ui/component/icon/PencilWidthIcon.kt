package com.stagegrowth.kidcanvas.ui.component.icon

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.ui.util.darkerColor

/**
 * 연필 굵기 아이콘 (B안)
 * - 선택 색이 연필 몸통에 반영됨
 * - 굵기 박스 안에 연필이 그려지고, 굵기에 따라 연필 폭과 끝 점이 달라짐
 * - 선택됨: 노란 배경 + 노란 외곽선
 * - 미선택: 흰 배경
 *
 * @param strokeWidth 실제 그릴 선의 굵기 (얇은 8dp / 중간 16dp / 굵은 28dp)
 * @param currentColor 선택된 색
 * @param isSelected 이 굵기가 선택되었는지
 */
@Composable
fun PencilWidthIcon(
    strokeWidth: Dp,
    currentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFFFFF8DC) else Color.White
    val borderColor = if (isSelected) Color(0xFFF4D03F) else Color.Transparent

    // 굵기에 따라 연필 폭 결정 (시각적 굵기 차이 명확)
    val pencilWidthRatio = when {
        strokeWidth <= 10.dp -> 0.10f  // 얇은
        strokeWidth <= 20.dp -> 0.21f  // 중간
        else -> 0.33f                    // 굵은
    }
    val tipDotRadiusRatio = when {
        strokeWidth <= 10.dp -> 0.025f
        strokeWidth <= 20.dp -> 0.075f
        else -> 0.137f
    }

    Box(
        modifier = modifier
            .size(width = 64.dp, height = 88.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = 56.dp, height = 80.dp)) {
            drawPencil(
                pencilWidthRatio = pencilWidthRatio,
                tipDotRadiusRatio = tipDotRadiusRatio,
                bodyColor = currentColor
            )
        }
    }
}

/**
 * 연필 그리기 — 분홍 지우개(상단) + 메탈 띠 + 색 몸통 + 살구 깎인 끝 + 검정 심 + 끝 점
 */
private fun DrawScope.drawPencil(
    pencilWidthRatio: Float,
    tipDotRadiusRatio: Float,
    bodyColor: Color
) {
    val w = size.width
    val h = size.height

    val pencilWidth = w * pencilWidthRatio
    val centerX = w / 2
    val left = centerX - pencilWidth / 2

    // 1. 분홍 지우개 (상단)
    val eraserTop = h * 0.05f
    val eraserHeight = h * 0.10f
    drawRect(
        color = Color(0xFFF48FB1),
        topLeft = Offset(left, eraserTop),
        size = Size(pencilWidth, eraserHeight)
    )

    // 2. 메탈 띠
    val metalTop = eraserTop + eraserHeight
    val metalHeight = h * 0.05f
    drawRect(
        color = Color(0xFFB0BEC5),
        topLeft = Offset(left, metalTop),
        size = Size(pencilWidth, metalHeight)
    )

    // 3. 색 몸통 (선택 색 반영)
    val bodyTop = metalTop + metalHeight
    val bodyHeight = h * 0.475f
    drawRect(
        color = bodyColor,
        topLeft = Offset(left, bodyTop),
        size = Size(pencilWidth, bodyHeight)
    )

    // 몸통 외곽선
    drawRect(
        color = darkerColor(bodyColor),
        topLeft = Offset(left, bodyTop),
        size = Size(pencilWidth, bodyHeight),
        style = Stroke(width = 0.8.dp.toPx())
    )

    // 4. 살구색 깎인 부분
    val woodTop = bodyTop + bodyHeight
    val woodHeight = h * 0.14f
    val woodPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(left, woodTop)
        lineTo(left + pencilWidth, woodTop)
        lineTo(centerX, woodTop + woodHeight)
        close()
    }
    drawPath(woodPath, color = Color(0xFFFFD8B5))

    // 5. 검정 심
    val nibTop = woodTop + woodHeight * 0.45f
    val nibHeight = h * 0.10f
    val nibPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(left + pencilWidth * 0.15f, nibTop)
        lineTo(left + pencilWidth * 0.85f, nibTop)
        lineTo(centerX, nibTop + nibHeight)
        close()
    }
    drawPath(nibPath, color = Color(0xFF1A0E12))

    // 6. 끝 아래 점 — 굵기 직접 표현 (선택 색)
    val dotY = h * 0.92f
    drawLine(
        color = bodyColor,
        start = Offset(centerX, dotY),
        end = Offset(centerX, dotY),
        strokeWidth = (h * tipDotRadiusRatio * 2),
        cap = StrokeCap.Round
    )
}
