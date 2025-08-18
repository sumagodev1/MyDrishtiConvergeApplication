package com.mydrishti.co.`in`.tutorial

import com.mydrishti.co.`in`.activities.tutorial.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for tutorial data models
 */
class TutorialDataModelsTest {

    @Test
    fun testTutorialStepValidation() {
        // Valid tutorial step
        val validStep = TutorialStep(
            id = "step1",
            title = "Welcome",
            description = "Welcome to the app"
        )
        assertTrue("Valid step should pass validation", validStep.isValid())

        // Invalid tutorial step - empty id
        val invalidStep = TutorialStep(
            id = "",
            title = "Welcome",
            description = "Welcome to the app"
        )
        assertFalse("Invalid step with empty id should fail validation", invalidStep.isValid())

        // Invalid tutorial step - negative duration
        val invalidDurationStep = TutorialStep(
            id = "step1",
            title = "Welcome",
            description = "Welcome to the app",
            duration = -1L
        )
        assertFalse("Invalid step with negative duration should fail validation", invalidDurationStep.isValid())
    }

    @Test
    fun testTutorialConfigValidation() {
        val validStep = TutorialStep(
            id = "step1",
            title = "Welcome",
            description = "Welcome to the app"
        )

        // Valid config
        val validConfig = TutorialConfig(
            tutorialId = "first_time_user",
            steps = listOf(validStep)
        )
        assertTrue("Valid config should pass validation", validConfig.isValid())

        // Invalid config - empty tutorial id
        val invalidConfig = TutorialConfig(
            tutorialId = "",
            steps = listOf(validStep)
        )
        assertFalse("Invalid config with empty id should fail validation", invalidConfig.isValid())

        // Invalid config - no steps
        val noStepsConfig = TutorialConfig(
            tutorialId = "first_time_user",
            steps = emptyList()
        )
        assertFalse("Invalid config with no steps should fail validation", noStepsConfig.isValid())
    }

    @Test
    fun testTutorialConfigStepNavigation() {
        val step1 = TutorialStep(id = "step1", title = "Step 1", description = "First step")
        val step2 = TutorialStep(id = "step2", title = "Step 2", description = "Second step")
        val step3 = TutorialStep(id = "step3", title = "Step 3", description = "Third step")

        val config = TutorialConfig(
            tutorialId = "test",
            steps = listOf(step1, step2, step3)
        )

        // Test getting step by ID
        assertEquals("Should find step1", step1, config.getStepById("step1"))
        assertNull("Should not find non-existent step", config.getStepById("nonexistent"))

        // Test getting next step
        assertEquals("Next step after step1 should be step2", step2, config.getNextStep("step1"))
        assertEquals("Next step after step2 should be step3", step3, config.getNextStep("step2"))
        assertNull("Next step after step3 should be null", config.getNextStep("step3"))
    }

    @Test
    fun testTutorialStateOperations() {
        val initialState = TutorialState()

        // Test initial state
        assertFalse("Initial state should not be running", initialState.isRunning())
        assertFalse("Initial state should not be completed", initialState.isCompleted())
        assertFalse("Initial state should not be skipped", initialState.isSkipped())
        assertEquals("Initial time spent should be 0", 0L, initialState.getTotalTimeSpent())

        // Test state with completed step
        val stateWithStep = initialState.withCompletedStep("step1")
        assertTrue("Should contain completed step", stateWithStep.completedSteps.contains("step1"))

        // Test state with current step
        val stateWithCurrent = initialState.withCurrentStep("step2")
        assertEquals("Should have current step", "step2", stateWithCurrent.currentStepId)
    }

    @Test
    fun testAnimationTypeEnum() {
        // Test that all animation types are available
        val animationTypes = AnimationType.values()
        assertTrue("Should have FADE animation", animationTypes.contains(AnimationType.FADE))
        assertTrue("Should have SCALE animation", animationTypes.contains(AnimationType.SCALE))
        assertTrue("Should have PULSE animation", animationTypes.contains(AnimationType.PULSE))
        assertTrue("Should have CELEBRATION animation", animationTypes.contains(AnimationType.CELEBRATION))
    }

    @Test
    fun testCompletionStatusEnum() {
        // Test that all completion statuses are available
        val statuses = CompletionStatus.values()
        assertTrue("Should have NOT_STARTED status", statuses.contains(CompletionStatus.NOT_STARTED))
        assertTrue("Should have IN_PROGRESS status", statuses.contains(CompletionStatus.IN_PROGRESS))
        assertTrue("Should have COMPLETED status", statuses.contains(CompletionStatus.COMPLETED))
        assertTrue("Should have SKIPPED status", statuses.contains(CompletionStatus.SKIPPED))
    }
}