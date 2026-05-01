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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** 굵기 단계 (8 / 16 / 28 dp). 도메인 Stroke.widthDp 와 동일한 값. */
private val WidthOptions: List<Float> = listOf(8f, 16f, 28f)

/**
 * 굵기 단계별 미리보기 가로 선분 두께 (dp). 5살이 한눈에 비교되도록 차이를 크게.
 *   - 얇은 4dp / 중간 12dp / 굵은 22dp
 */
private fun previewThicknessDp(widthDp: Float): Int = when (widthDp) {
    8f -> 4
    16f -> 12
    else -> 22
}

/** 미리보기 가로 선분 길이 (dp). 모든 박스 동일. */
private const val PreviewLineLengthDp = 64

/**
 * 펜 굵기 선택. 박스 3개 (얇/중/굵) 세로 정렬.
 *
 * 각 박스 내부 중앙에 "굵기 = 가로 선분 두께" 형태로 그려서 5살이 직관적으로 굵기 인지.
 * 색은 currentColor — "이 굵기 + 이 색 으로 그려진다" 미리보기.
 *
 * Spring 비유:
 *   - clip(RoundedCornerShape(percent = 50)) 은 capsule 모양 — CSS border-radius: 9999px 와 동일.
 *   - alpha 는 view 의 transparency. enabled=false 면 흐릿하게 + 클릭 무시.
 */
@Composable
fun PencilWidthPicker(
    currentWidthDp: Float,
    onWidthSelected: (Float) -> Unit,
    currentColor: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WidthOptions.forEach { w ->
            WidthOption(
                widthDp = w,
                selected = w == currentWidthDp,
                tipColor = Color(currentColor),
                enabled = enabled,
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
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerShape = RoundedCornerShape(12.dp)
    val containerBg = if (selected) Color(0xFFFFF59D) else Color(0xFFF5F5F5)
    val baseThickness = previewThicknessDp(widthDp)
    // 선택 시 +2dp 살짝 굵게 보이게 강조
    val thickness = (if (selected) baseThickness + 2 else baseThickness).dp

    Box(
        modifier = modifier
            .weight(1f)
            .heightIn(min = 56.dp) // 5살 터치 타깃
            .clip(containerShape)
            .background(containerBg)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color.Black else Color(0x33000000),
                shape = containerShape,
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // 가로 선분 = "이 굵기로 이 색으로 그려진다" 미리보기.
        // capsule 모양으로 양 끝이 둥글게 (StrokeCap.Round 와 동일 시각효과).
        Box(
            modifier = Modifier
                .size(width = PreviewLineLengthDp.dp, height = thickness)
                .clip(RoundedCornerShape(percent = 50))
                .background(tipColor)
                .border(
                    width = 1.dp,
                    color = Color(0x66000000), // 흰색·연한 색 선택 시에도 윤곽 유지
                    shape = RoundedCornerShape(percent = 50),
                ),
        )
    }
}
