package com.example.studentmanagement.presentation.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.studentmanagement.domain.model.Student
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val getStudents: GetStudentsUseCase,
    private val syncStudentsUseCase: SyncStudentsUseCase,
    private val refreshStudents: RefreshStudentsUseCase,
    private val add: AddStudentUseCase,
    private val update: UpdateStudentUseCase,
    private val delete: DeleteStudentUseCase,
    private val getOne: GetStudentUseCase
) : ViewModel() {
    private val query = MutableStateFlow("")
    val students = combine(getStudents(), query) { rows, text ->
        rows.filter { it.studentCode.contains(text.trim(), true) || it.name.contains(text.trim(), true) || it.major.contains(text.trim(), true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    var searchQuery by mutableStateOf(""); private set
    var isLoading by mutableStateOf(false); private set
    var errorMessage by mutableStateOf<String?>(null); private set
    var statusMessage by mutableStateOf<String?>(null); private set
    var isOnline by mutableStateOf(false); private set
    var hasLoaded by mutableStateOf(false); private set
    init { loadStudents() }
    fun updateSearchQuery(value: String) { searchQuery = value; query.value = value }
    private fun runAction(action: suspend () -> Unit) {
        if (isLoading) return
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try { action() }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { errorMessage = "ทำรายการไม่สำเร็จ: ${e.localizedMessage}" }
            finally { isLoading = false }
        }
    }
    fun loadStudents() = runAction {
        isOnline = refreshStudents()
        hasLoaded = true
    }
    fun syncStudents() = runAction {
        val result = syncStudentsUseCase()
        statusMessage = "Sync สำเร็จ ${result.synced} รายการ / FAILED ${result.failed} รายการ"
        isOnline = refreshStudents()
        hasLoaded = true
    }
    fun addStudent(student: Student, onSuccess: () -> Unit = {}) = runAction {
        add(student)
        statusMessage = "บันทึกในเครื่องแล้ว (PENDING) กด Sync Now เมื่อพร้อม"
        onSuccess()
    }
    fun updateStudent(student: Student, onSuccess: () -> Unit = {}) = runAction { update(student); onSuccess() }
    fun deleteStudent(student: Student, onSuccess: () -> Unit = {}) = runAction { delete(student); onSuccess() }
    suspend fun getStudent(id: Int) = getOne(id)
}
