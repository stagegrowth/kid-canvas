package com.stagegrowth.kidcanvas.ui.drawing

import androidx.compose.ui.graphics.ImageBitmap
import com.stagegrowth.kidcanvas.domain.model.NormalizedPoint
import com.stagegrowth.kidcanvas.domain.model.Stroke
import com.stagegrowth.kidcanvas.domain.model.Tool

/**
 * DrawingScreen 의 단일 UI 상태.
 *
 * - strokes / currentStroke: 완료된 획 + 진행 중인 획.
 * - currentColor / currentWidthDp / currentTool: 현재 도구 상태.
 * - activeMask: 진행 중인 stroke 의 영역 마스크 (있으면 그 영역 안에서만 그려진다).
 *               BRUSH 가 영역 안에서 시작했을 때 비동기 계산 후 채워짐.
 * - maskBySeed: 완료된 stroke 들이 사용한 마스크 캐시. seed 가 같으면 같은 비트맵 공유.
 *               render 시 stroke.seed → maskBySeed[seed] 로 마스크 조회.
 *
 * 기본 색은 새 색상 띠(DefaultStripColors) 의 빨강 (#FF5252) 와 일치.
 */
data class DrawingUiState(
    val targetId: String = "",
    val targetName: String = "",
    val outlinePath: String? = null,
    val strokes: List<Stroke> = emptyList(),
    val currentStroke: Stroke? = null,
    val currentColor: Long = 0xFFFF5252L, // 빨강 — DefaultStripColors[4]
    val currentWidthDp: Float = 16f,
    val currentTool: Tool = Tool.BRUSH,
    val activeMask: ImageBitmap? = null,
    val maskBySeed: Map<NormalizedPoint, ImageBitmap> = emptyMap(),
)
