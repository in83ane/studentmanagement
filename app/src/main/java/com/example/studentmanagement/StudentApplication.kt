package com.example.studentmanagement

import android.app.Application
import com.example.studentmanagement.data.local.AppDatabase
import com.example.studentmanagement.data.remote.RetrofitClient
import com.example.studentmanagement.data.repository.StudentRepository
import com.example.studentmanagement.util.NetworkMonitor

// Manual DI: Application class for singleton dependencies
class StudentApplication : Application() {
    
    // Lazy initialization of dependencies
    val database by lazy { AppDatabase.getDatabase(this) }
    
    val repository by lazy {
        StudentRepository(
            studentDao = database.studentDao(),
            studentApi = RetrofitClient.api
        )
    }

    val networkMonitor by lazy {
        NetworkMonitor(this)
    }
}
