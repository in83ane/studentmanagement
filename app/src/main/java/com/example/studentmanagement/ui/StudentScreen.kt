package com.example.studentmanagement.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.studentmanagement.model.Student
import com.example.studentmanagement.viewmodel.StudentViewModel

// UI Layer: หน้าจอหลัก แสดงรายชื่อนักศึกษา (StudentList)
// ใช้ State จาก ViewModel ผ่าน viewModel.students, viewModel.isLoading, viewModel.errorMessage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    // อ่าน State จาก ViewModel (State-driven UI)
    val students = viewModel.students
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val searchQuery = viewModel.searchQuery
    val isOnline = viewModel.isOnline

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Management") },
                actions = {
                    // Exercise 4: แสดงสถานะ ● Online / ● Offline
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    if (isOnline) Color.Green else Color.Red,
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            )
        },
        // Exercise 1: FAB "+" สำหรับเพิ่มนักศึกษา
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Filled.Add, contentDescription = "Add Student")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Exercise 3: Search Box ค้นจาก studentCode, name, major
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search Students") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // ปุ่ม Load Students (STEP 6)
            Button(
                onClick = { viewModel.loadStudents() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Load Students")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // STEP 26: Loading State -> CircularProgressIndicator
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            // STEP 27: Error State -> แสดง errorMessage
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            // Empty State: ไม่มีนักศึกษา
            } else if (students.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No students found.")
                }
            // แสดงรายการนักศึกษาใน LazyColumn (STEP 3)
            } else {
                LazyColumn {
                    items(students) { student ->
                        StudentCard(
                            student = student,
                            onClick = { onNavigateToDetail(student.id) },
                            onDelete = { viewModel.deleteStudent(student) }
                        )
                    }
                }
            }
        }
    }
}

// Card แสดงข้อมูลนักศึกษา: "65010001 - Somchai" + major + ปุ่ม Delete
@Composable
fun StudentCard(
    student: Student,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // แสดง "65010001 - Somchai"
                Text(
                    "${student.studentCode} - ${student.name}",
                    style = MaterialTheme.typography.titleMedium
                )
                // แสดง major
                Text(student.major, style = MaterialTheme.typography.bodyMedium)
            }
            // Exercise 2: ปุ่ม Delete ในแต่ละ Student
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
