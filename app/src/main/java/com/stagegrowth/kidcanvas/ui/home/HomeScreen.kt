package com.stagegrowth.kidcanvas.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 5살용 홈 화면.
 *   - 화면의 ~70% 를 차지하는 거대한 "🎨 시작하기" 버튼 (탭 → 카테고리 화면)
 *   - 우상단에 갤러리/설정 placeholder 아이콘 (M8 이후 채울 자리)
 *   - 파스텔 배경 + 큰 폰트 + 동심 분위기
 */
@Composable
fun HomeScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6E0)),
    ) {
        // 우상단 placeholder 아이콘 (현재 동작 없음)
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = { /* M8: 갤러리 화면 */ }, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = "갤러리(준비 중)",
                    tint = Color(0xFF8D6E63),
                )
            }
            IconButton(onClick = { /* M8: 설정 화면 */ }, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "설정(준비 중)",
                    tint = Color(0xFF8D6E63),
                )
            }
        }

        // 본문 — 큰 시작 버튼
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "오늘은 뭐 그릴까?",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF5D4037),
            )
            Spacer(modifier = Modifier.height(16.dp))
            StartButton(onClick = onStart)
        }
    }
}

/** 화면 70% 비중을 갖는 둥근 카드형 시작 버튼. 큰 이모지 + 큰 글씨. */
@Composable
private fun StartButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .fillMaxHeight(0.7f)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFFFC1CC))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = "🎨", fontSize = 120.sp)
            Spacer(modifier = Modifier.height(8.dp))
            // 실제 5살이 못 읽어도 이모지로 충분히 의미 전달.
            // 글자는 약간 거들어 주는 정도.
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(horizontal = 32.dp, vertical = 12.dp),
            ) {
                Text(
                    text = "시작하기",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFD81B60),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 720, heightDp = 480)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(onStart = {})
}
