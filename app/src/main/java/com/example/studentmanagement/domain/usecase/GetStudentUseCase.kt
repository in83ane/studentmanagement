package com.example.studentmanagement.domain.usecase

import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.model.Student

class GetStudentUseCase(private val repository: StudentRepository) {
    suspend operator fun invoke(id: Int) = repository.getStudentById(id)
}
