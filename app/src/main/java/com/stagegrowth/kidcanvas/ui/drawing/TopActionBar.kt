package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.domain.model.Tool

/**
 * 상단 액션 바.
 *   [뒤로] | [붓 토글] [지우개 토글] | 캐릭터 이름 | [되돌리기] [처음부터]
 *
 * 도구 토글은 레이아웃 재구성으로 큰 ToolBar 가 사라지면서 여기로 이동.
 * 선택된 도구는 옅은 배경 원으로 강조 (5살에게 즉각 시각 피드백).
 */
@Composable
fun TopActionBar(
    title: String,
    currentTool: Tool,
    onBack: () -> Unit,
    onToolSelected: (Tool) -> Unit,
    onUndo: () -> Unit,
    onResetRequest: () -> Unit,
    canUndo: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로",
            )
        }
        ToolToggleIcon(
            tool = Tool.BRUSH,
            selected = currentTool == Tool.BRUSH,
            onClick = { onToolSelected(Tool.BRUSH) },
        )
        ToolToggleIcon(
            tool = Tool.ERASER,
            selected = currentTool == Tool.ERASER,
            onClick = { onToolSelected(Tool.ERASER) },
        )
        Text(
            text = title,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        IconButton(
            onClick = onUndo,
            enabled = canUndo,
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Undo,
                contentDescription = "되돌리기",
            )
        }
        IconButton(onClick = onResetRequest, modifier = Modifier.size(56.dp)) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "처음부터",
            )
        }
    }
}

/** 붓/지우개 토글 — 56dp 영역 + 옅은 강조 배경. */
@Composable
private fun ToolToggleIcon(
    tool: Tool,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color(0xFFE3F2FD) else Color.Transparent
    val tint = if (selected) Color(0xFF0D47A1) else Color(0xFF555555)
    Box(
        modifier = Modifier
            .size(56.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        when (tool) {
            Tool.BRUSH -> Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = "붓",
                tint = tint,
                modifier = Modifier.size(28.dp),
            )

            Tool.ERASER -> EraserIcon(
                tint = tint,
                modifier = Modifier.size(32.dp),
            )
        }
    }
}
