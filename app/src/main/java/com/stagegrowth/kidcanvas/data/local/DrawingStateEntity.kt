package com.stagegrowth.kidcanvas.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 한 캐릭터(target) 당 한 행. 자동 저장으로 재진입 시 그림을 복원할 때 사용.
 *
 * strokesJson: List(Stroke) 를 kotlinx.serialization 으로 JSON 직렬화한 문자열.
 * Repository 에서 도메인 모델 DrawingState 와 양방향 변환한다.
 *
 * Spring 비유: JPA Entity. List 컬럼을 직접 쓰지 않고 직렬화된 TEXT 컬럼으로 저장.
 */
@Entity(tableName = "drawing_states")
data class DrawingStateEntity(
    @PrimaryKey val targetId: String,
    val strokesJson: String,
    val updatedAt: Long,
)
