package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 굵기 단계 (8 / 16 / 28 dp). 도메인 Stroke.widthDp 와 동일한 값. */
private val WidthOptions: List<Float> = listOf(8f, 16f, 28f)

/**
 * 굵기 단계별 미리보기 동그라미 직경 (dp).
 * 실제 widthDp 는 화면 그림 굵기, 미리보기 직경은 시각 강조용으로 살짝 더 크게 잡음.
 */
private fun previewDiameterDp(widthDp: Float): Int = when (widthDp) {
    8f -> 6
    16f -> 14
    else -> 24
}

/**
 * 펜 굵기 선택. 박스 3개 (얇/중/굵) 세로 정렬.
 *
 * 각 박스 내부 중앙에 현재 선택 색(currentColor) 의 동그라미를 그려 굵기 차이를 시각화.
 * 5살이 한눈에 "이건 작은 점, 이건 큰 점, 그래서 큰 게 굵게 그려진다" 인지.
 *
 * Spring 비유: Box + Modifier.size + clip(CircleShape) + background 는
 * Compose 의 가장 단순한 그래픽 구성. CSS 의 div + width/height + border-radius + background 와 동일.
 */
@Composable
fun PencilWidthPicker(
    currentWidthDp: Float,
    onWidthSelected: (Float) -> Unit,
    currentColor: Long,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WidthOptions.forEach { w ->
            WidthOption(
                widthDp = w,
                selected = w == currentWidthDp,
                tipColor = Color(currentColor),
                onClick = { onWidthSelected(w) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ColumnScope.WidthOption(
    widthDp: Float,
    selected: Boolean,
    tipColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (selected) Color(0xFFFFF59D) else Color(0xFFF5F5F5)
    val base = previewDiameterDp(widthDp)
    // 선택 시 +2dp 살짝 커지는 강조 효과
    val diameter = (if (selected) base + 2 else base).dp

    Box(
        modifier = modifier
            .weight(1f)
            .heightIn(min = 56.dp) // 5살 터치 타깃
            .clip(shape)
            .background(bg)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color.Black else Color(0x33000000),
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // 현재 선택 색의 동그라미 — "이 굵기 + 이 색 으로 그려진다" 미리보기
        Box(
            modifier = Modifier
                .size(diameter)
                .clip(CircleShape)
                .background(tipColor)
                .border(
                    width = 1.dp,
                    color = Color(0x66000000), // 흰색·연한 색 선택해도 윤곽 유지
                    shape = CircleShape,
                ),
        )
    }
}
