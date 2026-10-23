package com.example.studentmanagement.di

import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Provides fun getStudentsUseCase(repository: StudentRepository) = GetStudentsUseCase(repository)
    @Provides fun syncStudentsUseCase(repository: StudentRepository) = SyncStudentsUseCase(repository)
    @Provides fun refreshStudentsUseCase(repository: StudentRepository) = RefreshStudentsUseCase(repository)
    @Provides fun getStudentUseCase(repository: StudentRepository) = GetStudentUseCase(repository)
    @Provides fun updateStudentUseCase(repository: StudentRepository) = UpdateStudentUseCase(repository)
    @Provides fun deleteStudentUseCase(repository: StudentRepository) = DeleteStudentUseCase(repository)
    @Provides fun addStudentUseCase(repository: StudentRepository) = AddStudentUseCase(repository)
}
