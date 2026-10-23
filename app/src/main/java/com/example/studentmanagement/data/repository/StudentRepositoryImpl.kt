package com.example.studentmanagement.data.repository

import com.example.studentmanagement.data.local.StudentDao
import com.example.studentmanagement.data.remote.StudentApi
import com.example.studentmanagement.data.remote.toRequest
import com.example.studentmanagement.domain.model.Student
import com.example.studentmanagement.domain.model.SyncResult
import com.example.studentmanagement.domain.repository.StudentRepository
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException

class StudentRepositoryImpl @Inject constructor(private val studentDao: StudentDao, private val studentApi: StudentApi) : StudentRepository {
    private val mutex = Mutex()
    override fun observeStudents() = studentDao.observeStudents().map { rows -> rows.map { it.toDomain() } }

    override suspend fun refresh(): Boolean = mutex.withLock {
        val remote = try { studentApi.getStudents() }
        catch (e: CancellationException) { throw e }
        catch (e: HttpException) { if (e.code() == 401 || e.code() == 403) throw e else return@withLock false }
        catch (e: Exception) { return@withLock false }
        studentDao.mergeRemote(remote.map { it.toStudent() })
        true
    }
    override suspend fun addStudent(student: Student) = mutex.withLock {
        studentDao.insertStudent(student.toEntity().copy(id = 0, serverId = null, syncStatus = "PENDING", updatedAt = System.currentTimeMillis()))
    }
    override suspend fun syncPending(): SyncResult = mutex.withLock {
        var synced = 0
        var failed = 0
        var retryable = 0
        for (student in studentDao.getPendingStudents()) {
            // Mark before sending: interrupted/ambiguous requests remain retryable with the same UUID.
            studentDao.updateStudent(student.copy(syncStatus = "FAILED"))
            try {
                val response = studentApi.createStudent(student.toRequest())
                studentDao.updateStudent(student.copy(serverId = response.id, syncStatus = "SYNCED"))
                synced++
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (e is HttpException && (e.code() == 401 || e.code() == 403)) throw e
                failed++
                if (e is IOException || (e is HttpException && (e.code() == 429 || e.code() >= 500))) retryable++
            }
        }
        SyncResult(synced, failed, retryable)
    }
    // Lab 4 queues additions only. Existing server rows still use online PUT/DELETE.
    override suspend fun updateStudent(student: Student) = mutex.withLock {
        val current = requireNotNull(studentDao.getStudentById(student.id))
        check(current.syncStatus != "FAILED") { "กรุณา Sync รายการ FAILED ก่อนแก้ไข" }
        if (current.serverId == null) studentDao.updateStudent(student.toEntity().copy(syncStatus = "PENDING", updatedAt = System.currentTimeMillis()))
        else {
            studentApi.updateStudent(current.serverId, student.toEntity().toRequest())
            studentDao.updateStudent(student.toEntity().copy(serverId = current.serverId, syncStatus = "SYNCED", updatedAt = System.currentTimeMillis()))
        }
    }
    override suspend fun deleteStudent(student: Student) = mutex.withLock {
        val current = studentDao.getStudentById(student.id) ?: return@withLock
        check(current.syncStatus != "FAILED") { "กรุณา Sync รายการ FAILED ก่อนลบ" }
        current.serverId?.let { studentApi.deleteStudent(it) }
        studentDao.deleteStudent(current)
    }
    override suspend fun getStudentById(id: Int) = studentDao.getStudentById(id)?.toDomain()
}
