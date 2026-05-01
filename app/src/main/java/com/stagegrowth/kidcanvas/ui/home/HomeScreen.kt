package com.stagegrowth.kidcanvas.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 5살용 홈 화면.
 *   - 베이지 배경 위에 일러스트 + 타이틀 + 시작 버튼이 직접 배치 (분홍 박스 제거).
 *   - 진입 시: 일러스트 fade+down 등장 → 살짝 늦게 시작 버튼 등장.
 *   - 시작 버튼은 hover 처럼 위아래 부드럽게 떠다님 → "눌러봐" 신호.
 *   - 우상단 갤러리/설정 placeholder (M8 이후 채울 자리).
 *
 * Spring 비유:
 *   - rememberInfiniteTransition + animateFloat = CSS @keyframes 무한 반복.
 *   - animateFloatAsState = 단발성 transition. 값 바뀌면 부드럽게.
 */
@Composable
fun HomeScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = Color(0xFFFFF5E6)

    // 진입 등장 트리거 — 한 번만 true 로 전환되면 됨.
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }

    val illustrationAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "illustrationAlpha",
    )
    val illustrationOffsetY by animateDpAsState(
        targetValue = if (entered) 0.dp else (-20).dp,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "illustrationOffset",
    )
    val buttonAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(300, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "buttonAlpha",
    )

    // 시작 버튼이 위아래 부드럽게 떠다니는 hover 애니메이션 (-8dp ~ +8dp, 1.5s).
    val infinite = rememberInfiniteTransition(label = "hover")
    val hoverYpx by infinite.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "hoverY",
    )
    val hoverY = hoverYpx.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(bg),
    ) {
        // 짧은 변의 25% — 가로 모드든 세로 모드든 일러스트가 적당한 크기.
        val shortSide = if (maxWidth < maxHeight) maxWidth else maxHeight
        val illustrationSize = shortSide * 0.25f

        TopRightActions(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            HomeIllustration(
                bg = bg,
                modifier = Modifier
                    .size(illustrationSize)
                    .alpha(illustrationAlpha)
                    .offset(y = illustrationOffsetY),
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "오늘은 뭐 그릴까?",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF5D4037),
                modifier = Modifier.alpha(illustrationAlpha),
            )

            Spacer(modifier = Modifier.height(28.dp))

            StartButton(
                onClick = onStart,
                modifier = Modifier
                    .alpha(buttonAlpha)
                    .offset(y = hoverY),
            )
        }
    }
}

/**
 * Compose Canvas 로 그린 색칠 팔레트 일러스트.
 * 시스템 이모지(🎨)는 OS 마다 다르게 보여서 직접 그림.
 *   - 흰 둥근 팔레트 본체
 *   - 엄지 구멍 (배경색과 동일하게 뚫린 듯)
 *   - 5 색 점 (빨강·노랑·파랑·초록·분홍) — 위 3 / 아래 2 배열, 앱 다른 곳과 동일 톤.
 */
@Composable
private fun HomeIllustration(bg: Color, modifier: Modifier = Modifier) {
    val paintColors = remember {
        listOf(
            Color(0xFFFF5252), // 빨강
            Color(0xFFFFE74C), // 노랑
            Color(0xFF4D96FF), // 파랑
            Color(0xFF6BCB77), // 초록
            Color(0xFFE91E63), // 분홍
        )
    }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 팔레트 본체 (둥근 사각형). 좌우 padding 두고 중심에 큼지막하게.
        val paletteLeft = w * 0.08f
        val paletteTop = h * 0.22f
        val paletteW = w * 0.84f
        val paletteH = h * 0.6f
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(paletteLeft, paletteTop),
            size = Size(paletteW, paletteH),
            cornerRadius = CornerRadius(w * 0.12f),
        )

        // 엄지 구멍 — 배경색으로 채우면 뚫린 것처럼 보임.
        drawCircle(
            color = bg,
            radius = w * 0.07f,
            center = Offset(paletteLeft + paletteW * 0.15f, paletteTop + paletteH * 0.55f),
        )

        // 5색 점 — 위 3 (빨강/노랑/파랑) / 아래 2 (초록/분홍, 가운데 정렬).
        val dotR = w * 0.06f
        val dotsLeft = paletteLeft + paletteW * 0.36f
        val dotsRight = paletteLeft + paletteW * 0.92f
        val xsTop = listOf(
            dotsLeft,
            dotsLeft + (dotsRight - dotsLeft) * 0.5f,
            dotsRight,
        )
        val yTop = paletteTop + paletteH * 0.32f
        val yBottom = paletteTop + paletteH * 0.7f
        for (i in 0..2) {
            drawCircle(paintColors[i], radius = dotR, center = Offset(xsTop[i], yTop))
        }
        // 아래 2개는 위 3개의 가운데 두 자리 사이 (1/4, 3/4 간격) 에 배치.
        val xsBottom = listOf(
            dotsLeft + (dotsRight - dotsLeft) * 0.25f,
            dotsLeft + (dotsRight - dotsLeft) * 0.75f,
        )
        for (i in 0..1) {
            drawCircle(paintColors[i + 3], radius = dotR, center = Offset(xsBottom[i], yBottom))
        }
    }
}

/**
 * 둥근 시작 버튼. Material3 Button 으로 단순화 — Korean 글리프 클립·합성 Bold 등 텍스트 렌더 이슈를
 * Compose Material 기본값에 위임. 우리는 모양·색·padding 만 지정.
 *
 * lineHeight 는 fontSize 보다 충분히 크게 잡아야 한국어 받침/내림 글리프가 잘리지 않음 (22sp → 36sp).
 */
@Composable
private fun StartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE91E63),
            contentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        contentPadding = PaddingValues(horizontal = 40.dp, vertical = 14.dp),
    ) {
        Text(
            text = "시작하기",
            fontSize = 22.sp,
            lineHeight = 36.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** 우상단 placeholder 아이콘 (갤러리·설정). 부드러운 둥근 배경. 56dp 터치 타깃 보장. */
@Composable
private fun TopRightActions(modifier: Modifier = Modifier) {
    val tint = Color(0xFF8D6E63)
    val chipBg = Color(0xFFF5E6CC) // 베이지보다 살짝 진한 톤
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ActionChip(bg = chipBg) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = "갤러리(준비 중)",
                tint = tint,
                modifier = Modifier.size(28.dp),
            )
        }
        ActionChip(bg = chipBg) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "설정(준비 중)",
                tint = tint,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun ActionChip(bg: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 480)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(onStart = {})
}
