package com.example.studentmanagement.presentation.ui.music

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.studentmanagement.R
import com.example.studentmanagement.presentation.ui.life.LifePage
import kotlinx.coroutines.delay

@Composable
fun MusicScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val player = remember { MediaPlayer.create(context, R.raw.music) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableStateOf(0) }
    var error by remember { mutableStateOf(player == null) }
    DisposableEffect(player, lifecycle) {
        player?.setOnCompletionListener { playing = false; position = 0; it.seekTo(0) }
        player?.setOnErrorListener { _, _, _ -> error = true; playing = false; true }
        val observer = LifecycleEventObserver { _, event -> if(event == Lifecycle.Event.ON_STOP) { if (!error && player?.isPlaying == true) player.pause(); playing = false } }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); player?.release() }
    }
    LaunchedEffect(playing) { while(playing) { position = player?.currentPosition ?: 0; delay(250) } }
    LifePage("Music Player", onBack) {
        Text("Campus Morning", style = MaterialTheme.typography.headlineSmall)
        Text("Original synthesized demo • 12 seconds • Offline")
        Text(if(error) "Audio unavailable" else if(playing) "Playing" else "Paused / Ready")
        LinearProgressIndicator(progress = if(error) 0f else position.toFloat() / (player?.duration ?: 1).coerceAtLeast(1), modifier = androidx.compose.ui.Modifier.fillMaxWidth())
        Text("${position / 1000}s / ${(if(error) 0 else player?.duration ?: 0) / 1000}s")
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(enabled = !error, onClick = { if(playing) player?.pause() else player?.start(); playing = !playing }) { Text(if(playing) "Pause" else "Play") }
            OutlinedButton(enabled = !error, onClick = { player?.pause(); player?.seekTo(0); playing = false; position = 0 }) { Text("Stop") }
        }
        Text("Playback pauses when the app goes to background and releases when leaving this screen.")
    }
}
