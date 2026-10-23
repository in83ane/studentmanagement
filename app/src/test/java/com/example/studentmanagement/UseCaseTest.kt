package com.example.studentmanagement

import com.example.studentmanagement.domain.model.*
import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class UseCaseTest {
    private class FakeRepository : StudentRepository {
        val rows = MutableStateFlow<List<Student>>(emptyList())
        var syncCalls = 0
        override fun observeStudents() = rows
        override suspend fun refresh() = true
        override suspend fun addStudent(student: Student) { rows.value = rows.value + student }
        override suspend fun syncPending(): SyncResult { syncCalls++; return SyncResult(2, 1, 1) }
        override suspend fun updateStudent(student: Student) { rows.value = rows.value.map { if (it.id == student.id) student else it } }
        override suspend fun deleteStudent(student: Student) { rows.value = rows.value.filterNot { it.id == student.id } }
        override suspend fun getStudentById(id: Int) = rows.value.find { it.id == id }
    }
    @Test fun getStudentsReflectsRepositoryUpdates() = runTest {
        val repo = FakeRepository()
        val useCase = GetStudentsUseCase(repo)
        assertTrue(useCase().first().isEmpty())
        val student = Student(1, "66006001", "ronnakorn muangkan", "CS")
        repo.rows.value = listOf(student)
        assertEquals(listOf(student), useCase().first())
    }
    @Test fun syncReturnsCountsAndCallsRepositoryOnce() = runTest {
        val repo = FakeRepository()
        assertEquals(SyncResult(2, 1, 1), SyncStudentsUseCase(repo)())
        assertEquals(1, repo.syncCalls)
    }
    @Test fun addTrimsInput() = runTest {
        val repo = FakeRepository()
        AddStudentUseCase(repo)(Student(studentCode = " 66006001 ", name = " ronnakorn muangkan ", major = " CS "))
        assertEquals("66006001", repo.rows.value.single().studentCode)
        assertEquals("ronnakorn muangkan", repo.rows.value.single().name)
    }
    @Test fun blankInputDoesNotWriteRepository() = runTest {
        val repo = FakeRepository()
        try { AddStudentUseCase(repo)(Student(studentCode = " ", name = "Name", major = "CS")); fail("Expected validation") }
        catch (_: IllegalArgumentException) { }
        assertTrue(repo.rows.value.isEmpty())
    }
    @Test fun oversizedInputDoesNotWriteRepository() = runTest {
        val repo = FakeRepository()
        try { AddStudentUseCase(repo)(Student(studentCode = "X".repeat(21), name = "Name", major = "CS")); fail("Expected validation") }
        catch (_: IllegalArgumentException) { }
        assertTrue(repo.rows.value.isEmpty())
    }
    @Test fun getMissingStudentReturnsNull() = runTest {
        assertNull(GetStudentUseCase(FakeRepository())(999))
    }
}
