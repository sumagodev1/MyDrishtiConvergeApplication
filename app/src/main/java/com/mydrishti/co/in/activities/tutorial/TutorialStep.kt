package com.mydrishti.co.`in`.activities.tutorial

/**
 * Represents a single tutorial step
 */
data class TutorialStep(
    val id: String,
    val title: String,
    val description: String,
    val targetViewId: String? = null,
    val animationType: AnimationType = AnimationType.FADE_SCALE,
    val duration: Long = 3000L,
    val explanatoryText: String? = null,
    val isSkippable: Boolean = true,
    val validationRequired: Boolean = false,
    val order: Int? = null
) {
    /**
     * Validates if the tutorial step is properly configured
     */
    fun isValid(): Boolean {
        return id.isNotBlank() && 
               title.isNotBlank() && 
               description.isNotBlank() && 
               duration > 0
    }
    

}

/**
 * Animation types for tutorial steps
 */
enum class AnimationType {
    FADE,
    SCALE,
    SLIDE_IN,
    PULSE,
    RIPPLE,
    CELEBRATION,
    FADE_SCALE
}

/**
 * Tutorial completion status
 */
// Duplicate CompletionStatus enum removed. Use the canonical definition in
// `com.mydrishti.co.in.activities.tutorial.CompletionStatus` (see CompletionStatus.kt).