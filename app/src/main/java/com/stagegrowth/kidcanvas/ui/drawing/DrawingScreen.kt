package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
 * 가로 모드 우선 색칠 화면. 위에서 아래로:
 *   [상단 액션 바: 뒤로 | 붓·지우개 토글 | 이름 | Undo · Reset]
 *   [Row 4 분할:
 *     - 캔버스 (~75%)            정사각 비율 유지, 외곽선 z=2 오버레이
 *     - 세로 색상 띠 + ▶ (~13%)   드래그/탭으로 색 선택, 인디케이터가 따라 이동
 *     - 연필 굵기 (~12%)          연필 3개 (얇/중/굵), 촉이 현재 색으로 칠해짐
 *   ]
 *
 * 기존 가로 ColorPalette / StrokeWidthPicker / 큰 ToolBar 는 제거.
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
            currentTool = uiState.currentTool,
            onBack = { /* M7 NavGraph 도입 시 */ },
            onToolSelected = viewModel::changeTool,
            onUndo = viewModel::undo,
            onResetRequest = { showResetDialog = true },
            canUndo = uiState.strokes.isNotEmpty(),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // (1) 캔버스 ~75% — 정사각 비율 유지
            BoxWithConstraints(
                modifier = Modifier
                    .weight(75f)
                    .fillMaxHeight()
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                val side = if (maxWidth < maxHeight) maxWidth else maxHeight
                Box(
                    modifier = Modifier
                        .size(side)
                        .background(Color.White), // z=0 흰 배경 (graphicsLayer 외부)
                ) {
                    DrawingCanvas(
                        strokes = uiState.strokes,
                        currentStroke = uiState.currentStroke,
                        onStrokeStart = viewModel::onDragStart,
                        onStrokeUpdate = viewModel::onDrag,
                        onStrokeEnd = viewModel::onDragEnd,
                        modifier = Modifier.fillMaxSize(),
                    )
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

            // (2) 세로 색상 띠 + ▶ 인디케이터 ~13% (10 + 3)
            VerticalColorPickerArea(
                colors = DefaultStripColors,
                selectedColor = uiState.currentColor,
                onColorSelected = viewModel::changeColor,
                modifier = Modifier
                    .weight(13f)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
            )

            // (3) 연필 굵기 ~12%
            PencilWidthPicker(
                currentWidthDp = uiState.currentWidthDp,
                onWidthSelected = viewModel::changeWidthDp,
                currentColor = uiState.currentColor,
                modifier = Modifier
                    .weight(12f)
                    .fillMaxHeight()
                    .padding(8.dp),
            )
        }
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
