package com.stagegrowth.kidcanvas.di

import com.stagegrowth.kidcanvas.data.repository.ColoringRepository
import com.stagegrowth.kidcanvas.data.repository.ColoringRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 인터페이스 ColoringRepository 와 구현체 ColoringRepositoryImpl 의 바인딩.
 * @Binds 는 abstract function 만 허용하므로 클래스도 abstract.
 *
 * Spring 비유: @Service 인터페이스에 단일 구현체를 명시 등록.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindColoringRepository(impl: ColoringRepositoryImpl): ColoringRepository
}
