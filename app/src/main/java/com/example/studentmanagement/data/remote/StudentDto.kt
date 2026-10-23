package com.example.studentmanagement.data.remote

import com.example.studentmanagement.data.local.StudentEntity

data class StudentDto(val id: Int, val student_code: String, val name: String, val major: String, val client_id: String? = null) {
    fun toStudent() = StudentEntity(studentCode = student_code, name = name, major = major,
        serverId = id, syncStatus = "SYNCED", clientId = client_id ?: "server-$id")
}
data class StudentCreateRequest(val student_code: String, val name: String, val major: String, val client_id: String? = null)
fun StudentEntity.toRequest() = StudentCreateRequest(studentCode.trim(), name.trim(), major.trim(), clientId)
