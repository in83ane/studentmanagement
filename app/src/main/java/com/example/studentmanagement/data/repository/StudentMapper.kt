package com.example.studentmanagement.data.repository

import com.example.studentmanagement.data.local.StudentEntity
import com.example.studentmanagement.domain.model.Student
import java.util.UUID

fun StudentEntity.toDomain() = Student(id, studentCode, name, major, serverId, syncStatus, updatedAt, clientId)
fun Student.toEntity() = StudentEntity(id, studentCode, name, major, serverId, syncStatus,
    updatedAt, clientId.ifBlank { UUID.randomUUID().toString() })
