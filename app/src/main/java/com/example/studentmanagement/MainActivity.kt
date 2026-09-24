package com.example.studentmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studentmanagement.ui.AddStudentScreen
import com.example.studentmanagement.ui.StudentDetailScreen
import com.example.studentmanagement.ui.StudentScreen
import com.example.studentmanagement.viewmodel.StudentViewModel
import com.example.studentmanagement.viewmodel.StudentViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Manual DI from Application
        val app = application as StudentApplication
        val factory = StudentViewModelFactory(app.repository, app.networkMonitor)
        val viewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "student_list") {
                        composable("student_list") {
                            StudentScreen(
                                viewModel = viewModel,
                                onNavigateToAdd = { navController.navigate("add_student") },
                                onNavigateToDetail = { id -> navController.navigate("student_detail/$id") }
                            )
                        }
                        composable("add_student") {
                            AddStudentScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "student_detail/{studentId}",
                            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val id = backStackEntry.arguments?.getInt("studentId") ?: 0
                            StudentDetailScreen(
                                viewModel = viewModel,
                                studentId = id,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
