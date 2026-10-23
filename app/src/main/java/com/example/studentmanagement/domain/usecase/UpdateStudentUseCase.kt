package com.example.studentmanagement.domain.usecase

import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.model.Student

class UpdateStudentUseCase(private val repository: StudentRepository) {
    suspend operator fun invoke(student: Student) = repository.updateStudent(student)
}
