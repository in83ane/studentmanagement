package com.example.studentmanagement.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studentmanagement.data.repository.StudentRepository
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.util.NetworkMonitor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ViewModel Layer: จัดการ UI State และเรียกใช้ Repository
// ใช้ mutableStateOf กับ private set ตามแนวทางของ Worksheet (STEP 5, 26, 27)
class StudentViewModel(
    private val repository: StudentRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    // Student State (STEP 5)
    var students by mutableStateOf(listOf<Student>())
        private set

    // Loading State (STEP 26)
    var isLoading by mutableStateOf(false)
        private set

    // Error State (STEP 27)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Search query state (Exercise 3)
    var searchQuery by mutableStateOf("")
        private set

    // Online/Offline state (Exercise 4) — อยู่ใน ViewModel ไม่ใช่ UI
    var isOnline by mutableStateOf(false)
        private set

    // เก็บข้อมูลทั้งหมดไว้สำหรับ filter
    private var allStudents: List<Student> = emptyList()

    init {
        // สังเกตสถานะเครือข่ายจาก NetworkMonitor (Flow -> ViewModel)
        viewModelScope.launch {
            networkMonitor.isOnline.collectLatest { online ->
                isOnline = online
            }
        }
        // Auto-load เมื่อเริ่มต้น
        loadStudents()
    }

    // STEP 26, 27: loadStudents ใช้ viewModelScope.launch กับ try/catch/finally
    fun loadStudents() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                allStudents = repository.getStudents()
                applySearch(searchQuery)
            } catch (e: Exception) {
                errorMessage = "ไม่สามารถโหลดข้อมูลได้"
            } finally {
                isLoading = false
            }
        }
    }

    // Exercise 3: Search ค้นจาก studentCode, name, major (case-insensitive)
    fun updateSearchQuery(query: String) {
        searchQuery = query
        applySearch(query)
    }

    private fun applySearch(query: String) {
        if (query.isBlank()) {
            students = allStudents
        } else {
            val lowerQuery = query.lowercase()
            students = allStudents.filter {
                it.studentCode.lowercase().contains(lowerQuery) ||
                it.name.lowercase().contains(lowerQuery) ||
                it.major.lowercase().contains(lowerQuery)
            }
        }
    }

    // Exercise 1: เพิ่ม Student -> Flow: ViewModel -> Repository -> DAO -> Room
    fun addStudent(student: Student) {
        viewModelScope.launch {
            repository.addStudent(student)
            loadStudents()
        }
    }

    // Exercise 2: ลบ Student -> Flow: ViewModel -> Repository -> DAO -> Room
    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            loadStudents()
        }
    }

    // อัปเดต Student
    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
            loadStudents()
        }
    }

    // Bonus: ดึง Student ตาม id สำหรับหน้า Detail
    suspend fun getStudent(id: Int): Student? {
        return repository.getStudentById(id)
    }
}
