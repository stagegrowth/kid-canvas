package com.stagegrowth.kidcanvas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * 앱 단일 Room 데이터베이스.
 * 마이그레이션 전략은 외부 배포가 없으므로 단순화: 스키마 변경 시 fallbackToDestructiveMigration().
 */
@Database(
    entities = [DrawingStateEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun drawingStateDao(): DrawingStateDao
}
