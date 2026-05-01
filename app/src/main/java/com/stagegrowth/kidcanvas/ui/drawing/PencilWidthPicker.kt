package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stagegrowth.kidcanvas.ui.component.icon.PencilWidthIcon

/** 굵기 단계 (8 / 16 / 28 dp). 도메인 Stroke.widthDp 와 동일한 값. */
private val WidthOptions: List<Float> = listOf(8f, 16f, 28f)

/**
 * 펜 굵기 선택. 박스 3개 (얇/중/굵) 세로 정렬.
 *
 * 각 박스는 ui.component.icon.PencilWidthIcon 으로 그려짐.
 *   - 굵기에 따라 연필 폭과 끝 점 크기가 달라져 5살이 직관적으로 굵기 인지
 *   - 색은 currentColor — "이 굵기 + 이 색으로 그려진다" 미리보기
 *   - 선택된 박스는 노란 배경 + 노란 외곽선
 *
 * Spring 비유:
 *   - alpha 는 view 의 transparency. enabled=false 면 흐릿하게 + 클릭 무시.
 */
@Composable
fun PencilWidthPicker(
    currentWidthDp: Float,
    onWidthSelected: (Float) -> Unit,
    currentColor: Long,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier = modifier.alpha(if (enabled) 1f else 0.4f),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WidthOptions.forEach { w ->
            PencilWidthIcon(
                strokeWidth = w.dp,
                currentColor = Color(currentColor),
                isSelected = w == currentWidthDp,
                onClick = { if (enabled) onWidthSelected(w) },
            )
        }
    }
}
