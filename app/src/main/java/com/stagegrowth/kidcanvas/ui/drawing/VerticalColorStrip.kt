package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

/**
 * 색칠 화면 우측 세로 색상 띠 + 옆 ▶ 인디케이터의 기본 색상 13개.
 * 위→아래 순서로 화이트→난색→한색→검정 흐름.
 */
val DefaultStripColors: List<Long> = listOf(
    0xFFFFFFFFL, // 흰색
    0xFFFFE74CL, // 노랑
    0xFFFFD8B5L, // 살구
    0xFFFFB84DL, // 주황
    0xFFFF5252L, // 빨강 (default)
    0xFFFF5A8CL, // 분홍
    0xFFFFC0CBL, // 연분홍
    0xFFB362FFL, // 보라
    0xFF4D96FFL, // 파랑
    0xFF00CED1L, // 하늘
    0xFF6BCB77L, // 초록
    0xFF8B4513L, // 갈색
    0xFF2C2C2AL, // 검정
)

/**
 * 캔버스 우측에 위치하는 색상 선택 영역.
 * 좌측 띠(드래그/탭으로 색 선택) + 우측 ▶ 인디케이터로 분리.
 *
 * Spring 비유: pointerInput { awaitEachGesture { ... } } 는 down→drag→up 한 사이클의
 * 터치 이벤트만 받아 처리하는 핸들러 코루틴. Servlet Filter 안에서 InputStream 을
 * 직접 읽어 한 요청을 처리하는 것과 비슷한 위치.
 */
@Composable
fun VerticalColorPickerArea(
    colors: List<Long>,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 정확히 일치하는 색이 없으면 첫 번째 인덱스로
    val selectedIndex = colors.indexOf(selectedColor).let { if (it < 0) 0 else it }
    Row(modifier = modifier) {
        VerticalColorStrip(
            colors = colors,
            onColorSelected = onColorSelected,
            modifier = Modifier
                .weight(10f) // 색상 띠 ~10% (사용자 명세)
                .fillMaxHeight(),
        )
        ColorIndicator(
            selectedIndex = selectedIndex,
            totalCount = colors.size,
            modifier = Modifier
                .weight(3f) // 인디케이터 ~3%
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun VerticalColorStrip(
    colors: List<Long>,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 측정한 띠의 픽셀 높이를 기억 — y 좌표 → 색상 인덱스 매핑에 필요
    var stripHeightPx by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier
            .onSizeChanged { stripHeightPx = it.height.toFloat() }
            .pointerInput(colors) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    selectByY(down.position.y, stripHeightPx, colors, onColorSelected)
                    down.consume()

                    while (true) {
                        val event = awaitPointerEvent()
                        val pointer = event.changes.firstOrNull { it.id == down.id }
                        if (pointer == null || !pointer.pressed) break
                        // 드래그 중 매 이동마다 인덱스 → 색 매핑 → 자동 스냅
                        selectByY(pointer.position.y, stripHeightPx, colors, onColorSelected)
                        pointer.consume()
                    }
                }
            },
    ) {
        colors.forEach { c ->
            // 칩 사이 구분선 없이 같은 높이로 빽빽하게 쌓임
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(c)),
            )
        }
    }
}

/** y 좌표 → 색상 인덱스 매핑 (자동 스냅). 같은 색이면 StateFlow 가 자동으로 emit 생략. */
private fun selectByY(
    y: Float,
    totalHeightPx: Float,
    colors: List<Long>,
    onColorSelected: (Long) -> Unit,
) {
    if (totalHeightPx <= 0f || colors.isEmpty()) return
    val cy = y.coerceIn(0f, totalHeightPx - 1f)
    val index = (cy / totalHeightPx * colors.size).toInt().coerceIn(0, colors.size - 1)
    onColorSelected(colors[index])
}

/**
 * 선택된 색 옆에 ▶ 모양 표시.
 * animateDpAsState 로 색이 바뀔 때 부드럽게 위아래 이동.
 */
@Composable
private fun ColorIndicator(
    selectedIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val totalH = maxHeight
        val cellH = if (totalCount > 0) totalH / totalCount else totalH
        val triangleSize = 18.dp
        // 선택된 셀의 중심 = cellH * index + cellH/2. 거기서 삼각형 높이의 절반만큼 위로.
        val target = cellH * selectedIndex + cellH / 2 - triangleSize / 2
        val animY by animateDpAsState(targetValue = target, label = "colorIndicator")

        Box(
            modifier = Modifier
                .offset(y = animY)
                .fillMaxWidth()
                .height(triangleSize),
            contentAlignment = Alignment.Center,
        ) {
            TriangleRight(
                color = Color(0xFF333333),
                modifier = Modifier.size(triangleSize),
            )
        }
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
