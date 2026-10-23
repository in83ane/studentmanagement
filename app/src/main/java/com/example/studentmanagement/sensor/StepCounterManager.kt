package com.example.studentmanagement.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.*
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepCounterManager(private val context: Context) : SensorEventListener {
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val counter = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val hasStepSensor = counter != null
    val hasAccelerometer = accelerometer != null
    private val _steps = MutableStateFlow<Int?>(null)
    val steps = _steps.asStateFlow()
    private val _axes = MutableStateFlow("Waiting for accelerometer")
    val axes = _axes.asStateFlow()
    private val _status = MutableStateFlow("Sensor paused")
    val status = _status.asStateFlow()
    fun hasPermission() = Build.VERSION.SDK_INT < 29 || ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
    fun start() {
        stop()
        accelerometer?.let { manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        _status.value = when {
            counter == null -> "No step counter sensor on this device"
            !hasPermission() -> "Activity permission required"
            else -> try {
                if (manager.registerListener(this, counter, SensorManager.SENSOR_DELAY_NORMAL)) "Waiting for step sensor event" else "Step sensor unavailable"
            } catch (_: SecurityException) { "Activity permission required" }
        }
    }
    fun stop() { manager.unregisterListener(this) }
    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                _steps.value = event.values[0].toInt().coerceAtLeast(0)
                _status.value = "Live step counter · since device reboot"
            }
            Sensor.TYPE_ACCELEROMETER -> _axes.value = "X %.2f   Y %.2f   Z %.2f m/s²".format(event.values[0], event.values[1], event.values[2])
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
