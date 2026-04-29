package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 학용품 분홍 지우개 아이콘.
 *   - 분홍 직사각형 본체 (둥근 모서리 4dp)
 *   - 위쪽 약 1/3 보라 띠 — 지우개를 둘러싼 보호 종이 표현
 *   - 진한 분홍 외곽선
 *   - -12° 살짝 기울임 → 정적 도형이 아닌 "쓰는 도구" 느낌
 *
 * 호출처에서 가로로 약간 긴 size(예: 40 x 24 dp)를 주면 학교 지우개 비율과 비슷.
 *
 * Spring 비유: Modifier.rotate 는 view 의 transform: rotate(...) CSS 와 동일 — 레이아웃은 그대로,
 * 그리기만 회전.
 */
@Composable
fun EraserIcon(modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier = modifier
            .rotate(-12f)
            .clip(shape)
            .background(Color(0xFFFFB6C1))
            .border(
                width = 1.5.dp,
                color = Color(0xFFD81B60),
                shape = shape,
            ),
    ) {
        // 위쪽 보호 종이 띠 (본체 clip 안에서 그려져 둥근 모서리를 따라감)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.32f)
                .background(Color(0xFF7986CB)),
        )
    }
}
