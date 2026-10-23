package com.example.studentmanagement.presentation.ui.animation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.studentmanagement.R

@Composable
fun LoadingAnimation(walking: Boolean, active: Boolean) {
    val result = rememberLottieComposition(LottieCompositionSpec.RawRes(if(walking) R.raw.walking else R.raw.loading))
    val progress by animateLottieCompositionAsState(result.value, isPlaying = active, iterations = LottieConstants.IterateForever)
    if(result.isFailure) Text("Animation unavailable")
    else LottieAnimation(composition = result.value, progress = { progress }, modifier = Modifier.size(96.dp))
}
