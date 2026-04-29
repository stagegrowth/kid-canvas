package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * 외곽선 PNG 가 없을 때 보여주는 임시 placeholder.
 * Compose 만으로 큰 원(얼굴) + 작은 원 두 개(눈)를 그려 5살이 색칠해 볼 수 있는 단순 도안 흉내.
 *
 * 빌드 도구가 outlines/*.png 를 채우면 이 placeholder 자리에 AsyncImage 로 교체됨.
 */
@Composable
fun OutlinePlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val faceR = size.minDimension * 0.40f
        val eyeR = faceR * 0.12f
        val eyeOffsetX = faceR * 0.35f
        val eyeOffsetY = faceR * 0.15f
        val outlineWidth = size.minDimension * 0.012f

        val stroke = Stroke(
            width = outlineWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )

        // 얼굴 (외곽선만)
        drawCircle(
            color = Color.Black,
            radius = faceR,
            center = Offset(cx, cy),
            style = stroke,
        )
        // 두 눈 (채움)
        drawCircle(
            color = Color.Black,
            radius = eyeR,
            center = Offset(cx - eyeOffsetX, cy - eyeOffsetY),
        )
        drawCircle(
            color = Color.Black,
            radius = eyeR,
            center = Offset(cx + eyeOffsetX, cy - eyeOffsetY),
        )
    }
}
