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
import kotlinx.coroutines.launch

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
    val coroutineScope = rememberCoroutineScope()
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
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (student == null) {
                Text("Student not found")
            } else {
                val currentStudent = student!!
                // แสดงข้อมูลตาม Bonus: ID, Code, Name, Major
                Text("ID: ${currentStudent.id}", style = MaterialTheme.typography.titleLarge)
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
                    Button(onClick = { /* สามารถเพิ่ม Edit ได้ในอนาคต */ }) {
                        Text("Edit")
                    }
                    Button(
                        onClick = {
                            // Flow: Delete Button -> ViewModel -> Repository -> DAO -> Room
                            coroutineScope.launch {
                                viewModel.deleteStudent(currentStudent)
                                onNavigateBack()
                            }
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
