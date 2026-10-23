package com.example.studentmanagement.presentation.ui

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
import androidx.compose.ui.platform.LocalContext
import com.example.studentmanagement.worker.SyncScheduler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.studentmanagement.domain.model.Student
import com.example.studentmanagement.presentation.viewmodel.StudentViewModel

// UI Layer: หน้าจอหลัก แสดงรายชื่อนักศึกษา (StudentList)
// ใช้ State จาก ViewModel ผ่าน viewModel.students, viewModel.isLoading, viewModel.errorMessage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    viewModel: StudentViewModel,
    onNavigateToAdd: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToDetail: (Int) -> Unit
) {
    // อ่าน State จาก ViewModel (State-driven UI)
    val context = LocalContext.current
    val students by viewModel.students.collectAsState()
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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("เข้าสู่ระบบสำเร็จ", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = onLogout) { Text("Logout") }
            }
            // Exercise 3: Search Box ค้นจาก studentCode, name, major
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                label = { Text("Search Students") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(onClick = { SyncScheduler.syncNow(context) }, modifier = Modifier.fillMaxWidth()) {
                Text("Sync in Background")
            }
            Button(onClick = viewModel::syncStudents, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                Text("Sync Now")
            }
            viewModel.statusMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            // Refresh merges server data into Room without discarding unsynced rows.
            Button(
                onClick = { viewModel.loadStudents() },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Refresh")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error)
            if (viewModel.hasLoaded && !isOnline) Text("Offline: แสดงข้อมูลล่าสุดจาก Room")

            if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            if (students.isEmpty()) {
                Text("No students found.")
            } else {
                LazyColumn {
                    items(students, key = { it.id }) { student ->
                        StudentCard(student, { onNavigateToDetail(student.id) }, { if (!isLoading) viewModel.deleteStudent(student) })
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
                Text("Sync: ${student.syncStatus}", style = MaterialTheme.typography.labelLarge,
                    color = if (student.syncStatus == "FAILED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                Text("Local ID: ${student.id} | Server ID: ${student.serverId ?: "-"}", style = MaterialTheme.typography.bodySmall)
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
