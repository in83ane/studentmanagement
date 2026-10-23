package com.example.studentmanagement.domain.usecase

import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.model.Student

class DeleteStudentUseCase(private val repository: StudentRepository) {
    suspend operator fun invoke(student: Student) = repository.deleteStudent(student)
}
