package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

/**
 * M4 드로잉 화면.
 * 레이아웃 (위에서 아래로):
 *   TopActionBar (뒤로/이름/되돌리기/처음부터)
 *   정사각 캔버스 (z=0 흰 배경 / z=1 사용자 그림 / z=2 외곽선)
 *   ToolBar (붓 / 지우개)
 *   StrokeWidthPicker (8 / 16 / 28dp)
 *   ColorPalette (5칼럼 × 4행, 총 20색)
 */
@Composable
fun DrawingScreen(
    modifier: Modifier = Modifier,
    viewModel: DrawingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBE6)),
    ) {
        TopActionBar(
            title = uiState.targetName,
            onBack = { /* M7 NavGraph 도입 시 */ },
            onUndo = viewModel::undo,
            onResetRequest = { showResetDialog = true },
            canUndo = uiState.strokes.isNotEmpty(),
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            // 짧은 변 기준 정사각형
            val side = if (maxWidth < maxHeight) maxWidth else maxHeight

            Box(
                modifier = Modifier
                    .size(side)
                    .background(Color.White), // z=0 흰 배경 (graphicsLayer 외부)
            ) {
                // z=1 사용자 드로잉 (graphicsLayer Offscreen 안에서 BlendMode 처리)
                DrawingCanvas(
                    strokes = uiState.strokes,
                    currentStroke = uiState.currentStroke,
                    onStrokeStart = viewModel::onDragStart,
                    onStrokeUpdate = viewModel::onDrag,
                    onStrokeEnd = viewModel::onDragEnd,
                    modifier = Modifier.fillMaxSize(),
                )

                // z=2 외곽선
                val outlinePath = uiState.outlinePath
                if (outlinePath != null) {
                    AsyncImage(
                        model = "file:///android_asset/$outlinePath",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    OutlinePlaceholder(modifier = Modifier.fillMaxSize())
                }
            }
        }

        ToolBar(
            currentTool = uiState.currentTool,
            onToolSelected = viewModel::changeTool,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
        )

        StrokeWidthPicker(
            currentWidthDp = uiState.currentWidthDp,
            onWidthSelected = viewModel::changeWidthDp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp),
        )

        ColorPalette(
            selectedColor = uiState.currentColor,
            onColorSelected = viewModel::changeColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }

    if (showResetDialog) {
        ResetConfirmDialog(
            onConfirm = {
                viewModel.reset()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false },
        )
    }
}

@Composable
private fun ResetConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "정말 다 지울까요?",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = "모든 그림이 사라져요.",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.heightIn(min = 56.dp),
            ) {
                Text("응, 지울래", style = MaterialTheme.typography.titleLarge)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 56.dp),
            ) {
                Text("아니", style = MaterialTheme.typography.titleLarge)
            }
        },
    )
}
