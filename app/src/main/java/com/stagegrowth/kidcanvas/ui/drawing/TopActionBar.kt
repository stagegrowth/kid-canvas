package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.domain.model.Tool
import com.stagegrowth.kidcanvas.ui.component.icon.ColorPencilIcon
import com.stagegrowth.kidcanvas.ui.component.icon.EraserIcon

/**
 * 상단 액션 바.
 *   [뒤로] | [색연필(현재 색)] [지우개] | 캐릭터 이름 | [되돌리기] [처음부터]
 *
 * 5살용 시각 피드백:
 *   - 선택된 도구는 둥근 컬러 배경 + opacity 1.0
 *   - 비선택은 배경 없음 + opacity 0.4 (흐릿)
 *   - 색연필 몸통 색이 currentColor 따라 즉시 바뀜
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
        ColorPencilIcon(
            currentColor = Color(currentColor),
            isSelected = currentTool == Tool.BRUSH,
            onClick = { onToolSelected(Tool.BRUSH) },
        )
        EraserIcon(
            isSelected = currentTool == Tool.ERASER,
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
