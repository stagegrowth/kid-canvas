package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.domain.model.Tool

/**
 * 붓 / 지우개 두 도구 토글.
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ToolButton(
            label = "붓",
            selected = currentTool == Tool.BRUSH,
            selectedBg = Color(0xFF2196F3),
            onClick = { onToolSelected(Tool.BRUSH) },
            modifier = Modifier.weight(1f),
        )
        ToolButton(
            label = "지우개",
            selected = currentTool == Tool.ERASER,
            selectedBg = Color(0xFF9E9E9E),
            onClick = { onToolSelected(Tool.ERASER) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RowScope.ToolButton(
    label: String,
    selected: Boolean,
    selectedBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) selectedBg else Color(0xFFE0E0E0)
    val fg = if (selected) Color.White else Color.Black
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(bg)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color.Black else Color(0x33000000),
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
