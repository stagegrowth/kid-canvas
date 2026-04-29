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

    /**
     * 주어진 targetIds 중 "획이 1개 이상 저장된" 행 수.
     * strokesJson 이 빈 문자열이거나 '[]'(빈 배열) 인 경우는 시작 안 한 것으로 본다.
     */
    @Query(
        """
        SELECT COUNT(*) FROM drawing_states
        WHERE targetId IN (:targetIds)
          AND strokesJson != ''
          AND strokesJson != '[]'
        """
    )
    fun countStartedTargets(targetIds: List<String>): Flow<Int>

    /** 주어진 targetIds 중 시작된 것들의 id 목록. PickerScreen 에서 배지 표시용. */
    @Query(
        """
        SELECT targetId FROM drawing_states
        WHERE targetId IN (:targetIds)
          AND strokesJson != ''
          AND strokesJson != '[]'
        """
    )
    fun startedTargetIds(targetIds: List<String>): Flow<List<String>>

    @Upsert
    suspend fun upsert(entity: DrawingStateEntity)

    @Query("DELETE FROM drawing_states WHERE targetId = :targetId")
    suspend fun deleteByTargetId(targetId: String)
}
