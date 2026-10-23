package com.example.studentmanagement.di

import com.example.studentmanagement.data.repository.StudentRepositoryImpl
import com.example.studentmanagement.domain.repository.StudentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun repository(implementation: StudentRepositoryImpl): StudentRepository
}
