package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 5살이 즐겨 쓰는 비비드 + 파스텔 톤 20색.
 * 흰색은 흰 배경 위에서 보이지 않으므로 제외, 검정·회색은 외곽선 채움용으로 포함.
 */
val PaletteColors: List<Long> = listOf(
    0xFFFF3B30L, 0xFFFF6B00L, 0xFFFFCC00L, 0xFFFFE74CL, 0xFFA8E063L,
    0xFF6BCB77L, 0xFF00BFA5L, 0xFF4FC3F7L, 0xFF2196F3L, 0xFF1A237EL,
    0xFF9C27B0L, 0xFFB362FFL, 0xFFFF80ABL, 0xFFFF1493L, 0xFFFFB6C1L,
    0xFFFFAB91L, 0xFF8B4513L, 0xFFFFC0CBL, 0xFF2C2C2AL, 0xFF9E9E9EL,
)

/**
 * 5칼럼 × 4행 색상 그리드. 동그라미 사이 공백 제거를 위해 둥근 사각 타일로 빽빽하게.
 * - 셀 너비: weight(1f) 로 행 균등 분할
 * - 셀 높이: 36dp
 * - 셀 사이 간격: 2dp
 * - 선택된 색은 검정 3dp 테두리
 */
@Composable
fun ColorPalette(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        PaletteColors.chunked(5).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                rowColors.forEach { c ->
                    ColorTile(
                        color = c,
                        selected = c == selectedColor,
                        onClick = { onColorSelected(c) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ColorTile(
    color: Long,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = Modifier
            .weight(1f)
            .height(36.dp)
            .clip(shape)
            .background(Color(color))
            .border(
                width = if (selected) 3.dp else 0.5.dp,
                color = if (selected) Color.Black else Color(0x33000000),
                shape = shape,
            )
            .clickable(onClick = onClick),
    )
}
