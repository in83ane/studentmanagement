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

// UI Layer: หน้าจอ Student Detail (Bonus point)
// แสดง ID, Code, Name, Major พร้อมปุ่ม [Edit] [Delete]
// Flow: StudentList -> click -> StudentDetail
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    viewModel: StudentViewModel,
    studentId: Int,
    onNavigateBack: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var major by remember { mutableStateOf("") }
    var student by remember { mutableStateOf<Student?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // โหลดข้อมูลนักศึกษาจาก Repository ผ่าน ViewModel
    LaunchedEffect(studentId) {
        student = viewModel.getStudent(studentId)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Detail") },
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
            if (isLoading) {
                CircularProgressIndicator()
            } else if (student == null) {
                Text("Student not found")
            } else {
                val currentStudent = student!!
                viewModel.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (editing) {
                    OutlinedTextField(code, { code = it }, label = { Text("Student Code") })
                    OutlinedTextField(name, { name = it }, label = { Text("Name") })
                    OutlinedTextField(major, { major = it }, label = { Text("Major") })
                    Button(
                        enabled = !viewModel.isLoading && code.isNotBlank() && name.isNotBlank() && major.isNotBlank(),
                        onClick = {
                            val updated = currentStudent.copy(studentCode = code.trim(), name = name.trim(), major = major.trim())
                            viewModel.updateStudent(updated) { student = updated; editing = false }
                        }
                    ) { Text("Save changes") }
                }
                Text("Server ID: ${currentStudent.serverId ?: "-"} | Sync: ${currentStudent.syncStatus}")
                // แสดงข้อมูลตาม Bonus: ID, Code, Name, Major
                Text("Local ID: ${currentStudent.id}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Code: ${currentStudent.studentCode}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Name: ${currentStudent.name}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Major: ${currentStudent.major}", style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // ปุ่ม [Edit] และ [Delete] ตาม Bonus
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(enabled = !viewModel.isLoading, onClick = {
                        code = currentStudent.studentCode
                        name = currentStudent.name
                        major = currentStudent.major
                        editing = true
                    }) {
                        Text("Edit")
                    }
                    Button(
                        enabled = !viewModel.isLoading,
                        onClick = {
                            // Flow: Delete Button -> ViewModel -> Repository -> API -> Room
                            viewModel.deleteStudent(currentStudent, onSuccess = onNavigateBack)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}
