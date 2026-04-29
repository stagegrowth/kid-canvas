package com.stagegrowth.kidcanvas.domain.model

import kotlinx.serialization.Serializable

/** 드로잉 도구 종류. M3 PoC 단계에서는 BRUSH만 사용. */
@Serializable
enum class Tool { BRUSH, ERASER }

/**
 * 한 번의 손가락 드래그(획)에 해당하는 데이터.
 * - color: ARGB 값을 Long으로 (Compose Color.value 가 ULong 이라 Long 으로 보관)
 * - widthDp: dp 단위 굵기 (px 변환은 화면에서)
 * - points: 정규화 좌표 시퀀스
 */
@Serializable
data class Stroke(
    val color: Long,
    val widthDp: Float,
    val tool: Tool,
    val points: List<NormalizedPoint>,
)
