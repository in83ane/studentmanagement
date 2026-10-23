package com.example.studentmanagement.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.studentmanagement.domain.model.StepProgress
import com.example.studentmanagement.sensor.StepCounterManager
import com.example.studentmanagement.presentation.ui.animation.LoadingAnimation
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(manager: StepCounterManager, navigate: (String) -> Unit) {
    val steps by manager.steps.collectAsState()
    val sensorStatus by manager.status.collectAsState()
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(false) }
    var message by rememberSaveable { mutableStateOf(false) }
    var demo by rememberSaveable { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> active = lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) { delay(100); visible = true }
    val shownSteps = if(demo) 1200 else steps ?: 0
    val progress by animateFloatAsState(StepProgress.fraction(shownSteps), tween(800), label = "stepProgress")
    val height by animateDpAsState(if(expanded) 220.dp else 120.dp, tween(500), label = "cardHeight")
    val transition = updateTransition(selected, label = "cardTransition")
    val scale by transition.animateFloat(label = "scale") { if(it) 1.05f else 1f }
    val alpha by transition.animateFloat(label = "alpha") { if(it) 1f else .7f }
    val selectedColor = MaterialTheme.colorScheme.primaryContainer
    val normalColor = MaterialTheme.colorScheme.surfaceVariant
    val cardColor by transition.animateColor(label = "cardColor") { if(it) selectedColor else normalColor }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("STUDENT LIFE / LAB 11", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        AnimatedVisibility(visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }) {
            Column { Text("Hello Student", style = MaterialTheme.typography.headlineLarge); Text("ronnakorn muangkan · Learn. Move. Create.") }
        }
        Card(Modifier.fillMaxWidth().height(height).graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }.clickable { selected = !selected }, colors = CardDefaults.cardColors(containerColor = cardColor)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if(demo) "DEMO PREVIEW · 1,200 steps" else "${steps?.toString() ?: "—"} steps since reboot", style = MaterialTheme.typography.titleLarge)
                LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                Text("Goal 10,000 · Tap card to ${if(selected) "deselect" else "select"}", style = MaterialTheme.typography.bodySmall)
                if(expanded) { Text(if(demo) "Demo values preview motion only; no sensor data is changed." else sensorStatus); Text("Progress animates with the step value.") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { expanded = !expanded }) { Text(if(expanded) "Collapse" else "Expand") }
            OutlinedButton(onClick = { message = !message }) { Text(if(message) "Hide Message" else "Show Message") }
        }
        AnimatedVisibility(message, enter = fadeIn() + slideInVertically(), exit = fadeOut() + slideOutVertically()) {
            Card(Modifier.fillMaxWidth()) { Text("Keep Going! Every step matters.", Modifier.padding(16.dp)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            LoadingAnimation(StepProgress.walking(shownSteps), active)
            Column(Modifier.weight(1f)) {
                Text(if(StepProgress.walking(shownSteps)) "Walking animation" else "Loading animation", style = MaterialTheme.typography.titleMedium)
                Text(if(demo) "Demo preview is ON" else "Live sensor mode", style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = { demo = !demo }) { Text(if(demo) "Use live steps" else "Preview 1,200 steps") }
            }
        }
        Text("Explore your day", style = MaterialTheme.typography.titleLarge)
        listOf(listOf("Camera Filter" to "camera", "Music Player" to "music"), listOf("Campus Video" to "video", "Step Counter" to "sensor")).forEachIndexed { index, row ->
            AnimatedVisibility(visible, enter = fadeIn(tween(600, index*120)) + slideInVertically(tween(600, index*120)) { it/2 }) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { (title, route) ->
                        FilledTonalButton(onClick = { navigate(route) }, modifier = Modifier.weight(1f).height(70.dp)) { Text(title) }
                    }
                }
            }
        }
        OutlinedButton(onClick = { navigate("student_list") }, modifier = Modifier.fillMaxWidth()) { Text("Student Management · Labs 3–7") }
    }
}
