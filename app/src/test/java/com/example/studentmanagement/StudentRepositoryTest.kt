package com.example.studentmanagement

import com.example.studentmanagement.data.local.StudentDao
import com.example.studentmanagement.data.remote.*
import com.example.studentmanagement.data.repository.StudentRepositoryImpl
import com.example.studentmanagement.data.repository.toDomain
import com.example.studentmanagement.data.local.StudentEntity as Student
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class StudentRepositoryTest {
    private class MemoryDao : StudentDao {
        private val state = MutableStateFlow<List<Student>>(emptyList())
        var rows: List<Student>
            get() = state.value
            set(value) { state.value = value }
        private var nextId = 100
        override fun observeStudents(): Flow<List<Student>> = state
        override suspend fun getAllStudents() = rows
        override suspend fun getPendingStudents() = rows.filter { it.syncStatus != "SYNCED" }
        override suspend fun getStudentById(id: Int) = rows.find { it.id == id }
        override suspend fun insertStudent(student: Student) { rows = rows + student.copy(id = if (student.id == 0) nextId++ else student.id) }
        override suspend fun updateStudent(student: Student) { rows = rows.map { if (it.id == student.id) student else it } }
        override suspend fun deleteStudent(student: Student) { rows = rows.filterNot { it.id == student.id } }
    }
    private class FakeApi : StudentApi {
        var failure: Exception? = null
        var calls = 0
        var loseResponse = false
        var rows = emptyList<StudentDto>()
        override suspend fun getStudents(): List<StudentDto> { failure?.let { throw it }; return rows }
        override suspend fun getStudent(id: Int) = rows.first { it.id == id }
        override suspend fun createStudent(student: StudentCreateRequest): StudentDto {
            calls++
            failure?.let { throw it }
            val record = rows.find { it.client_id == student.client_id } ?: StudentDto(rows.size + 1, student.student_code, student.name, student.major, student.client_id).also { rows = rows + it }
            if (loseResponse) { loseResponse = false; throw IOException("Response lost") }
            return record
        }
        override suspend fun updateStudent(id: Int, student: StudentCreateRequest): StudentDto {
            failure?.let { throw it }
            return StudentDto(id, student.student_code, student.name, student.major, student.client_id).also { updated -> rows = rows.map { if (it.id == id) updated else it } }
        }
        override suspend fun deleteStudent(id: Int) { failure?.let { throw it }; rows = rows.filterNot { it.id == id } }
    }
    private fun student() = Student(studentCode = "66006004", name = "Offline Test", major = "CS")
    @Test fun addOfflineWritesRoomWithoutNetworkAndFlowEmits() = runBlocking {
        val dao = MemoryDao(); val api = FakeApi(); val repo = StudentRepositoryImpl(dao, api)
        api.failure = IOException("offline")
        repo.addStudent(student().toDomain())
        assertEquals(0, api.calls)
        assertEquals("PENDING", repo.observeStudents().first().single().syncStatus)
        assertNull(dao.rows.single().serverId)
    }
    @Test fun syncRetainsLocalIdAndRepeatDoesNotPostAgain() = runBlocking {
        val dao = MemoryDao(); val api = FakeApi(); val repo = StudentRepositoryImpl(dao, api)
        repo.addStudent(student().toDomain()); val localId = dao.rows.single().id
        assertEquals(1, repo.syncPending().synced)
        assertEquals(localId, dao.rows.single().id)
        assertEquals(1, dao.rows.single().serverId)
        assertEquals("SYNCED", dao.rows.single().syncStatus)
        repo.syncPending(); assertEquals(1, api.calls)
    }
    @Test fun failureRetriesSameUuidAfterLostResponseWithoutDuplicate() = runBlocking {
        val dao = MemoryDao(); val api = FakeApi(); val repo = StudentRepositoryImpl(dao, api)
        repo.addStudent(student().toDomain()); val key = dao.rows.single().clientId
        api.loseResponse = true
        assertEquals(1, repo.syncPending().failed)
        assertEquals("FAILED", dao.rows.single().syncStatus)
        repo.refresh(); assertEquals(1, dao.rows.size)
        assertEquals("FAILED", dao.rows.single().syncStatus)
        assertEquals(1, repo.syncPending().synced)
        assertEquals(key, dao.rows.single().clientId)
        assertEquals(1, api.rows.size)
    }
    @Test fun refreshNeverDeletesPendingAndPreservesLocalIdentity() = runBlocking {
        val dao = MemoryDao(); val api = FakeApi(); val repo = StudentRepositoryImpl(dao, api)
        repo.addStudent(student().toDomain()); val id = dao.rows.single().id
        repo.refresh(); assertEquals(id, dao.rows.single().id)
        repo.syncPending(); repo.refresh(); assertEquals(id, dao.rows.single().id)
        api.rows = emptyList(); repo.refresh(); assertTrue(dao.rows.isEmpty())
    }
    @Test fun cancellationAnd401PropagateWithoutErasingQueue() = runBlocking {
        val dao = MemoryDao(); val api = FakeApi(); val repo = StudentRepositoryImpl(dao, api)
        repo.addStudent(student().toDomain())
        api.failure = CancellationException("cancel")
        try { repo.syncPending(); fail("Cancellation swallowed") } catch (_: CancellationException) { }
        assertEquals(1, dao.rows.size)
        api.failure = retrofit2.HttpException(retrofit2.Response.error<Any>(401, okhttp3.ResponseBody.create(null, "Unauthorized")))
        try { repo.syncPending(); fail("401 swallowed") } catch (e: retrofit2.HttpException) { assertEquals(401, e.code()) }
        assertEquals("FAILED", dao.rows.single().syncStatus)
    }
    @Test fun concurrentSyncIsSerialized() = runBlocking {
        val dao = MemoryDao(); val api = FakeApi(); val repo = StudentRepositoryImpl(dao, api)
        repo.addStudent(student().toDomain())
        coroutineScope { listOf(async { repo.syncPending() }, async { repo.syncPending() }).awaitAll() }
        assertEquals(1, api.calls)
    }
}
