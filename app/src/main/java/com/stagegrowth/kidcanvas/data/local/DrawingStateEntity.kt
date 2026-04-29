package com.stagegrowth.kidcanvas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.stagegrowth.kidcanvas.domain.model.Stroke

/**
 * 한 캐릭터(target)당 한 행. 자동 저장으로 재진입 시 그림을 복원할 때 사용.
 *
 * Spring 비유: JPA @Entity + @Id, strokes 는 List<Stroke> 를 JSON 으로 직렬화해 TEXT 컬럼 저장.
 */
@Entity(tableName = "drawing_states")
data class DrawingStateEntity(
    @PrimaryKey val targetId: String,
    val strokes: List<Stroke>,
    val updatedAt: Long,
)
