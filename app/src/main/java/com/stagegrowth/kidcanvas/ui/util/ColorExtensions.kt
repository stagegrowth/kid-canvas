package com.stagegrowth.kidcanvas.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * 색을 더 어둡게 만드는 헬퍼
 * 색연필/연필의 진한 띠, 외곽선에 사용
 *
 * 흰색 같은 매우 밝은 색은 회색으로 폴백 (안 보이는 문제 방지)
 */
fun darkerColor(color: Color, factor: Float = 0.65f): Color {
    // 매우 밝은 색은 회색으로 폴백
    if (color.luminance() > 0.85f) {
        return Color(0xFF888888)
    }
    return Color(
        red = (color.red * factor).coerceIn(0f, 1f),
        green = (color.green * factor).coerceIn(0f, 1f),
        blue = (color.blue * factor).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}
