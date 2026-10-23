package com.example.studentmanagement.domain.model

data class Student(
    val id: Int = 0,
    val studentCode: String,
    val name: String,
    val major: String,
    val serverId: Int? = null,
    val syncStatus: String = "PENDING",
    val updatedAt: Long = 0,
    val clientId: String = ""
)
data class SyncResult(val synced: Int, val failed: Int, val retryable: Int = 0)
