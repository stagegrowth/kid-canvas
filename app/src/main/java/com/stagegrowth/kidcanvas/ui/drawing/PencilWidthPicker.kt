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

/**
 * 굵기 단계와 도구 매핑:
 *   - 8dp  → 볼펜 (가는 푸른 본체 + 클립 + 가는 촉)
 *   - 16dp → 연필 (지우개·페룰·노란 본체·나무 끝·흑심)
 *   - 28dp → 붓   (갈색 손잡이 + 메탈 페룰 + 굵은 둥근 털)
 *
 * 도구 종류 자체를 형태로 차별화 — 5살이 한글 못 읽어도 "이건 볼펜·이건 연필·이건 붓"으로 인식.
 * 각 도구의 잉크/촉/털은 currentColor 로 칠해져 "이 도구로 그리면 이 색이 나옴" 직감.
 */
private val WidthOptions: List<Float> = listOf(8f, 16f, 28f)

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
            ToolOption(
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
private fun ColumnScope.ToolOption(
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
            .heightIn(min = 56.dp)
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
        ToolGlyph(
            widthDp = widthDp,
            tipColor = tipColor,
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp, horizontal = 4.dp),
        )
    }
}

@Composable
private fun ToolGlyph(widthDp: Float, tipColor: Color, modifier: Modifier) {
    when (widthDp) {
        8f -> BallpointPenGlyph(tipColor, modifier)
        16f -> PencilGlyph(tipColor, modifier)
        else -> PaintBrushGlyph(tipColor, modifier)
    }
}

/**
 * 볼펜: 가늘고 매끈한 본체(파랑) + 한쪽 옆에 클립 + 끝에 가는 촉.
 * 길고 날씬한 실루엣으로 "가는 도구" 인지.
 */
@Composable
private fun BallpointPenGlyph(tipColor: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val bodyW = w * 0.22f
        val outline = Color(0x66000000)
        val outlineW = 1.dp.toPx()

        // 본체 (파랑)
        drawRect(
            color = Color(0xFF1E88E5),
            topLeft = Offset(cx - bodyW / 2f, h * 0.05f),
            size = Size(bodyW, h * 0.62f),
        )
        drawRect(
            color = outline,
            topLeft = Offset(cx - bodyW / 2f, h * 0.05f),
            size = Size(bodyW, h * 0.62f),
            style = Stroke(width = outlineW),
        )
        // 클립 (검정 작은 띠) — 본체 왼쪽에 살짝 튀어나옴
        drawRect(
            color = Color(0xFF333333),
            topLeft = Offset(cx - bodyW / 2f - bodyW * 0.35f, h * 0.18f),
            size = Size(bodyW * 0.35f, h * 0.28f),
        )
        // 본체 → 촉 사이 테이퍼 (사다리꼴)
        val taperPath = Path().apply {
            moveTo(cx - bodyW / 2f, h * 0.67f)
            lineTo(cx + bodyW / 2f, h * 0.67f)
            lineTo(cx + bodyW * 0.20f, h * 0.85f)
            lineTo(cx - bodyW * 0.20f, h * 0.85f)
            close()
        }
        drawPath(taperPath, color = Color(0xFFB0B0B0))
        drawPath(taperPath, color = outline, style = Stroke(width = outlineW))
        // 촉 (선택 색)
        val tipPath = Path().apply {
            moveTo(cx - bodyW * 0.20f, h * 0.85f)
            lineTo(cx + bodyW * 0.20f, h * 0.85f)
            lineTo(cx, h * 0.97f)
            close()
        }
        drawPath(tipPath, color = tipColor)
        drawPath(tipPath, color = outline, style = Stroke(width = outlineW))
    }
}

/**
 * 연필: 지우개·페룰·본체·나무 끝·흑심 5단 구성.
 * 중간 굵기. 노란 본체로 한눈에 "연필" 인식.
 */
@Composable
private fun PencilGlyph(tipColor: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val bodyW = w * 0.45f
        val leadW = w * 0.30f
        val outline = Color(0x66000000)
        val outlineW = 1.dp.toPx()

        // 1) 지우개 (분홍, 위쪽)
        drawRect(
            color = Color(0xFFFF8A95),
            topLeft = Offset(cx - bodyW / 2f, h * 0.04f),
            size = Size(bodyW, h * 0.10f),
        )
        // 2) 페룰 (메탈)
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
        // 본체 외곽선 (지우개~본체 통째로)
        drawRect(
            color = outline,
            topLeft = Offset(cx - bodyW / 2f, h * 0.04f),
            size = Size(bodyW, h * 0.60f),
            style = Stroke(width = outlineW),
        )
        // 4) 나무 끝 (사다리꼴)
        val woodPath = Path().apply {
            moveTo(cx - bodyW / 2f, h * 0.64f)
            lineTo(cx + bodyW / 2f, h * 0.64f)
            lineTo(cx + leadW / 2f, h * 0.86f)
            lineTo(cx - leadW / 2f, h * 0.86f)
            close()
        }
        drawPath(woodPath, color = Color(0xFFD7B68E))
        drawPath(woodPath, color = outline, style = Stroke(width = outlineW))
        // 5) 흑심 — 선택 색
        val leadPath = Path().apply {
            moveTo(cx - leadW / 2f, h * 0.86f)
            lineTo(cx + leadW / 2f, h * 0.86f)
            lineTo(cx, h * 0.96f)
            close()
        }
        drawPath(leadPath, color = tipColor)
        drawPath(leadPath, color = outline, style = Stroke(width = outlineW))
    }
}

/**
 * 붓: 짧은 갈색 손잡이 + 넓은 메탈 페룰 + 굵은 둥근 털.
 * 통통한 실루엣으로 "굵은 도구" 인지.
 */
@Composable
private fun PaintBrushGlyph(tipColor: Color, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val handleW = w * 0.32f
        val ferruleW = w * 0.55f
        val bristleW = w * 0.62f
        val outline = Color(0x66000000)
        val outlineW = 1.dp.toPx()

        // 1) 손잡이 (갈색, 위)
        drawRect(
            color = Color(0xFF8B4513),
            topLeft = Offset(cx - handleW / 2f, h * 0.05f),
            size = Size(handleW, h * 0.40f),
        )
        drawRect(
            color = outline,
            topLeft = Offset(cx - handleW / 2f, h * 0.05f),
            size = Size(handleW, h * 0.40f),
            style = Stroke(width = outlineW),
        )
        // 2) 페룰 (메탈, 가운데)
        drawRect(
            color = Color(0xFFC0C0C0),
            topLeft = Offset(cx - ferruleW / 2f, h * 0.45f),
            size = Size(ferruleW, h * 0.13f),
        )
        drawRect(
            color = outline,
            topLeft = Offset(cx - ferruleW / 2f, h * 0.45f),
            size = Size(ferruleW, h * 0.13f),
            style = Stroke(width = outlineW),
        )
        // 3) 털 (선택 색) — 굵은 사각 + 둥근 끝
        drawRect(
            color = tipColor,
            topLeft = Offset(cx - bristleW / 2f, h * 0.58f),
            size = Size(bristleW, h * 0.28f),
        )
        // 끝의 둥근 부분
        drawCircle(
            color = tipColor,
            radius = bristleW * 0.40f,
            center = Offset(cx, h * 0.86f),
        )
        // 털 외곽선 (사각 + 원 통째로)
        drawRect(
            color = outline,
            topLeft = Offset(cx - bristleW / 2f, h * 0.58f),
            size = Size(bristleW, h * 0.28f),
            style = Stroke(width = outlineW),
        )
        drawCircle(
            color = outline,
            radius = bristleW * 0.40f,
            center = Offset(cx, h * 0.86f),
            style = Stroke(width = outlineW),
        )
    }
}
