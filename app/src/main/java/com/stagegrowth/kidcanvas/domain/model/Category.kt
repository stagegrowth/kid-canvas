package com.stagegrowth.kidcanvas.domain.model

import kotlinx.serialization.Serializable

/**
 * 카테고리(예: "하츄핑 친구들"). 다수의 ColoringTarget 을 묶는 단위.
 * themeColor 는 "#RRGGBB" 또는 "#AARRGGBB" 형태 문자열.
 */
@Serializable
data class Category(
    val id: String,
    val name: String,
    val themeColor: String,
    val thumbnail: String,
    val targets: List<ColoringTarget>,
)
