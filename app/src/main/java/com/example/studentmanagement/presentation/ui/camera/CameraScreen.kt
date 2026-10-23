package com.example.studentmanagement.presentation.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.content.ContextCompat
import com.example.studentmanagement.presentation.ui.life.LifePage
import java.io.File

@Composable
fun CameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    var message by remember { mutableStateOf("Allow camera access to start preview") }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it; message = if(it) "Camera ready" else "Camera denied. Tap Allow camera to try again, or enable it in App Settings." }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    var filter by rememberSaveable { mutableStateOf("Original") }
    val view = remember { PreviewView(context).apply { implementationMode = PreviewView.ImplementationMode.COMPATIBLE } }
    val capture = remember { ImageCapture.Builder().build() }
    var ready by remember { mutableStateOf(false) }
    DisposableEffect(granted, owner) {
        var disposed = false
        var provider: ProcessCameraProvider? = null
        val preview = Preview.Builder().build()
        if (granted) {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                if (!disposed) try {
                    provider = future.get()
                    preview.setSurfaceProvider(view.surfaceProvider)
                    provider?.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture)
                    ready = true
                    message = "Live preview · overlay only"
                } catch (e: Exception) { ready = false; message = "Camera unavailable: ${e.javaClass.simpleName}" }
            }, ContextCompat.getMainExecutor(context))
        }
        onDispose { disposed = true; ready = false; provider?.unbind(preview, capture) }
    }
    LifePage("Camera Filter", onBack) {
        if (!granted) Button(onClick = { permission.launch(Manifest.permission.CAMERA) }) { Text("Allow camera") }
        Text(message)
        if(granted) Box(Modifier.fillMaxWidth().height(310.dp).clipToBounds()) {
            AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
            val overlay = when(filter) {
                "Warm" -> Color(0xFFFFBF47).copy(alpha=.20f)
                "Cool" -> Color(0xFF4BAFFF).copy(alpha=.20f)
                "Vintage" -> Color(0xFFA97642).copy(alpha=.25f)
                "Dark" -> Color.Black.copy(alpha=.40f)
                "B&W" -> Color.Gray.copy(alpha=.25f)
                else -> Color.Transparent
            }
            Box(Modifier.fillMaxSize().background(overlay))
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Original","Warm","Cool","Vintage","Dark","B&W").forEach { name ->
                FilterChip(selected = filter == name, onClick = { filter = name }, label = { Text(name) })
            }
        }
        Text("Selected: $filter · Visual overlay, not pixel processing. B&W is a gray tint, not true grayscale.", style = MaterialTheme.typography.bodySmall)
        Button(enabled = ready, onClick = {
            val file = File(context.filesDir, "profile-${System.currentTimeMillis()}.jpg")
            capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(result: ImageCapture.OutputFileResults) { message = "Saved original photo: ${file.name}" }
                override fun onError(error: ImageCaptureException) { message = "Capture failed: ${error.imageCaptureError}" }
            })
        }) { Text("Save profile photo") }
        Text("Photo stays in app-private storage. Preview overlays are not applied to the saved photo.", style = MaterialTheme.typography.bodySmall)
    }
}
