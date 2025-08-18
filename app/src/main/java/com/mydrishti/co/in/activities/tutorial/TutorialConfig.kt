package com.mydrishti.co.`in`.activities.tutorial

/**
 * Configuration for a tutorial flow
 */
data class TutorialConfig(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<TutorialStep>,
    val version: String = TutorialConstants.TUTORIAL_CONFIG_VERSION
) {
    /**
     * Validates if the tutorial configuration is valid
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && 
               name.isNotBlank() && 
               steps.isNotEmpty() && 
               steps.all { it.isValid() } &&
               hasUniqueStepIds()
    }
    
    /**
     * Checks if all step IDs are unique
     */
    private fun hasUniqueStepIds(): Boolean {
        val stepIds = steps.map { it.id }
        return stepIds.size == stepIds.distinct().size
    }
    
    /**
     * Gets a step by its ID
     */
    fun getStepById(stepId: String): TutorialStep? {
        return steps.find { it.id == stepId }
    }
    
    /**
     * Gets the index of a step by its ID
     */
    fun getStepIndex(stepId: String): Int {
        return steps.indexOfFirst { it.id == stepId }
    }
    
    /**
     * Gets the next step after the given step ID
     */
    fun getNextStep(currentStepId: String): TutorialStep? {
        val currentIndex = getStepIndex(currentStepId)
        return if (currentIndex >= 0 && currentIndex < steps.size - 1) {
            steps[currentIndex + 1]
        } else {
            null
        }
    }
    
    /**
     * Gets the previous step before the given step ID
     */
    fun getPreviousStep(currentStepId: String): TutorialStep? {
        val currentIndex = getStepIndex(currentStepId)
        return if (currentIndex > 0) {
            steps[currentIndex - 1]
        } else {
            null
        }
    }
    
    /**
     * Checks if this is the first step
     */
    fun isFirstStep(stepId: String): Boolean {
        return getStepIndex(stepId) == 0
    }
    
    /**
     * Checks if this is the last step
     */
    fun isLastStep(stepId: String): Boolean {
        return getStepIndex(stepId) == steps.size - 1
    }
}