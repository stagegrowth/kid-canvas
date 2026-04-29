package com.stagegrowth.kidcanvas.data.repository

import com.stagegrowth.kidcanvas.domain.model.Category
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
}
