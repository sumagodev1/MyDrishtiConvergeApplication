package com.mydrishti.co.`in`.tutorial

import com.mydrishti.co.`in`.activities.tutorial.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TutorialStateManager data models and logic
 * Note: These tests focus on data model validation and logic without requiring Android Context
 */
class TutorialStateManagerTest {

    @Test
    fun testTutorialStateLogic() {
        // Test TutorialState logic methods
        val initialState = TutorialState()
        
        // Test initial state
        assertFalse("Initial state should not be running", initialState.isRunning())
        assertFalse("Initial state should not be completed", initialState.isCompleted())
        assertFalse("Initial state should not be skipped", initialState.isSkipped())
        assertEquals("Initial time spent should be 0", 0L, initialState.getTotalTimeSpent())
        
        // Test completed state
        val completedState = initialState.copy(completionStatus = CompletionStatus.COMPLETED)
        assertTrue("Completed state should be completed", completedState.isCompleted())
        assertFalse("Completed state should not be running", completedState.isRunning())
        
        // Test skipped state
        val skippedState = initialState.copy(completionStatus = CompletionStatus.SKIPPED, wasSkipped = true)
        assertTrue("Skipped state should be skipped", skippedState.isSkipped())
        assertFalse("Skipped state should not be running", skippedState.isRunning())
        
        // Test running state
        val runningState = initialState.copy(
            isActive = true, 
            completionStatus = CompletionStatus.IN_PROGRESS,
            currentStepId = "step1"
        )
        assertTrue("Running state should be running", runningState.isRunning())
        assertFalse("Running state should not be completed", runningState.isCompleted())
    }

    @Test
    fun testTutorialStateWithCompletedStep() {
        // Test state with completed step
        val initialState = TutorialState(
            startTime = 1000L,
            lastInteractionTime = 3000L,
            completedSteps = setOf("step1")
        )
        
        val stateWithNewStep = initialState.withCompletedStep("step2")
        assertTrue("Should contain original step", stateWithNewStep.completedSteps.contains("step1"))
        assertTrue("Should contain new step", stateWithNewStep.completedSteps.contains("step2"))
        assertEquals("Should have 2 completed steps", 2, stateWithNewStep.completedSteps.size)
        
        // Test time calculation
        assertEquals("Should calculate time spent correctly", 2000L, initialState.getTotalTimeSpent())
    }

    @Test
    fun testTutorialStateWithCurrentStep() {
        // Test state with current step
        val initialState = TutorialState()
        val stateWithCurrent = initialState.withCurrentStep("step2")
        
        assertEquals("Should have current step", "step2", stateWithCurrent.currentStepId)
        assertTrue("Last interaction time should be updated", stateWithCurrent.lastInteractionTime > 0)
    }

    @Test
    fun testTutorialConstants() {
        // Test that constants are properly defined
        assertNotNull("Tutorial ID should be defined", TutorialConstants.FIRST_TIME_USER_TUTORIAL_ID)
        assertNotNull("Step IDs should be defined", TutorialConstants.STEP_WELCOME_FAB)
        assertNotNull("Preferences name should be defined", TutorialConstants.TUTORIAL_PREFS_NAME)
        
        // Test timeout values are reasonable
        assertTrue("Step timeout should be positive", TutorialConstants.TUTORIAL_STEP_TIMEOUT > 0)
        assertTrue("Total timeout should be positive", TutorialConstants.TUTORIAL_TOTAL_TIMEOUT > 0)
        assertTrue("Total timeout should be greater than step timeout", 
            TutorialConstants.TUTORIAL_TOTAL_TIMEOUT > TutorialConstants.TUTORIAL_STEP_TIMEOUT)
    }

    @Test
    fun testTutorialConfigFactoryCreation() {
        // Test that the factory creates a valid tutorial configuration
        val config = TutorialConfigFactory.createFirstTimeUserTutorial()
        
        assertTrue("Config should be valid", config.isValid())
        assertEquals("Should have correct tutorial ID", TutorialConstants.FIRST_TIME_USER_TUTORIAL_ID, config.tutorialId)
        assertEquals("Should have 6 steps", 6, config.steps.size)
        
        // Test step IDs are correct
        val stepIds = config.steps.map { it.id }
        assertTrue("Should contain welcome step", stepIds.contains(TutorialConstants.STEP_WELCOME_FAB))
        assertTrue("Should contain chart type step", stepIds.contains(TutorialConstants.STEP_CHART_TYPE_SELECTION))
        assertTrue("Should contain site selection step", stepIds.contains(TutorialConstants.STEP_SITE_SELECTION))
        assertTrue("Should contain parameter step", stepIds.contains(TutorialConstants.STEP_PARAMETER_SELECTION))
        assertTrue("Should contain save step", stepIds.contains(TutorialConstants.STEP_SAVE_CHART))
        assertTrue("Should contain celebration step", stepIds.contains(TutorialConstants.STEP_COMPLETION_CELEBRATION))
    }

    @Test
    fun testTutorialConfigStepNavigation() {
        // Test step navigation in the factory-created config
        val config = TutorialConfigFactory.createFirstTimeUserTutorial()
        
        // Test getting step by ID
        val welcomeStep = config.getStepById(TutorialConstants.STEP_WELCOME_FAB)
        assertNotNull("Should find welcome step", welcomeStep)
        assertEquals("Welcome step should have correct title", "Welcome!", welcomeStep?.title)
        
        // Test getting next step
        val nextStep = config.getNextStep(TutorialConstants.STEP_WELCOME_FAB)
        assertNotNull("Should have next step after welcome", nextStep)
        assertEquals("Next step should be chart type selection", TutorialConstants.STEP_CHART_TYPE_SELECTION, nextStep?.id)
        
        // Test last step has no next step
        val lastStep = config.getNextStep(TutorialConstants.STEP_COMPLETION_CELEBRATION)
        assertNull("Last step should have no next step", lastStep)
    }

    @Test
    fun testTutorialStepValidation() {
        // Test that all factory-created steps are valid
        val config = TutorialConfigFactory.createFirstTimeUserTutorial()
        
        config.steps.forEach { step ->
            assertTrue("Step ${step.id} should be valid", step.isValid())
            assertTrue("Step ${step.id} should have non-empty title", step.title.isNotBlank())
            assertTrue("Step ${step.id} should have non-empty description", step.description.isNotBlank())
            assertTrue("Step ${step.id} should have positive duration", step.duration > 0)
        }
    }

    @Test
    fun testAnimationTypes() {
        // Test that animation types are properly assigned
        val config = TutorialConfigFactory.createFirstTimeUserTutorial()
        
        val welcomeStep = config.getStepById(TutorialConstants.STEP_WELCOME_FAB)
        assertEquals("Welcome step should use PULSE animation", AnimationType.PULSE, welcomeStep?.animationType)
        
        val chartTypeStep = config.getStepById(TutorialConstants.STEP_CHART_TYPE_SELECTION)
        assertEquals("Chart type step should use FADE_SCALE animation", AnimationType.FADE_SCALE, chartTypeStep?.animationType)
        
        val celebrationStep = config.getStepById(TutorialConstants.STEP_COMPLETION_CELEBRATION)
        assertEquals("Celebration step should use CELEBRATION animation", AnimationType.CELEBRATION, celebrationStep?.animationType)
    }
}