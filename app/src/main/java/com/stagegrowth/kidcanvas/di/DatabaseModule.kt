package com.stagegrowth.kidcanvas.di

import android.content.Context
import androidx.room.Room
import com.stagegrowth.kidcanvas.data.local.AppDatabase
import com.stagegrowth.kidcanvas.data.local.DrawingStateDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Room 데이터베이스 + Dao 를 싱글톤으로 제공.
 *
 * Spring 비유: @Configuration 클래스에서 DataSource·EntityManagerFactory·Repository Bean 정의.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            DATABASE_NAME,
        )
            // 가족 내부용이라 마이그레이션 누락 시 데이터 초기화로 단순화
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDrawingStateDao(database: AppDatabase): DrawingStateDao =
        database.drawingStateDao()

    private const val DATABASE_NAME = "kidcanvas.db"
}
