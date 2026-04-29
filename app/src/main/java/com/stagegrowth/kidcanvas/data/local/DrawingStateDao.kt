package com.stagegrowth.kidcanvas.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * 그림 저장 상태에 대한 CRUD.
 * Flow 를 반환하는 쿼리는 데이터 변경 시 자동으로 새 값을 흘려보낸다 (= LiveQuery).
 *
 * Spring 비유: Spring Data JPA Repository + reactive 쿼리.
 */
@Dao
interface DrawingStateDao {

    @Query("SELECT * FROM drawing_states WHERE targetId = :targetId")
    fun getByTargetId(targetId: String): Flow<DrawingStateEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM drawing_states WHERE targetId = :targetId)")
    fun existsByTargetId(targetId: String): Flow<Boolean>

    @Upsert
    suspend fun upsert(entity: DrawingStateEntity)

    @Query("DELETE FROM drawing_states WHERE targetId = :targetId")
    suspend fun deleteByTargetId(targetId: String)
}
