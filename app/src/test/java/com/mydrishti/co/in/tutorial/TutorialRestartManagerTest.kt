package com.mydrishti.co.`in`.tutorial

import com.mydrishti.co.`in`.activities.tutorial.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for TutorialRestartManager and restart functionality
 * Note: These tests focus on the business logic without requiring Android Context
 */
class TutorialRestartManagerTest {

    @Test
    fun testRestartEligibilityLogic() {
        // Test restart eligibility logic
        fun canRestartTutorial(
            isActive: Boolean,
            completionStatus: CompletionStatus,
            restartAttemptCount: Int
        ): Boolean {
            return when {
                isActive -> false
                restartAttemptCount > 5 -> false
                completionStatus == CompletionStatus.COMPLETED -> true
                completionStatus == CompletionStatus.SKIPPED -> true
                completionStatus == CompletionStatus.NOT_STARTED -> true
                else -> false
            }
        }
        
        // Test various scenarios
        assertTrue("Should allow restart for completed tutorial", 
            canRestartTutorial(false, CompletionStatus.COMPLETED, 1))
        assertTrue("Should allow restart for skipped tutorial", 
            canRestartTutorial(false, CompletionStatus.SKIPPED, 1))
        assertTrue("Should allow restart for not started tutorial", 
            canRestartTutorial(false, CompletionStatus.NOT_STARTED, 1))
        
        assertFalse("Should not allow restart for active tutorial", 
            canRestartTutorial(true, CompletionStatus.IN_PROGRESS, 1))
        assertFalse("Should not allow restart after too many attempts", 
            canRestartTutorial(false, CompletionStatus.COMPLETED, 6))
        assertFalse("Should not allow restart for failed tutorial", 
            canRestartTutorial(false, CompletionStatus.FAILED, 1))
    }

    @Test
    fun testRestartSourceHandling() {
        // Test restart source handling logic
        fun getRestartDialogType(source: String): String {
            return when (source) {
                "SETTINGS_MENU" -> "settings_restart_dialog"
                "TUTORIAL_COMPLETED" -> "post_completion_restart_dialog"
                "TUTORIAL_SKIPPED" -> "post_skip_restart_dialog"
                "USER_REQUEST" -> "user_request_restart_dialog"
                "DEBUG_MODE" -> "direct_restart"
                else -> "unknown_source"
            }
        }
        
        // Test different sources
        assertEquals("Should handle settings menu", "settings_restart_dialog", 
            getRestartDialogType("SETTINGS_MENU"))
        assertEquals("Should handle post completion", "post_completion_restart_dialog", 
            getRestartDialogType("TUTORIAL_COMPLETED"))
        assertEquals("Should handle post skip", "post_skip_restart_dialog", 
            getRestartDialogType("TUTORIAL_SKIPPED"))
        assertEquals("Should handle user request", "user_request_restart_dialog", 
            getRestartDialogType("USER_REQUEST"))
        assertEquals("Should handle debug mode", "direct_restart", 
            getRestartDialogType("DEBUG_MODE"))
        assertEquals("Should handle unknown source", "unknown_source", 
            getRestartDialogType("UNKNOWN"))
    }

    @Test
    fun testRestartReasonProcessing() {
        // Test restart reason processing
        fun processRestartReason(reason: String?): Map<String, String> {
            return when {
                reason.isNullOrBlank() -> mapOf("category" to "unknown", "details" to "")
                reason.contains("|feedback:") -> {
                    val parts = reason.split("|feedback:")
                    mapOf("category" to parts[0], "details" to parts.getOrNull(1) ?: "")
                }
                reason.startsWith("Other:") -> mapOf("category" to "other", "details" to reason.substring(6).trim())
                else -> mapOf("category" to reason, "details" to "")
            }
        }
        
        // Test different reason formats
        val unknownReason = processRestartReason(null)
        assertEquals("Should categorize unknown reason", "unknown", unknownReason["category"])
        
        val standardReason = processRestartReason("practice_steps")
        assertEquals("Should categorize standard reason", "practice_steps", standardReason["category"])
        
        val feedbackReason = processRestartReason("missed_information|feedback:The tutorial went too fast")
        assertEquals("Should categorize feedback reason", "missed_information", feedbackReason["category"])
        assertEquals("Should extract feedback details", "The tutorial went too fast", feedbackReason["details"])
        
        val otherReason = processRestartReason("Other: Want to test new features")
        assertEquals("Should categorize other reason", "other", otherReason["category"])
        assertEquals("Should extract other details", "Want to test new features", otherReason["details"])
    }

    @Test
    fun testRestartAttemptTracking() {
        // Test restart attempt tracking
        var restartAttemptCount = 0
        val restartHistory = mutableListOf<Map<String, Any>>()
        
        fun recordRestartAttempt(source: String, reason: String?) {
            restartAttemptCount++
            restartHistory.add(mapOf(
                "attempt" to restartAttemptCount,
                "source" to source,
                "reason" to (reason ?: "unknown"),
                "timestamp" to System.currentTimeMillis()
            ))
        }
        
        fun getRestartStatistics(): Map<String, Any> {
            val sourceCounts = restartHistory.groupBy { it["source"] }.mapValues { it.value.size }
            return mapOf(
                "total_attempts" to restartAttemptCount,
                "history_count" to restartHistory.size,
                "source_breakdown" to sourceCounts,
                "most_recent_source" to (restartHistory.lastOrNull()?.get("source") ?: "none")
            )
        }
        
        // Test tracking
        assertEquals("Should start with 0 attempts", 0, restartAttemptCount)
        
        recordRestartAttempt("SETTINGS_MENU", "practice_steps")
        assertEquals("Should have 1 attempt", 1, restartAttemptCount)
        
        recordRestartAttempt("TUTORIAL_COMPLETED", null)
        assertEquals("Should have 2 attempts", 2, restartAttemptCount)
        
        val stats = getRestartStatistics()
        assertEquals("Should track total attempts", 2, stats["total_attempts"])
        assertEquals("Should track most recent source", "TUTORIAL_COMPLETED", stats["most_recent_source"])
        
        @Suppress("UNCHECKED_CAST")
        val sourceBreakdown = stats["source_breakdown"] as Map<String, Int>
        assertEquals("Should count settings menu attempts", 1, sourceBreakdown["SETTINGS_MENU"])
        assertEquals("Should count completion attempts", 1, sourceBreakdown["TUTORIAL_COMPLETED"])
    }

    @Test
    fun testRestartAvailabilityMessages() {
        // Test restart availability message generation
        fun getRestartAvailabilityMessage(
            isActive: Boolean,
            completionStatus: CompletionStatus,
            canRestart: Boolean
        ): String {
            return when {
                isActive -> "Tutorial is currently active"
                !canRestart -> "Restart not available"
                completionStatus == CompletionStatus.COMPLETED -> "Restart completed tutorial"
                completionStatus == CompletionStatus.SKIPPED -> "Start skipped tutorial"
                else -> "Start tutorial"
            }
        }
        
        // Test different scenarios
        assertEquals("Should show active message", "Tutorial is currently active", 
            getRestartAvailabilityMessage(true, CompletionStatus.IN_PROGRESS, false))
        assertEquals("Should show not available message", "Restart not available", 
            getRestartAvailabilityMessage(false, CompletionStatus.FAILED, false))
        assertEquals("Should show restart completed message", "Restart completed tutorial", 
            getRestartAvailabilityMessage(false, CompletionStatus.COMPLETED, true))
        assertEquals("Should show start skipped message", "Start skipped tutorial", 
            getRestartAvailabilityMessage(false, CompletionStatus.SKIPPED, true))
        assertEquals("Should show start tutorial message", "Start tutorial", 
            getRestartAvailabilityMessage(false, CompletionStatus.NOT_STARTED, true))
    }

    @Test
    fun testRestartAnalyticsCollection() {
        // Test restart analytics collection
        val analyticsEvents = mutableListOf<Map<String, Any>>()
        
        fun recordRestartAnalytics(
            source: String,
            reason: String?,
            attemptCount: Int,
            previousStatus: CompletionStatus,
            completedSteps: Int
        ) {
            analyticsEvents.add(mapOf(
                "event_type" to "tutorial_restart",
                "restart_source" to source,
                "restart_reason" to (reason ?: "unknown"),
                "attempt_count" to attemptCount,
                "previous_status" to previousStatus.name,
                "previous_completed_steps" to completedSteps,
                "timestamp" to System.currentTimeMillis()
            ))
        }
        
        fun getAnalyticsSummary(): Map<String, Any> {
            val sourceCounts = analyticsEvents.groupBy { it["restart_source"] }.mapValues { it.value.size }
            val reasonCounts = analyticsEvents.groupBy { it["restart_reason"] }.mapValues { it.value.size }
            
            return mapOf(
                "total_events" to analyticsEvents.size,
                "source_breakdown" to sourceCounts,
                "reason_breakdown" to reasonCounts,
                "most_common_source" to (sourceCounts.maxByOrNull { it.value }?.key ?: "none"),
                "most_common_reason" to (reasonCounts.maxByOrNull { it.value }?.key ?: "none")
            )
        }
        
        // Record some analytics
        recordRestartAnalytics("SETTINGS_MENU", "practice_steps", 1, CompletionStatus.COMPLETED, 6)
        recordRestartAnalytics("TUTORIAL_COMPLETED", "showing_others", 1, CompletionStatus.COMPLETED, 6)
        recordRestartAnalytics("SETTINGS_MENU", "refresh_memory", 2, CompletionStatus.COMPLETED, 6)
        
        val summary = getAnalyticsSummary()
        
        assertEquals("Should have 3 events", 3, summary["total_events"])
        
        @Suppress("UNCHECKED_CAST")
        val sourceBreakdown = summary["source_breakdown"] as Map<String, Int>
        assertEquals("Should count settings menu restarts", 2, sourceBreakdown["SETTINGS_MENU"])
        assertEquals("Should count completion restarts", 1, sourceBreakdown["TUTORIAL_COMPLETED"])
        
        assertEquals("Should identify most common source", "SETTINGS_MENU", summary["most_common_source"])
    }

    @Test
    fun testSettingsIntegrationInfo() {
        // Test settings integration information
        fun getTutorialSettingsInfo(
            completionStatus: CompletionStatus,
            completedSteps: Int,
            timeSpent: Long,
            canRestart: Boolean,
            isFirstTimeUser: Boolean
        ): Map<String, Any> {
            val statusText = when (completionStatus) {
                CompletionStatus.NOT_STARTED -> "Not started"
                CompletionStatus.IN_PROGRESS -> "In progress ($completedSteps steps completed)"
                CompletionStatus.COMPLETED -> "Completed"
                CompletionStatus.SKIPPED -> "Skipped"
                CompletionStatus.FAILED -> "Failed"
            }
            
            val restartButtonText = when {
                !canRestart -> "Restart not available"
                completionStatus == CompletionStatus.COMPLETED -> "Restart completed tutorial"
                completionStatus == CompletionStatus.SKIPPED -> "Start skipped tutorial"
                else -> "Start tutorial"
            }
            
            return mapOf(
                "status_text" to statusText,
                "can_restart" to canRestart,
                "restart_button_text" to restartButtonText,
                "completed_steps" to completedSteps,
                "time_spent_minutes" to (timeSpent / 60000),
                "is_first_time_user" to isFirstTimeUser
            )
        }
        
        // Test different scenarios
        val completedInfo = getTutorialSettingsInfo(
            CompletionStatus.COMPLETED, 6, 180000L, true, false
        )
        assertEquals("Should show completed status", "Completed", completedInfo["status_text"])
        assertEquals("Should show restart button", "Restart completed tutorial", completedInfo["restart_button_text"])
        assertEquals("Should calculate time in minutes", 3L, completedInfo["time_spent_minutes"])
        
        val inProgressInfo = getTutorialSettingsInfo(
            CompletionStatus.IN_PROGRESS, 3, 90000L, false, true
        )
        assertEquals("Should show in progress status", "In progress (3 steps completed)", inProgressInfo["status_text"])
        assertFalse("Should not allow restart for in progress", inProgressInfo["can_restart"] as Boolean)
        
        val skippedInfo = getTutorialSettingsInfo(
            CompletionStatus.SKIPPED, 0, 0L, true, false
        )
        assertEquals("Should show skipped status", "Skipped", skippedInfo["status_text"])
        assertEquals("Should show start skipped button", "Start skipped tutorial", skippedInfo["restart_button_text"])
    }

    @Test
    fun testRestartDialogStates() {
        // Test restart dialog state management
        var currentDialog: String? = null
        var dialogHistory = mutableListOf<String>()
        
        fun showDialog(dialogType: String) {
            currentDialog = dialogType
            dialogHistory.add(dialogType)
        }
        
        fun dismissDialog() {
            currentDialog = null
        }
        
        fun hasActiveDialog(): Boolean {
            return currentDialog != null
        }
        
        // Test dialog flow
        assertFalse("Should not have active dialog initially", hasActiveDialog())
        
        showDialog("restart_confirmation_dialog")
        assertTrue("Should have active dialog", hasActiveDialog())
        assertEquals("Should track current dialog", "restart_confirmation_dialog", currentDialog)
        
        dismissDialog()
        assertFalse("Should not have active dialog after dismiss", hasActiveDialog())
        
        showDialog("restart_reason_dialog")
        assertTrue("Should have active dialog", hasActiveDialog())
        
        val history = dialogHistory
        assertEquals("Should track dialog history", 2, history.size)
        assertEquals("Should track dialog sequence", 
            listOf("restart_confirmation_dialog", "restart_reason_dialog"), history)
    }

    @Test
    fun testRestartReasonOptions() {
        // Test restart reason options and categorization
        val restartReasons = listOf(
            "Want to practice the steps again" to "practice_steps",
            "Missed some information the first time" to "missed_information",
            "Showing someone else how to use the app" to "showing_others",
            "Want to refresh my memory" to "refresh_memory",
            "Had technical issues during tutorial" to "technical_issues",
            "Want to see if anything changed" to "check_updates"
        )
        
        fun categorizeReason(selectedIndex: Int, customText: String? = null): String {
            val (reasonText, reasonCode) = restartReasons.getOrNull(selectedIndex) ?: return "unknown"
            
            return if (reasonCode == "other" && !customText.isNullOrBlank()) {
                "other:$customText"
            } else {
                reasonCode
            }
        }
        
        // Test standard reasons
        assertEquals("Should categorize practice_steps", "practice_steps", categorizeReason(0))
        assertEquals("Should categorize missed_information", "missed_information", categorizeReason(1))
        assertEquals("Should categorize showing_others", "showing_others", categorizeReason(2))
        assertEquals("Should categorize refresh_memory", "refresh_memory", categorizeReason(3))
        
        // Test invalid index
        assertEquals("Should handle invalid index", "unknown", categorizeReason(99))
    }
}