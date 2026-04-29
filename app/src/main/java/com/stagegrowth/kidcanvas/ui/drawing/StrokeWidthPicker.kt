package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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

/** M3 명세대로 작은·중간·큰 세 단계 굵기. */
private val WidthOptions: List<Float> = listOf(8f, 16f, 28f)

/**
 * 굵기 선택. 미리보기로 실제 굵기의 검은 동그라미를 보여 5살이 직관적으로 인식.
 */
@Composable
fun StrokeWidthPicker(
    currentWidthDp: Float,
    onWidthSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WidthOptions.forEach { w ->
            WidthDot(
                widthDp = w,
                selected = w == currentWidthDp,
                onClick = { onWidthSelected(w) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RowScope.WidthDot(
    widthDp: Float,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(if (selected) Color(0xFFFFF59D) else Color(0xFFF5F5F5))
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Color.Black else Color(0x33000000),
                shape = shape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(widthDp.dp)
                .clip(CircleShape)
                .background(Color.Black),
        )
    }
}
