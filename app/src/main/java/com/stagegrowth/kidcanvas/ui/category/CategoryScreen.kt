package com.stagegrowth.kidcanvas.ui.category

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.CategoryProgress
import com.stagegrowth.kidcanvas.domain.model.ColoringTarget

/**
 * 카테고리 선택 화면. 5살 기준:
 *   - 큰 컬러 카드, 안에는 카테고리 캐릭터 4 개 미리보기 콜라주
 *     (글을 못 읽어도 "여기에 어떤 친구들이 있는지" 그림으로 인식)
 *   - 카드 위쪽에 카테고리 이름, 아래쪽에 진행 표시
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
            .clip(RoundedCornerShape(20.dp))
            .background(themeBg)
            .clickable(onClick = onClick)
            .padding(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = onTheme,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategoryThumbCollage(
                category = category,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = formatProgress(progress),
                style = MaterialTheme.typography.bodyMedium,
                color = onTheme.copy(alpha = 0.85f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * 카테고리 안의 캐릭터 썸네일을 콜라주로 표시.
 *   - 0 개 : 빈 영역
 *   - 1 개 : 한 칸 가득
 *   - 2 개 : 좌우 분할
 *   - 3 개 : 왼쪽 큰 칸 + 오른쪽 위·아래 두 칸
 *   - 4 개 이상 : 2 × 2 그리드 (앞 4 개)
 */
@Composable
private fun CategoryThumbCollage(category: Category, modifier: Modifier = Modifier) {
    val sample = category.targets.take(4)
    val context = LocalContext.current

    if (sample.isEmpty()) {
        Spacer(modifier = modifier)
        return
    }

    when (sample.size) {
        1 -> ThumbTile(sample[0], context, modifier)

        2 -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThumbTile(sample[0], context, Modifier.weight(1f).fillMaxHeight())
            ThumbTile(sample[1], context, Modifier.weight(1f).fillMaxHeight())
        }

        3 -> Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThumbTile(sample[0], context, Modifier.weight(1f).fillMaxHeight())
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ThumbTile(sample[1], context, Modifier.weight(1f).fillMaxWidth())
                ThumbTile(sample[2], context, Modifier.weight(1f).fillMaxWidth())
            }
        }

        else -> Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ThumbTile(sample[0], context, Modifier.weight(1f).fillMaxHeight())
                ThumbTile(sample[1], context, Modifier.weight(1f).fillMaxHeight())
            }
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ThumbTile(sample[2], context, Modifier.weight(1f).fillMaxHeight())
                ThumbTile(sample[3], context, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

/**
 * 단일 썸네일 칸. 외곽선 PNG 는 알파 배경이라 흰 배경 박스 안에 띄워야 카드의 컬러 위에서 보임.
 */
@Composable
private fun ThumbTile(target: ColoringTarget, context: Context, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/${target.thumbnail}")
                .build(),
            contentDescription = target.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp),
        )
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
                        name = "캐치 티니핑",
                        themeColor = "#FF5A8C",
                        thumbnail = "thumbs/tinyping.png",
                        targets = (1..21).map {
                            ColoringTarget(
                                id = "tp_$it",
                                name = "핑이$it",
                                outline = "outlines/tp_$it.png",
                                thumbnail = "thumbs/tp_$it.png",
                                addedAt = 0,
                            )
                        },
                    ),
                    progress = CategoryProgress(total = 21, started = 3),
                ),
                CategoryListItem(
                    category = Category(
                        id = "animals",
                        name = "동물 친구들",
                        themeColor = "#A5D6A7",
                        thumbnail = "thumbs/animals.png",
                        targets = (1..3).map {
                            ColoringTarget(
                                id = "an_$it",
                                name = "동물$it",
                                outline = "outlines/an_$it.png",
                                thumbnail = "thumbs/an_$it.png",
                                addedAt = 0,
                            )
                        },
                    ),
                    progress = CategoryProgress(total = 3, started = 0),
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
