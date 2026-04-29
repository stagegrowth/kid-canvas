package com.stagegrowth.kidcanvas.domain.model

import kotlinx.serialization.Serializable

/**
 * 한 캐릭터(target)에 대한 그림 저장 상태.
 * Room에 영속화하여 앱을 다시 열었을 때 이어서 그릴 수 있게 한다.
 */
@Serializable
data class DrawingState(
    val targetId: String,
    val strokes: List<Stroke>,
    val updatedAt: Long,
)
