package com.example.studentmanagement.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studentmanagement.model.Student

// Data Layer: Room Data Access Object (DAO) for local DB operations
@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    suspend fun getAllStudents(): List<Student>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Int): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)
}
