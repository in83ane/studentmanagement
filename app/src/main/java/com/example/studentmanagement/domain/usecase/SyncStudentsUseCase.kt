package com.example.studentmanagement.domain.usecase

import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.model.Student

class SyncStudentsUseCase(private val repository: StudentRepository) {
    suspend operator fun invoke() = repository.syncPending()
}
