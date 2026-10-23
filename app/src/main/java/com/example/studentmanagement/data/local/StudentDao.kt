package com.example.studentmanagement.data.local

import androidx.room.*
import com.example.studentmanagement.data.local.StudentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name")
    fun observeStudents(): Flow<List<StudentEntity>>
    @Query("SELECT * FROM students ORDER BY name")
    suspend fun getAllStudents(): List<StudentEntity>
    @Query("SELECT * FROM students WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY id")
    suspend fun getPendingStudents(): List<StudentEntity>
    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Int): StudentEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertStudent(student: StudentEntity)
    @Update suspend fun updateStudent(student: StudentEntity)
    @Delete suspend fun deleteStudent(student: StudentEntity)

    // Merge by server identity; never clear pending/failed local work during refresh.
    @Transaction
    suspend fun mergeRemote(remote: List<StudentEntity>) {
        val local = getAllStudents()
        for (item in remote) {
            val existing = local.firstOrNull { it.serverId == item.serverId || it.clientId == item.clientId }
            if (existing == null) insertStudent(item.copy(id = 0))
            else if (existing.syncStatus == "SYNCED") updateStudent(item.copy(id = existing.id, clientId = existing.clientId))
        }
        val serverIds = remote.map { it.serverId }.toSet()
        local.filter { it.syncStatus == "SYNCED" && it.serverId !in serverIds }.forEach { deleteStudent(it) }
    }
}
