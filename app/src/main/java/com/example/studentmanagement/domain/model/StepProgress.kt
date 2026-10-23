package com.example.studentmanagement.domain.model

object StepProgress {
    const val GOAL = 10_000
    fun fraction(steps: Int): Float = (steps.coerceAtLeast(0) / GOAL.toFloat()).coerceIn(0f, 1f)
    fun walking(steps: Int) = steps >= 1000
}
