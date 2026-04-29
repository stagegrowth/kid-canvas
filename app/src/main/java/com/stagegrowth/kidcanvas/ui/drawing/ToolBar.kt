package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.domain.model.Tool

/**
 * 붓 / 지우개 두 도구 토글. 한글 텍스트 대신 그림(아이콘)으로 표현.
 * 선택된 쪽은 진한 배경 + 검정 3dp 테두리로 강조.
 */
@Composable
fun ToolBar(
    currentTool: Tool,
    onToolSelected: (Tool) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ToolButton(
            selected = currentTool == Tool.BRUSH,
            selectedBg = Color(0xFF2196F3),
            onClick = { onToolSelected(Tool.BRUSH) },
            modifier = Modifier.weight(1f),
        ) { tint ->
            Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = "붓",
                tint = tint,
                modifier = Modifier.size(28.dp),
            )
        }
        ToolButton(
            selected = currentTool == Tool.ERASER,
            selectedBg = Color(0xFF9E9E9E),
            onClick = { onToolSelected(Tool.ERASER) },
            modifier = Modifier.weight(1f),
        ) { tint ->
            EraserIcon(
                tint = tint,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}

@Composable
private fun RowScope.ToolButton(
    selected: Boolean,
    selectedBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (tint: Color) -> Unit,
) {
    val bg = if (selected) selectedBg else Color(0xFFE0E0E0)
    val tint = if (selected) Color.White else Color(0xFF333333)
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(bg)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color.Black else Color(0x33000000),
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content(tint)
    }
}

/**
 * 직접 그린 지우개 모양. monochrome (tint 색 하나로).
 * 위에서 보면: [본체 둥근 사각형 외곽선] + [중앙 가로 띠]
 */
@Composable
private fun EraserIcon(
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
        val strokeW = size.minDimension * 0.06f
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
