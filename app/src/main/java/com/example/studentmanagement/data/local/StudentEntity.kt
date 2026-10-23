package com.example.studentmanagement.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // local primary key
    val studentCode: String,
    val name: String,
    val major: String,
    val serverId: Int? = null,
    @ColumnInfo(defaultValue = "'SYNCED'") val syncStatus: String = "PENDING",
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "''") val clientId: String = UUID.randomUUID().toString()
)
