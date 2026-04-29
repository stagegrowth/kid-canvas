package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * 굵기 단계와 도구 매핑:
 *   - 8dp  → 볼펜 (얇음)
 *   - 16dp → 연필 (중간)
 *   - 28dp → 붓   (굵음)
 *
 * 도구를 그림(아이콘)으로 보여 5살이 한글 못 읽어도 인식 가능.
 */
private val WidthOptions: List<Float> = listOf(8f, 16f, 28f)

@Composable
fun StrokeWidthPicker(
    currentWidthDp: Float,
    onWidthSelected: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WidthOptions.forEach { w ->
            WidthOption(
                widthDp = w,
                selected = w == currentWidthDp,
                onClick = { onWidthSelected(w) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun RowScope.WidthOption(
    widthDp: Float,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .heightIn(min = 48.dp)
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
        ToolGlyph(
            widthDp = widthDp,
            tint = Color(0xFF333333),
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

/** 굵기 값에 대응하는 도구 그림. */
@Composable
private fun ToolGlyph(widthDp: Float, tint: Color, modifier: Modifier) {
    when (widthDp) {
        8f -> BallpointGlyph(tint, modifier)
        16f -> PencilGlyph(tint, modifier)
        else -> BrushGlyph(tint, modifier)
    }
}

/** 볼펜: 가는 사선 + 끝에 작은 진한 점(촉). */
@Composable
private fun BallpointGlyph(tint: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = h * 0.16f
        val tipPoint = Offset(w * 0.20f, h * 0.78f)
        drawLine(
            color = tint,
            start = tipPoint,
            end = Offset(w * 0.80f, h * 0.22f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
        // 펜 촉 표현용 진한 작은 원
        drawCircle(
            color = tint,
            radius = strokeW * 0.7f,
            center = tipPoint,
        )
    }
}

/** 연필: 중간 굵기 사선 + 사각 끝(연필 끝 모양). */
@Composable
private fun PencilGlyph(tint: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = h * 0.30f
        drawLine(
            color = tint,
            start = Offset(w * 0.22f, h * 0.78f),
            end = Offset(w * 0.78f, h * 0.22f),
            strokeWidth = strokeW,
            cap = StrokeCap.Square,
        )
    }
}

/** 붓: 굵은 사선 + 둥근 끝(붓털 모양). */
@Composable
private fun BrushGlyph(tint: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val strokeW = h * 0.50f
        drawLine(
            color = tint,
            start = Offset(w * 0.25f, h * 0.75f),
            end = Offset(w * 0.75f, h * 0.25f),
            strokeWidth = strokeW,
            cap = StrokeCap.Round,
        )
    }
}
