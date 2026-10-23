package com.example.studentmanagement

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.studentmanagement.sensor.StepCounterManager
import com.example.studentmanagement.presentation.ui.home.HomeScreen
import com.example.studentmanagement.presentation.ui.camera.CameraScreen
import com.example.studentmanagement.presentation.ui.music.MusicScreen
import com.example.studentmanagement.presentation.ui.video.VideoScreen
import com.example.studentmanagement.presentation.ui.sensor.StepCounterScreen
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.example.studentmanagement.data.remote.TokenStorage
import com.example.studentmanagement.worker.SyncScheduler
import com.example.studentmanagement.presentation.ui.login.LoginScreen
import com.example.studentmanagement.presentation.ui.login.LoginViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studentmanagement.presentation.ui.AddStudentScreen
import com.example.studentmanagement.presentation.ui.StudentDetailScreen
import com.example.studentmanagement.presentation.ui.StudentScreen
import com.example.studentmanagement.presentation.viewmodel.StudentViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var tokenStorage: TokenStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val manager = remember { StepCounterManager(applicationContext) }
                    DisposableEffect(lifecycle) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) manager.start()
                            if (event == Lifecycle.Event.ON_PAUSE) manager.stop()
                        }
                        lifecycle.addObserver(observer)
                        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) manager.start()
                        onDispose { lifecycle.removeObserver(observer); manager.stop() }
                    }
                    val token by tokenStorage.token.collectAsState()
                    val loginViewModel: LoginViewModel = hiltViewModel()
                    val navController = rememberNavController()
                    LaunchedEffect(token) {
                        if (token == null) SyncScheduler.cancel(this@MainActivity)
                        else SyncScheduler.schedulePeriodicSync(this@MainActivity)
                        navController.navigate(if (token == null) "login" else "home") {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }

                    NavHost(navController = navController, startDestination = if (tokenStorage.getToken() == null) "login" else "home") {
                        composable("home") { HomeScreen(manager) { navController.navigate(it) } }
                        composable("camera", enterTransition = { slideInHorizontally(tween(450)) { it } }, exitTransition = { slideOutHorizontally(tween(450)) { it } }) { CameraScreen { navController.popBackStack() } }
                        composable("music", enterTransition = { fadeIn(tween(450)) }, exitTransition = { fadeOut(tween(450)) }) { MusicScreen { navController.popBackStack() } }
                        composable("video", enterTransition = { scaleIn(tween(450), initialScale = .85f) + fadeIn() }, exitTransition = { scaleOut(tween(450), targetScale = .85f) + fadeOut() }) { VideoScreen { navController.popBackStack() } }
                        composable("sensor", enterTransition = { slideInHorizontally(tween(450)) { it } }, exitTransition = { slideOutHorizontally(tween(450)) { it } }) { StepCounterScreen(manager) { navController.popBackStack() } }
                        composable("login") { LoginScreen(loginViewModel) }
                        composable("student_list") { entry ->
                            val viewModel: StudentViewModel = hiltViewModel(entry)
                            StudentScreen(
                                viewModel = viewModel,
                                onLogout = { tokenStorage.clearToken() },
                                onNavigateToAdd = { navController.navigate("add_student") },
                                onNavigateToDetail = { id -> navController.navigate("student_detail/$id") }
                            )
                        }
                        composable("add_student") {
                            val parent = remember(it) { navController.getBackStackEntry("student_list") }
                            val viewModel: StudentViewModel = hiltViewModel(parent)
                            AddStudentScreen(
                                viewModel = viewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "student_detail/{studentId}",
                            arguments = listOf(navArgument("studentId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val parent = remember(backStackEntry) { navController.getBackStackEntry("student_list") }
                            val viewModel: StudentViewModel = hiltViewModel(parent)
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
