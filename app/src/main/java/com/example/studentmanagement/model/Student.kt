package com.example.studentmanagement.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// Data Layer: Local Room Database Entity
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentCode: String,
    val name: String,
    val major: String
)
