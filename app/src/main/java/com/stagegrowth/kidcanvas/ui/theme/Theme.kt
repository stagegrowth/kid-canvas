package com.stagegrowth.kidcanvas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// 가족 내부용·5살 사용자라 다크 테마 없음. 밝은 파스텔 단일 테마.
private val LightColors = lightColorScheme(
    primary = Pink,
    onPrimary = White,
    secondary = Sky,
    onSecondary = White,
    tertiary = Mint,
    onTertiary = White,
    background = Cream,
    onBackground = Charcoal,
    surface = Cream,
    onSurface = Charcoal
)

@Composable
fun KidCanvasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
