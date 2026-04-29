package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/** 8 / 16 / 28 dp 세 단계. 각 단계마다 연필 본체와 촉 굵기를 비례적으로 그림. */
private val WidthOptions: List<Float> = listOf(8f, 16f, 28f)

/**
 * 캔버스 우측 가장자리에 위치하는 연필 굵기 선택.
 * 위→아래: 얇은 / 보통 / 굵은 연필. 선택 시 진한 배경 + 굵은 테두리.
 *
 * 연필 촉(흑심) 색은 currentColor 로 칠해져 — 5살이 "이 도구로 그리면 이 색이 나옴" 즉시 인식.
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
            PencilOption(
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
private fun ColumnScope.PencilOption(
    widthDp: Float,
    selected: Boolean,
    tipColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    val bg = if (selected) Color(0xFFFFF59D) else Color(0xFFF5F5F5)
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
        PencilGlyph(
            widthDp = widthDp,
            tipColor = tipColor,
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 4.dp),
        )
    }
}

/**
 * 세로 연필 그림. 위에서 아래로:
 *   [지우개(분홍)] → [페룰(회색)] → [본체(노랑)] → [나무 끝(베이지 사다리꼴)] → [흑심(선택 색)]
 * 굵기 단계마다 본체/흑심 폭이 비례적으로 굵어짐.
 */
@Composable
private fun PencilGlyph(
    widthDp: Float,
    tipColor: Color,
    modifier: Modifier,
) {
    // 굵기에 따른 본체 / 흑심 너비 비율
    val (bodyRatio, leadRatio) = when (widthDp) {
        8f -> 0.30f to 0.16f
        16f -> 0.45f to 0.28f
        else -> 0.62f to 0.42f
    }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val bodyW = w * bodyRatio
        val leadW = w * leadRatio

        // 1) 지우개 (분홍, 위쪽)
        drawRect(
            color = Color(0xFFFF8A95),
            topLeft = Offset(cx - bodyW / 2f, h * 0.04f),
            size = Size(bodyW, h * 0.10f),
        )
        // 2) 페룰 (메탈, 회색 띠)
        drawRect(
            color = Color(0xFFB0B0B0),
            topLeft = Offset(cx - bodyW / 2f, h * 0.14f),
            size = Size(bodyW, h * 0.05f),
        )
        // 3) 본체 (노랑)
        drawRect(
            color = Color(0xFFFFD54F),
            topLeft = Offset(cx - bodyW / 2f, h * 0.19f),
            size = Size(bodyW, h * 0.45f),
        )
        // 4) 나무 끝 (베이지 사다리꼴 — 본체 너비 → 흑심 너비)
        val woodPath = Path().apply {
            moveTo(cx - bodyW / 2f, h * 0.64f)
            lineTo(cx + bodyW / 2f, h * 0.64f)
            lineTo(cx + leadW / 2f, h * 0.86f)
            lineTo(cx - leadW / 2f, h * 0.86f)
            close()
        }
        drawPath(woodPath, color = Color(0xFFD7B68E))
        // 5) 흑심 — 선택된 색
        val leadPath = Path().apply {
            moveTo(cx - leadW / 2f, h * 0.86f)
            lineTo(cx + leadW / 2f, h * 0.86f)
            lineTo(cx, h * 0.96f)
            close()
        }
        drawPath(leadPath, color = tipColor)
        // 흰색·연한 색이 배경에 묻히지 않도록 흑심에 가는 외곽선
        drawPath(
            leadPath,
            color = Color(0x66000000),
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}
