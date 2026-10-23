package com.example.studentmanagement

import com.example.studentmanagement.domain.model.*
import com.example.studentmanagement.domain.repository.StudentRepository
import com.example.studentmanagement.domain.usecase.*
import com.example.studentmanagement.presentation.viewmodel.StudentViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudentViewModelTest {
    private class FakeRepository : StudentRepository {
        val rows = MutableStateFlow(listOf(Student(1, "66006001", "ronnakorn muangkan", "CS"), Student(2, "66006002", "Other", "IT")))
        override fun observeStudents() = rows
        override suspend fun refresh() = true
        override suspend fun addStudent(student: Student) { rows.value += student }
        override suspend fun syncPending() = SyncResult(1, 0)
        override suspend fun updateStudent(student: Student) = Unit
        override suspend fun deleteStudent(student: Student) = Unit
        override suspend fun getStudentById(id: Int) = rows.value.find { it.id == id }
    }
    @Test fun stateReflectsUseCaseAndSearchThenSyncResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var vm: StudentViewModel? = null
        try {
            val repo = FakeRepository()
            val model = StudentViewModel(GetStudentsUseCase(repo), SyncStudentsUseCase(repo), RefreshStudentsUseCase(repo), AddStudentUseCase(repo), UpdateStudentUseCase(repo), DeleteStudentUseCase(repo), GetStudentUseCase(repo))
            vm = model
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.students.collect() }
            runCurrent()
            assertEquals(2, model.students.value.size)
            assertTrue(model.isOnline)
            model.updateSearchQuery("ronnakorn")
            runCurrent()
            assertEquals("66006001", model.students.value.single().studentCode)
            model.syncStudents()
            runCurrent()
            assertEquals("Sync สำเร็จ 1 รายการ / FAILED 0 รายการ", model.statusMessage)
            assertNull(model.errorMessage)
            assertFalse(model.isLoading)
        } finally {
            vm?.viewModelScope?.cancel()
            runCurrent()
            Dispatchers.resetMain()
        }
    }
}
