package com.example.studentmanagement

import com.example.studentmanagement.domain.model.StepProgress
import org.junit.Assert.*
import org.junit.Test

class StepProgressTest {
    @Test fun progressClampsBadAndAboveGoalReadings() {
        assertEquals(0f, StepProgress.fraction(-5), 0f)
        assertEquals(.5f, StepProgress.fraction(5000), 0f)
        assertEquals(1f, StepProgress.fraction(14000), 0f)
    }
    @Test fun walkingAnimationChangesAtThreshold() {
        assertFalse(StepProgress.walking(999))
        assertTrue(StepProgress.walking(1000))
    }
}
