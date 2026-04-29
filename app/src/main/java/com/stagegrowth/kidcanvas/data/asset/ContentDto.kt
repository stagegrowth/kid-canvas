package com.stagegrowth.kidcanvas.data.asset

import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.ColoringTarget
import kotlinx.serialization.Serializable

/**
 * content.json 직렬화 매핑.
 * 빌드 도구가 갱신하는 단일 메타데이터 파일이며 도메인 모델로 변환해 사용한다.
 */
@Serializable
data class ContentRoot(
    val version: Int = 1,
    val generatedAt: Long? = null,
    val categories: List<CategoryDto> = emptyList(),
)

@Serializable
data class CategoryDto(
    val id: String,
    val name: String,
    val themeColor: String,
    val thumbnail: String,
    val targets: List<ColoringTargetDto> = emptyList(),
) {
    fun toDomain(): Category = Category(
        id = id,
        name = name,
        themeColor = themeColor,
        thumbnail = thumbnail,
        targets = targets.map { it.toDomain() },
    )
}

@Serializable
data class ColoringTargetDto(
    val id: String,
    val name: String,
    val outline: String,
    val thumbnail: String,
    val addedAt: Long,
) {
    fun toDomain(): ColoringTarget = ColoringTarget(
        id = id,
        name = name,
        outline = outline,
        thumbnail = thumbnail,
        addedAt = addedAt,
    )
}
