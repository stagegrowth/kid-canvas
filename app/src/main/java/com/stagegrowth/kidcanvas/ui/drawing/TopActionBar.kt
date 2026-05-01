package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.domain.model.Tool

/**
 * 상단 액션 바.
 *   [뒤로] | [붓 토글 + 색 미리보기] [지우개 토글] | 캐릭터 이름 | [되돌리기] [처음부터]
 *
 * 5살용 시각 피드백:
 *   - 선택된 도구 아이콘은 진한 배경 + alpha 1.0
 *   - 비선택은 alpha 0.4 (흐릿)
 *   - 붓 우상단 작은 점 = 현재 선택 색 미리보기 (어떤 색이라도 또렷이 보이도록 점만 별도)
 *
 * Spring 비유: alpha 는 CSS opacity 와 동일. 컨테이너 자체는 그대로 두고 보이기만 흐려진다.
 */
@Composable
fun TopActionBar(
    title: String,
    currentTool: Tool,
    currentColor: Long,
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
        BrushToggleIcon(
            selected = currentTool == Tool.BRUSH,
            currentColor = currentColor,
            onClick = { onToolSelected(Tool.BRUSH) },
        )
        EraserToggleIcon(
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

/**
 * 붓 토글. 선택 시 파란 톤 강조 배경 + 우상단에 currentColor 점(색 미리보기).
 */
@Composable
private fun BrushToggleIcon(
    selected: Boolean,
    currentColor: Long,
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
        // 토글 자체는 alpha 로 흐릿. 색 점은 alpha 영향 안 받게 별도 Box 로 분리.
        Box(
            modifier = Modifier.alpha(if (selected) 1f else 0.4f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Brush,
                contentDescription = "붓",
                tint = tint,
                modifier = Modifier.size(28.dp),
            )
        }
        // 우상단 색 미리보기 — 어떤 색이든 또렷이 보이도록 어두운 테두리.
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(currentColor))
                .border(width = 1.dp, color = Color(0x66000000), shape = CircleShape),
        )
    }
}

/** 지우개 토글. 선택 시 분홍 톤 강조 배경. 미선택 시 alpha 0.4. */
@Composable
private fun EraserToggleIcon(
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Color(0xFFFCE4EC) else Color.Transparent
    Box(
        modifier = Modifier
            .size(56.dp)
            .padding(4.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.alpha(if (selected) 1f else 0.4f)) {
            EraserIcon(
                modifier = Modifier.size(width = 40.dp, height = 24.dp),
            )
        }
    }
}
