package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

/**
 * 색칠 화면 우측 세로 색상 띠의 기본 색상 40 개.
 * 위→아래 흐름: 흰·노랑계 → 주황·빨강 → 분홍 → 보라·파랑 → 초록 → 갈색·회색·검정 → 파스텔.
 * 한 화면에 13~15 개 정도 보이고 LazyColumn 으로 스크롤.
 */
val DefaultStripColors: List<Long> = listOf(
    0xFFFFFFFFL, // 흰색
    0xFFFFF59DL, // 라이트 옐로우
    0xFFFFE74CL, // 노랑
    0xFFFFC107L, // 진한 노랑
    0xFFFFD8B5L, // 살구
    0xFFFFCC80L, // 옅은 주황
    0xFFFFB84DL, // 주황
    0xFFFF9800L, // 진한 주황
    0xFFFF7043L, // 코랄
    0xFFFF5252L, // 빨강 (default)
    0xFFD32F2FL, // 진한 빨강
    0xFFFF5A8CL, // 분홍
    0xFFE91E63L, // 핫핑크
    0xFFFFC0CBL, // 연분홍
    0xFFF8BBD0L, // 로즈
    0xFFCE93D8L, // 라일락
    0xFFB362FFL, // 보라
    0xFF7B1FA2L, // 진한 보라
    0xFF5C6BC0L, // 인디고
    0xFF4D96FFL, // 파랑
    0xFF1976D2L, // 진한 파랑
    0xFF00CED1L, // 하늘
    0xFF4DD0E1L, // 청록
    0xFF80CBC4L, // 민트
    0xFFA5D6A7L, // 옅은 초록
    0xFF6BCB77L, // 초록
    0xFF388E3CL, // 진한 초록
    0xFF9E9D24L, // 올리브
    0xFFF5DEB3L, // 베이지
    0xFFBDB76BL, // 카키
    0xFF8B4513L, // 갈색
    0xFF5D4037L, // 진한 갈색
    0xFFC68642L, // 카멜
    0xFFBDBDBDL, // 회색
    0xFF616161L, // 진한 회색
    0xFF2C2C2AL, // 검정
    0xFFB39DDBL, // 라벤더
    0xFFFFCBA4L, // 피치
    0xFFB2DFDBL, // 민트크림
    0xFFE6D5B8L, // 오트밀
)

/**
 * 캔버스 우측에 위치하는 색상 선택 영역.
 * 각 행 = [좌측 indicator 자리(▶) | 색상 가로 띠]. selected 행에만 ▶ 표시.
 *
 * Spring 비유:
 *   - LazyColumn 은 RecyclerView 처럼 보이는 만큼만 컴포지션 → 40 색이라도 부드러운 스크롤.
 *   - clickable(enabled = false) 는 이벤트 자체를 무시. 시각도 alpha 로 흐릿하게.
 *
 * enabled = false 인 경우(=지우개 모드) alpha 낮추고 스크롤·탭 모두 무시.
 */
@Composable
fun VerticalColorPickerArea(
    colors: List<Long>,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val selectedIndex = remember(colors, selectedColor) {
        colors.indexOf(selectedColor).coerceAtLeast(0)
    }
    val listState = rememberLazyListState()

    // 진입 시 / 외부에서 색이 바뀌어 보이지 않을 때 자동으로 selected 위치로 스크롤.
    LaunchedEffect(selectedIndex) {
        val visible = listState.layoutInfo.visibleItemsInfo
        val isVisible = visible.any { it.index == selectedIndex }
        if (!isVisible) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Box(modifier = modifier.alpha(if (enabled) 1f else 0.4f)) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = enabled,
        ) {
            items(
                count = colors.size,
                key = { idx -> colors[idx] },
            ) { idx ->
                ColorRow(
                    color = Color(colors[idx]),
                    selected = idx == selectedIndex,
                    enabled = enabled,
                    onClick = { onColorSelected(colors[idx]) },
                )
            }
        }
    }
}

@Composable
private fun ColorRow(
    color: Color,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 좌측 indicator slot — selected 면 ▶ 그림.
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            if (selected) {
                TriangleRight(
                    color = Color(0xFF333333),
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        // 색상 가로 띠
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .background(color),
        )
    }
}

@Composable
private fun TriangleRight(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, size.height / 2f)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path = path, color = color)
    }
}
