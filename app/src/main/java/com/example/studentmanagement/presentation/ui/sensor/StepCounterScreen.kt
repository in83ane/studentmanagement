package com.example.studentmanagement.presentation.ui.sensor

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.studentmanagement.sensor.StepCounterManager
import com.example.studentmanagement.domain.model.StepProgress
import com.example.studentmanagement.presentation.ui.life.LifePage

@Composable
fun StepCounterScreen(manager: StepCounterManager, onBack: () -> Unit) {
    val steps by manager.steps.collectAsState()
    val axes by manager.axes.collectAsState()
    val status by manager.status.collectAsState()
    var granted by remember { mutableStateOf(manager.hasPermission()) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it; manager.start() }
    val progress by animateFloatAsState(StepProgress.fraction(steps ?: 0), label = "sensorProgress")
    LifePage("Step Counter", onBack) {
        Text(status)
        if(!granted && manager.hasStepSensor) Button(onClick = { if(Build.VERSION.SDK_INT >= 29) permission.launch(Manifest.permission.ACTIVITY_RECOGNITION) }) { Text("Allow activity recognition") }
        Text(steps?.toString() ?: "—", style = MaterialTheme.typography.displayLarge)
        Text("Cumulative steps since reboot · target 10,000")
        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
        Text("${((steps ?: 0).coerceAtMost(10000).coerceAtLeast(0)/100f).toInt()}% of target")
        Text("The counter is not today's steps. Sensor events can be delayed; no data is shown as —, not invented steps.")
        HorizontalDivider()
        Text("Accelerometer", style = MaterialTheme.typography.titleLarge)
        Text(if(manager.hasAccelerometer) axes else "No accelerometer on this device")
        Text("Listeners pause while the app is in background.")
    }
}
