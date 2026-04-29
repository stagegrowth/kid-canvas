package com.stagegrowth.kidcanvas.ui.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.ColoringTarget
import androidx.compose.foundation.gestures.detectTapGestures

/**
 * 카테고리 내 캐릭터 선택 화면.
 *   - 상단: 뒤로 + 카테고리 이름
 *   - 본문: 2열 그리드, 외곽선 썸네일 + 이름, 진행 중인 항목은 우상단 🎨 배지
 *   - 길게 누르면 "이 그림 다시 시작?" 다이얼로그
 */
@Composable
fun PickerScreen(
    onBack: () -> Unit,
    onTargetClick: (targetId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PickerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    PickerScreenContent(
        uiState = uiState,
        onBack = onBack,
        onTargetClick = onTargetClick,
        onResetTarget = viewModel::resetTarget,
        modifier = modifier,
    )
}

@Composable
private fun PickerScreenContent(
    uiState: PickerUiState,
    onBack: () -> Unit,
    onTargetClick: (targetId: String) -> Unit,
    onResetTarget: (targetId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var resetCandidate by remember { mutableStateOf<ColoringTarget?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBE6)),
    ) {
        PickerTopBar(
            title = uiState.category?.name ?: "그림 고르기",
            onBack = onBack,
        )

        when {
            !uiState.isLoaded -> Spacer(modifier = Modifier.fillMaxSize())

            uiState.category == null -> EmptyState(
                title = "카테고리를 찾을 수 없어요",
                detail = "콘텐츠가 갱신되었는지 확인해 주세요.",
            )

            uiState.targets.isEmpty() -> EmptyState(
                title = "이 카테고리엔 아직 친구가 없어요",
                detail = null,
            )

            else -> TargetGrid(
                targets = uiState.targets,
                startedTargetIds = uiState.startedTargetIds,
                onTargetClick = onTargetClick,
                onTargetLongPress = { resetCandidate = it },
            )
        }
    }

    val candidate = resetCandidate
    if (candidate != null) {
        ResetTargetDialog(
            targetName = candidate.name,
            onConfirm = {
                onResetTarget(candidate.id)
                resetCandidate = null
            },
            onDismiss = { resetCandidate = null },
        )
    }
}

@Composable
private fun PickerTopBar(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(56.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로",
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun TargetGrid(
    targets: List<ColoringTarget>,
    startedTargetIds: Set<String>,
    onTargetClick: (targetId: String) -> Unit,
    onTargetLongPress: (target: ColoringTarget) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        items(targets, key = { it.id }) { target ->
            TargetCard(
                target = target,
                started = startedTargetIds.contains(target.id),
                onClick = { onTargetClick(target.id) },
                onLongPress = { onTargetLongPress(target) },
            )
        }
    }
}

@Composable
private fun TargetCard(
    target: ColoringTarget,
    started: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            // tap = 진입, long press = 다시 시작 다이얼로그.
            // detectTapGestures 는 두 제스처를 한 핸들러에서 분기 처리.
            .pointerInput(target.id) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { onLongPress() },
                )
            }
            .padding(8.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/${target.thumbnail}")
                        .build(),
                    contentDescription = target.name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = target.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF333333),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
        }

        if (started) {
            // 우상단 🎨 배지 — 진행 중 표시.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF59D)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "🎨", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ResetTargetDialog(
    targetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "이 그림 다시 시작할까?",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = "$targetName 그림이 모두 사라져요.",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("응, 다시 시작", style = MaterialTheme.typography.titleLarge)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("아니", style = MaterialTheme.typography.titleLarge)
            }
        },
    )
}

@Composable
private fun EmptyState(title: String, detail: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        if (detail != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF777777),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 480)
@Composable
private fun PickerScreenPreview() {
    PickerScreenContent(
        uiState = PickerUiState(
            isLoaded = true,
            category = Category(
                id = "tinyping",
                name = "티니핑",
                themeColor = "#FFC0CB",
                thumbnail = "thumbs/tinyping.png",
                targets = (1..6).map {
                    ColoringTarget(
                        id = "tp_$it",
                        name = "핑이$it",
                        outline = "outlines/tp_$it.png",
                        thumbnail = "thumbs/tp_$it.png",
                        addedAt = 0,
                    )
                },
            ),
            startedTargetIds = setOf("tp_1", "tp_3"),
        ),
        onBack = {},
        onTargetClick = {},
        onResetTarget = {},
    )
}
