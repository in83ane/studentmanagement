package com.example.studentmanagement.domain.repository

import com.example.studentmanagement.domain.model.Student
import com.example.studentmanagement.domain.model.SyncResult
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    fun observeStudents(): Flow<List<Student>>
    suspend fun refresh(): Boolean
    suspend fun addStudent(student: Student)
    suspend fun syncPending(): SyncResult
    suspend fun updateStudent(student: Student)
    suspend fun deleteStudent(student: Student)
    suspend fun getStudentById(id: Int): Student?
}
