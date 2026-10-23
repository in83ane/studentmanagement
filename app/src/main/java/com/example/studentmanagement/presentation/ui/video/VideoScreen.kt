package com.example.studentmanagement.presentation.ui.video

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.studentmanagement.R
import com.example.studentmanagement.presentation.ui.life.LifePage

@Composable
fun VideoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var status by remember { mutableStateOf("Preparing video") }
    var prepared by remember { mutableStateOf(false) }
    val video = remember { VideoView(context) }
    DisposableEffect(video, lifecycle) {
        val observer = LifecycleEventObserver { _, event -> if(event == Lifecycle.Event.ON_STOP) { video.pause(); status = "Paused" } }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); video.stopPlayback() }
    }
    LifePage("Campus Video", onBack) {
        Text("Original animated campus demo • 8 seconds • Offline")
        AndroidView(factory = {
            video.apply {
                setMediaController(MediaController(context).also { it.setAnchorView(this) })
                setOnPreparedListener { prepared = true; status = "Ready"; seekTo(1) }
                setOnCompletionListener { status = "Completed" }
                setOnErrorListener { _, _, _ -> prepared = false; status = "Video unavailable"; true }
                setVideoURI(Uri.parse("android.resource://${context.packageName}/${R.raw.campus}"))
            }
        }, modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f))
        Text(status)
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(enabled = prepared, onClick = { video.start(); status = "Playing" }) { Text("Play") }
            OutlinedButton(enabled = prepared, onClick = { video.pause(); status = "Paused" }) { Text("Pause") }
            OutlinedButton(enabled = prepared, onClick = { video.pause(); video.seekTo(0); status = "Stopped" }) { Text("Stop") }
        }
        Text("Compose → AndroidView → VideoView. Tap video for native media controls.")
    }
}
