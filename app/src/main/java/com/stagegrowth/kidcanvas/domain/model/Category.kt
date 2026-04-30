package com.stagegrowth.kidcanvas.domain.model

import kotlinx.serialization.Serializable

/**
 * 카테고리(예: "하츄핑 친구들"). 다수의 ColoringTarget 을 묶는 단위.
 * themeColor 는 "#RRGGBB" 또는 "#AARRGGBB" 형태 문자열.
 * thumbnail 은 카테고리 대표 이미지 경로 — 빌드 도구가 생성 안 했으면 null.
 *   (현재 UI 는 카드 안에 캐릭터 썸네일 콜라주를 쓰므로 이 값을 직접 참조하지 않음.)
 */
@Serializable
data class Category(
    val id: String,
    val name: String,
    val themeColor: String,
    val thumbnail: String? = null,
    val targets: List<ColoringTarget>,
)
