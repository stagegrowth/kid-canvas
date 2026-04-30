package com.stagegrowth.kidcanvas.domain.model

import kotlinx.serialization.Serializable

/** 드로잉 도구 종류. */
@Serializable
enum class Tool { BRUSH, ERASER }

/**
 * 한 번의 손가락 드래그(획)에 해당하는 데이터.
 * - color: ARGB 값을 Long 으로 (Compose Color.value 가 ULong 이라 Long 으로 보관)
 * - widthDp: dp 단위 굵기 (px 변환은 화면에서)
 * - points: 정규화 좌표 시퀀스
 * - seed: 첫 터치 위치(정규화). 영역 인식 자유 드로잉의 영역 식별자로 사용.
 *         null = 영역 마스크 없음 (예: 지우개, 외곽선 위 시작, 구버전 데이터 호환).
 */
@Serializable
data class Stroke(
    val color: Long,
    val widthDp: Float,
    val tool: Tool,
    val points: List<NormalizedPoint>,
    val seed: NormalizedPoint? = null,
)
