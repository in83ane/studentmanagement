package com.example.studentmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.studentmanagement.data.repository.StudentRepository
import com.example.studentmanagement.util.NetworkMonitor

// ViewModel Factory: For manual DI of StudentViewModel
class StudentViewModelFactory(
    private val repository: StudentRepository,
    private val networkMonitor: NetworkMonitor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudentViewModel(repository, networkMonitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
