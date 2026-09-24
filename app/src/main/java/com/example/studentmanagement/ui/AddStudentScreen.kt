package com.example.studentmanagement.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.viewmodel.StudentViewModel

// UI Layer: หน้าจอเพิ่มนักศึกษา (Exercise 1)
// Flow: User กด Save -> StudentScreen -> ViewModel -> Repository -> DAO -> Room -> SQLite
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

            Button(
                onClick = {
                    if (studentCode.isNotBlank() && name.isNotBlank() && major.isNotBlank()) {
                        // id เริ่มจาก 1000 เพื่อหลีกเลี่ยง ID ซ้ำกับ API (ซึ่งให้ id 1..10)
                        // Note: นี่เป็น simplification สำหรับ Lab เท่านั้น
                        // ในระบบจริงควรใช้ autoGenerate หรือ UUID
                        viewModel.addStudent(
                            Student(
                                id = 0, // autoGenerate จะจัดการให้
                                studentCode = studentCode,
                                name = name,
                                major = major
                            )
                        )
                        onNavigateBack()
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SAVE")
            }
        }
    }
}
