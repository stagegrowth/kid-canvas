package com.stagegrowth.kidcanvas.data.repository

import com.stagegrowth.kidcanvas.data.asset.AssetContentLoader
import com.stagegrowth.kidcanvas.data.local.DrawingStateDao
import com.stagegrowth.kidcanvas.data.local.DrawingStateEntity
import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.CategoryProgress
import com.stagegrowth.kidcanvas.domain.model.DrawingState
import com.stagegrowth.kidcanvas.domain.model.Stroke
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 카테고리/캐릭터 메타는 assets/content.json (읽기 전용)에서,
 * 사용자 그림 상태는 Room (쓰기 가능)에서 가져온다.
 *
 * List(Stroke) 와 Room 의 String 컬럼 사이의 변환은 여기서 담당.
 * (Room TypeConverter 로 generic List 를 다루면 KSP 분석이 까다로워 Repository 에서 처리.)
 */
@Singleton
class ColoringRepositoryImpl @Inject constructor(
    private val dao: DrawingStateDao,
    private val loader: AssetContentLoader,
) : ColoringRepository {

    // 메타데이터는 앱 실행 중 변하지 않으므로 한 번만 읽어 흘려보냄.
    override fun getCategories(): Flow<List<Category>> = flow {
        emit(loader.loadCategories())
    }

    override fun getCategory(categoryId: String): Flow<Category?> =
        getCategories().map { list -> list.firstOrNull { it.id == categoryId } }

    override fun getDrawingState(targetId: String): Flow<DrawingState?> =
        dao.getByTargetId(targetId).map { entity -> entity?.toDomain() }

    override suspend fun saveDrawingState(state: DrawingState) {
        dao.upsert(state.toEntity())
    }

    override suspend fun resetDrawing(targetId: String) {
        dao.deleteByTargetId(targetId)
    }

    override fun hasDrawing(targetId: String): Flow<Boolean> =
        dao.existsByTargetId(targetId)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getCategoryProgress(categoryId: String): Flow<CategoryProgress> =
        getCategory(categoryId).flatMapLatest { category ->
            if (category == null || category.targets.isEmpty()) {
                flowOf(CategoryProgress.Empty.copy(total = category?.targets?.size ?: 0))
            } else {
                val ids = category.targets.map { it.id }
                dao.countStartedTargets(ids).map { started ->
                    CategoryProgress(total = category.targets.size, started = started)
                }
            }
        }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun startedTargetIds(categoryId: String): Flow<Set<String>> =
        getCategory(categoryId).flatMapLatest { category ->
            val ids = category?.targets?.map { it.id } ?: emptyList()
            if (ids.isEmpty()) {
                flowOf(emptySet())
            } else {
                dao.startedTargetIds(ids).map { it.toSet() }
            }
        }

    private fun DrawingStateEntity.toDomain(): DrawingState = DrawingState(
        targetId = targetId,
        strokes = decodeStrokes(strokesJson),
        updatedAt = updatedAt,
    )

    private fun DrawingState.toEntity(): DrawingStateEntity = DrawingStateEntity(
        targetId = targetId,
        strokesJson = encodeStrokes(strokes),
        updatedAt = updatedAt,
    )

    private fun encodeStrokes(strokes: List<Stroke>): String =
        json.encodeToString(strokeListSerializer, strokes)

    private fun decodeStrokes(value: String): List<Stroke> =
        if (value.isBlank()) emptyList()
        else json.decodeFromString(strokeListSerializer, value)

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        private val strokeListSerializer = ListSerializer(Stroke.serializer())
    }
}
