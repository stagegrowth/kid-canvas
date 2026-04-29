package com.stagegrowth.kidcanvas.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.CategoryProgress
import com.stagegrowth.kidcanvas.domain.model.ColoringTarget

/**
 * 카테고리 선택 화면. 5살 기준:
 *   - 큰 컬러 카드, 한 줄 큰 글씨 카테고리 이름
 *   - 진행 표시는 작은 글씨로 부담 없이
 *   - 비어 있을 때는 큰 글씨 안내 + 어른용 작은 안내 (콘텐츠 추가 방법)
 */
@Composable
fun CategoryScreen(
    onCategoryClick: (categoryId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    CategoryScreenContent(
        uiState = uiState,
        onCategoryClick = onCategoryClick,
        modifier = modifier,
    )
}

@Composable
private fun CategoryScreenContent(
    uiState: CategoryUiState,
    onCategoryClick: (categoryId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBE6))
            .padding(16.dp),
    ) {
        Text(
            text = "어떤 친구를 그릴까?",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center,
        )

        when {
            !uiState.isLoaded -> {
                // 첫 emit 전: 화면을 비워두는 것이 깜박임이 없어 5살 UX 에 더 나음.
                Spacer(modifier = Modifier.fillMaxSize())
            }

            uiState.items.isEmpty() -> EmptyCategoriesState()

            else -> CategoryGrid(
                items = uiState.items,
                onCategoryClick = onCategoryClick,
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    items: List<CategoryListItem>,
    onCategoryClick: (categoryId: String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items, key = { it.category.id }) { item ->
            CategoryCard(
                category = item.category,
                progress = item.progress,
                onClick = { onCategoryClick(item.category.id) },
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    progress: CategoryProgress,
    onClick: () -> Unit,
) {
    val themeBg = parseHexColor(category.themeColor) ?: Color(0xFFFFE0B2)
    val onTheme = contrastingTextColor(themeBg)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(themeBg)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = onTheme,
            )
            Text(
                text = formatProgress(progress),
                style = MaterialTheme.typography.bodyLarge,
                color = onTheme.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun EmptyCategoriesState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "아직 그림이 없어요",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        // 어른용 안내 — 작게.
        Text(
            text = "tools/content-builder 로 outlines/, thumbs/, content.json 을 채워주세요.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = Color(0xFF777777),
            modifier = Modifier.padding(horizontal = 32.dp),
        )
    }
}

/** "8개 중 3개 그렸어요" / 하나도 안 그렸으면 "8개 친구". */
private fun formatProgress(p: CategoryProgress): String =
    if (p.total == 0) "준비 중"
    else if (p.started == 0) "${p.total}개 친구"
    else "${p.total}개 중 ${p.started}개 그렸어요"

/**
 * "#RRGGBB" 또는 "#AARRGGBB" 16진 문자열을 Compose Color 로.
 * 형식이 맞지 않으면 null. CategoryCard 가 폴백 색으로 fallback.
 */
private fun parseHexColor(hex: String): Color? {
    val raw = hex.removePrefix("#")
    return try {
        when (raw.length) {
            6 -> {
                val rgb = raw.toLong(16)
                Color(0xFF000000L or rgb)
            }
            8 -> Color(raw.toLong(16))
            else -> null
        }
    } catch (e: NumberFormatException) {
        null
    }
}

/** 배경이 밝으면 검정 글씨, 어두우면 흰 글씨. (5살 가독성용) */
private fun contrastingTextColor(bg: Color): Color {
    val luminance = (0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue)
    return if (luminance > 0.6f) Color(0xFF222222) else Color.White
}

@Preview(showBackground = true, widthDp = 720, heightDp = 480)
@Composable
private fun CategoryScreenPreview() {
    CategoryScreenContent(
        uiState = CategoryUiState(
            isLoaded = true,
            items = listOf(
                CategoryListItem(
                    category = Category(
                        id = "tinyping",
                        name = "티니핑",
                        themeColor = "#FFC0CB",
                        thumbnail = "thumbs/tinyping.png",
                        targets = (1..8).map {
                            ColoringTarget(
                                id = "tp_$it",
                                name = "핑이$it",
                                outline = "outlines/tp_$it.png",
                                thumbnail = "thumbs/tp_$it.png",
                                addedAt = 0,
                            )
                        },
                    ),
                    progress = CategoryProgress(total = 8, started = 3),
                ),
                CategoryListItem(
                    category = Category(
                        id = "animals",
                        name = "동물 친구들",
                        themeColor = "#A5D6A7",
                        thumbnail = "thumbs/animals.png",
                        targets = (1..5).map {
                            ColoringTarget(
                                id = "an_$it",
                                name = "동물$it",
                                outline = "outlines/an_$it.png",
                                thumbnail = "thumbs/an_$it.png",
                                addedAt = 0,
                            )
                        },
                    ),
                    progress = CategoryProgress(total = 5, started = 0),
                ),
            ),
        ),
        onCategoryClick = {},
    )
}

@Preview(showBackground = true, widthDp = 720, heightDp = 480)
@Composable
private fun CategoryScreenEmptyPreview() {
    CategoryScreenContent(
        uiState = CategoryUiState(isLoaded = true, items = emptyList()),
        onCategoryClick = {},
    )
}
