package com.stagegrowth.kidcanvas.data.repository

import com.stagegrowth.kidcanvas.data.asset.AssetContentLoader
import com.stagegrowth.kidcanvas.data.local.DrawingStateDao
import com.stagegrowth.kidcanvas.data.local.DrawingStateEntity
import com.stagegrowth.kidcanvas.domain.model.Category
import com.stagegrowth.kidcanvas.domain.model.DrawingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 카테고리/캐릭터 메타는 assets/content.json (읽기 전용)에서,
 * 사용자 그림 상태는 Room (쓰기 가능)에서 가져온다.
 */
@Singleton
class ColoringRepositoryImpl @Inject constructor(
    private val dao: DrawingStateDao,
    private val loader: AssetContentLoader,
) : ColoringRepository {

    // 메타데이터는 앱 실행 중 변하지 않으므로 한 번만 읽어 흘려보냄.
    // (콘텐츠 갱신은 빌드 도구가 APK 새로 만들어야 반영됨 — 앱 재시작 필요)
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
}

private fun DrawingStateEntity.toDomain(): DrawingState = DrawingState(
    targetId = targetId,
    strokes = strokes,
    updatedAt = updatedAt,
)

private fun DrawingState.toEntity(): DrawingStateEntity = DrawingStateEntity(
    targetId = targetId,
    strokes = strokes,
    updatedAt = updatedAt,
)
