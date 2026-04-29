package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

/**
 * M3 PoC 드로잉 화면.
 * 레이아웃:
 *   [상단] 캐릭터 이름
 *   [중앙] 정사각 캔버스 (z=0 흰 배경 / z=1 사용자 그림 / z=2 외곽선)
 *   [하단] 지우기 버튼
 */
@Composable
fun DrawingScreen(
    modifier: Modifier = Modifier,
    viewModel: DrawingViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBE6)), // 따뜻한 크림색 배경
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = uiState.targetName,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                    .background(Color.White), // z=0 흰 배경
            ) {
                // z=1 사용자 드로잉
                DrawingCanvas(
                    strokes = uiState.strokes,
                    currentStroke = uiState.currentStroke,
                    onStrokeStart = viewModel::onDragStart,
                    onStrokeUpdate = viewModel::onDrag,
                    onStrokeEnd = viewModel::onDragEnd,
                    modifier = Modifier.fillMaxSize(),
                )

                // z=2 외곽선 (asset 이 있으면 PNG, 없으면 placeholder)
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

        Button(
            onClick = viewModel::clearAll,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(64.dp), // 5살 UX: 터치 타깃 ≥ 56dp
        ) {
            Text(
                text = "지우기",
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}
