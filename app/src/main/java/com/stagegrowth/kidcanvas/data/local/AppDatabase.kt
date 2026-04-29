package com.stagegrowth.kidcanvas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * 앱 단일 Room 데이터베이스.
 * 마이그레이션 전략은 외부 배포가 없으므로 단순화: 스키마 변경 시 fallbackToDestructiveMigration().
 *
 * Stroke 직렬화는 Repository 가 담당하므로 TypeConverter 는 사용하지 않는다.
 */
@Database(
    entities = [DrawingStateEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawingStateDao(): DrawingStateDao
}
