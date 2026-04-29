package com.stagegrowth.kidcanvas.domain.model

import kotlinx.serialization.Serializable

/**
 * 정규화 좌표 (0~1 범위).
 * 화면 크기·회전과 무관하게 그림을 동일 비율로 복원하기 위해 저장 단위로 사용한다.
 */
@Serializable
data class NormalizedPoint(
    val x: Float,
    val y: Float,
)
