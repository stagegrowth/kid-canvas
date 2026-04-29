package com.stagegrowth.kidcanvas.data.repository

import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.CategoryProgress
import com.stagegrowth.kidcanvas.domain.model.DrawingState
import kotlinx.coroutines.flow.Flow

/**
 * UI 가 데이터 출처(Asset 메타데이터 + Room)를 모르고 사용할 수 있도록 묶어주는 단일 진입점.
 * Spring 비유: Service 계층 또는 Aggregate Repository.
 */
interface ColoringRepository {
    fun getCategories(): Flow<List<Category>>
    fun getCategory(categoryId: String): Flow<Category?>

    fun getDrawingState(targetId: String): Flow<DrawingState?>
    suspend fun saveDrawingState(state: DrawingState)
    suspend fun resetDrawing(targetId: String)
    fun hasDrawing(targetId: String): Flow<Boolean>

    /** 카테고리 진행 상황(total / started). 메타데이터(content.json) + Room 결합. */
    fun getCategoryProgress(categoryId: String): Flow<CategoryProgress>

    /** 카테고리 내 "시작된 targetId" 집합. PickerScreen 의 배지용. */
    fun startedTargetIds(categoryId: String): Flow<Set<String>>
}
