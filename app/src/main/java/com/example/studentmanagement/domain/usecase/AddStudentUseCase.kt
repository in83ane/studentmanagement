package com.example.studentmanagement.domain.usecase

import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.model.Student

class AddStudentUseCase(private val repository: StudentRepository) {
    suspend operator fun invoke(student: Student) {
        require(student.studentCode.isNotBlank() && student.name.isNotBlank() && student.major.isNotBlank()) { "กรุณากรอกข้อมูลให้ครบ" }
        require(student.studentCode.trim().length <= 20 && student.name.trim().length <= 100 && student.major.trim().length <= 100) { "ข้อมูลยาวเกินกำหนด" }
        repository.addStudent(student.copy(studentCode = student.studentCode.trim(), name = student.name.trim(), major = student.major.trim()))
    }
}
