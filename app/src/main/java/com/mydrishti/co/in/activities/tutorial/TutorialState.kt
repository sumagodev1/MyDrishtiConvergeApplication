package com.mydrishti.co.`in`.activities.tutorial

/**
 * Data class representing the current state of a tutorial
 */
data class TutorialState(
    val isActive: Boolean = false,
    val currentStepId: String? = null,
    val completedSteps: Set<String> = emptySet(),
    val startTime: Long = 0L,
    val lastInteractionTime: Long = 0L,
    val skipCount: Int = 0,
    val completionStatus: CompletionStatus = CompletionStatus.NOT_STARTED,
    val wasSkipped: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val canRestart: Boolean = true
) {
    /**
     * Checks if the tutorial is currently running
     */
    fun isRunning(): Boolean {
        return isActive && completionStatus == CompletionStatus.IN_PROGRESS
    }
    
    /**
     * Checks if the tutorial has been completed successfully
     */
    fun isCompleted(): Boolean {
        return completionStatus == CompletionStatus.COMPLETED
    }
    
    /**
     * Checks if the tutorial was skipped by the user
     */
    fun isSkipped(): Boolean {
        return completionStatus == CompletionStatus.SKIPPED || wasSkipped
    }
    
    /**
     * Gets the total time spent in the tutorial (in milliseconds)
     */
    fun getTotalTimeSpent(): Long {
        return if (startTime > 0 && lastInteractionTime > startTime) {
            lastInteractionTime - startTime
        } else {
            0L
        }
    }
    
    /**
     * Creates a new state with the given step marked as completed
     */
    fun withCompletedStep(stepId: String): TutorialState {
        return copy(
            completedSteps = completedSteps + stepId,
            lastInteractionTime = System.currentTimeMillis()
        )
    }
    
    /**
     * Creates a new state with the current step updated
     */
    fun withCurrentStep(stepId: String?): TutorialState {
        return copy(
            currentStepId = stepId,
            lastInteractionTime = System.currentTimeMillis()
        )
    }
}