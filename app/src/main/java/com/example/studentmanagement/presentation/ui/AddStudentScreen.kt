package com.example.studentmanagement.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studentmanagement.domain.model.Student
import com.example.studentmanagement.presentation.viewmodel.StudentViewModel

// UI Layer: หน้าจอเพิ่มนักศึกษา (Exercise 1)
// Flow: User กด Save -> StudentScreen -> ViewModel -> Repository -> Retrofit -> API -> PostgreSQL -> Room
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(
    viewModel: StudentViewModel,
    onNavigateBack: () -> Unit
) {
    var studentCode by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Student") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = studentCode,
                onValueChange = { studentCode = it },
                label = { Text("Student Code") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = major,
                onValueChange = { major = it },
                label = { Text("Major") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (showError) {
                Text("All fields are required", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            viewModel.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                enabled = !viewModel.isLoading,
                onClick = {
                    if (studentCode.isNotBlank() && name.isNotBlank() && major.isNotBlank()) {
                        viewModel.addStudent(
                            Student(
                                id = 0,
                                studentCode = studentCode.trim(),
                                name = name.trim(),
                                major = major.trim()
                            ), onSuccess = onNavigateBack
                        )
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAVE TO DEVICE")
            }
        }
    }
}
