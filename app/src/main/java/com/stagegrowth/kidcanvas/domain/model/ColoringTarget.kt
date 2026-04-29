package com.stagegrowth.kidcanvas.domain.model

import kotlinx.serialization.Serializable

/**
 * 색칠 대상(개별 캐릭터). content.json 의 targets[] 항목.
 * outline / thumbnail 은 assets 내부 상대 경로 (예: "outlines/hachu_main.png").
 */
@Serializable
data class ColoringTarget(
    val id: String,
    val name: String,
    val outline: String,
    val thumbnail: String,
    val addedAt: Long,
)
