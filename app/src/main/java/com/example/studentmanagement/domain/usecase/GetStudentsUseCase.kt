package com.example.studentmanagement.domain.usecase

import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.model.Student

class GetStudentsUseCase(private val repository: StudentRepository) {
    operator fun invoke() = repository.observeStudents()
}
