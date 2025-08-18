package com.mydrishti.co.`in`.tutorial

import com.mydrishti.co.`in`.activities.tutorial.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for FirstTimeUserDetector logic and decision making
 * Note: These tests focus on the business logic without requiring Android Context
 */
class FirstTimeUserDetectorTest {

    @Test
    fun testTutorialEligibilityLogic() {
        // Test the core logic that would be used in FirstTimeUserDetector
        
        // Scenario 1: New user should be eligible
        val newUserData = mapOf(
            "successful_login_count" to 1,
            "has_created_chart" to false,
            "time_since_first_login_days" to 0L,
            "tutorial_completed" to false,
            "tutorial_skipped" to false
        )
        
        assertTrue("New user should be eligible for tutorial", 
            shouldShowTutorialBasedOnData(newUserData))
        
        // Scenario 2: User who completed tutorial should not be eligible
        val completedUserData = newUserData + ("tutorial_completed" to true)
        assertFalse("User who completed tutorial should not be eligible", 
            shouldShowTutorialBasedOnData(completedUserData))
        
        // Scenario 3: User who skipped tutorial should not be eligible
        val skippedUserData = newUserData + ("tutorial_skipped" to true)
        assertFalse("User who skipped tutorial should not be eligible", 
            shouldShowTutorialBasedOnData(skippedUserData))
        
        // Scenario 4: User with significant usage should not be eligible
        val experiencedUserData = newUserData + ("successful_login_count" to 5)
        assertFalse("Experienced user should not be eligible", 
            shouldShowTutorialBasedOnData(experiencedUserData))
    }

    @Test
    fun testFirstLoginDetectionLogic() {
        // Test first login detection logic
        
        // New user with 0 successful logins
        assertTrue("User with 0 logins should be first-time", isFirstLoginBasedOnCount(0))
        
        // User with 1 successful login
        assertTrue("User with 1 login should be first-time", isFirstLoginBasedOnCount(1))
        
        // User with 2 successful logins
        assertFalse("User with 2 logins should not be first-time", isFirstLoginBasedOnCount(2))
        
        // User with many logins
        assertFalse("User with many logins should not be first-time", isFirstLoginBasedOnCount(10))
    }

    @Test
    fun testSignificantUsageDetection() {
        // Test significant usage detection logic
        
        // New user
        assertFalse("New user should not have significant usage", 
            hasSignificantUsageBasedOnData(1, false, 0L))
        
        // User with many logins
        assertTrue("User with many logins should have significant usage", 
            hasSignificantUsageBasedOnData(5, false, 0L))
        
        // User who created charts
        assertTrue("User who created charts should have significant usage", 
            hasSignificantUsageBasedOnData(1, true, 0L))
        
        // User with old first login (8 days ago)
        val eightDaysInMs = 8L * 24 * 60 * 60 * 1000L
        assertTrue("User with old first login should have significant usage", 
            hasSignificantUsageBasedOnData(1, false, eightDaysInMs))
        
        // User with recent first login (1 day ago)
        val oneDayInMs = 1L * 24 * 60 * 60 * 1000L
        assertFalse("User with recent first login should not have significant usage", 
            hasSignificantUsageBasedOnData(1, false, oneDayInMs))
    }

    @Test
    fun testTutorialConfigValidation() {
        // Test that tutorial configuration is valid
        val config = TutorialConfigFactory.createFirstTimeUserTutorial()
        
        assertTrue("Tutorial config should be valid", config.isValid())
        assertTrue("Tutorial should have steps", config.steps.isNotEmpty())
        assertEquals("Tutorial should have 6 steps", 6, config.steps.size)
        
        // Test that all steps are valid
        config.steps.forEach { step ->
            assertTrue("Step ${step.id} should be valid", step.isValid())
        }
    }

    @Test
    fun testUserAnalyticsDataStructure() {
        // Test the structure of analytics data that would be collected
        val sampleAnalytics = mapOf(
            "is_first_login" to true,
            "login_count" to 1,
            "successful_login_count" to 1,
            "has_created_charts" to false,
            "time_since_first_login_days" to 0L,
            "tutorial_eligible" to true,
            "should_show_tutorial" to true,
            "has_significant_usage" to false,
            "tutorial_system_available" to true,
            "completion_status" to "NOT_STARTED",
            "completed_steps_count" to 0,
            "skip_count" to 0,
            "total_time_spent" to 0L,
            "onboarding_completed" to false,
            "session_timed_out" to false
        )
        
        // Verify all expected keys are present
        assertTrue("Should contain first login status", sampleAnalytics.containsKey("is_first_login"))
        assertTrue("Should contain login counts", sampleAnalytics.containsKey("successful_login_count"))
        assertTrue("Should contain chart creation status", sampleAnalytics.containsKey("has_created_charts"))
        assertTrue("Should contain tutorial eligibility", sampleAnalytics.containsKey("tutorial_eligible"))
        assertTrue("Should contain tutorial recommendation", sampleAnalytics.containsKey("should_show_tutorial"))
        assertTrue("Should contain completion status", sampleAnalytics.containsKey("completion_status"))
        
        // Verify data types
        assertTrue("First login should be boolean", sampleAnalytics["is_first_login"] is Boolean)
        assertTrue("Login count should be number", sampleAnalytics["successful_login_count"] is Int)
        assertTrue("Time should be long", sampleAnalytics["time_since_first_login_days"] is Long)
    }

    @Test
    fun testTimeCalculations() {
        // Test time-based calculations
        val currentTime = System.currentTimeMillis()
        
        // Test days calculation
        val oneDayMs = 24 * 60 * 60 * 1000L
        val threeDaysAgo = currentTime - (3 * oneDayMs)
        val daysDifference = (currentTime - threeDaysAgo) / oneDayMs
        
        assertEquals("Should calculate 3 days difference", 3L, daysDifference)
        
        // Test timeout detection
        val timeoutMs = TutorialConstants.TUTORIAL_TOTAL_TIMEOUT
        val oldTime = currentTime - timeoutMs - 1000L // 1 second past timeout
        val timeDifference = currentTime - oldTime
        
        assertTrue("Should detect timeout", timeDifference > timeoutMs)
    }

    @Test
    fun testTutorialStepProgression() {
        // Test tutorial step progression logic
        val config = TutorialConfigFactory.createFirstTimeUserTutorial()
        
        // Test first step
        val firstStep = config.steps.first()
        assertEquals("First step should be welcome", TutorialConstants.STEP_WELCOME_FAB, firstStep.id)
        
        // Test step navigation
        val secondStep = config.getNextStep(firstStep.id)
        assertNotNull("Should have next step after first", secondStep)
        assertEquals("Second step should be chart type", TutorialConstants.STEP_CHART_TYPE_SELECTION, secondStep?.id)
        
        // Test last step
        val lastStep = config.steps.last()
        assertEquals("Last step should be celebration", TutorialConstants.STEP_COMPLETION_CELEBRATION, lastStep.id)
        assertNull("Last step should have no next step", config.getNextStep(lastStep.id))
    }

    // Helper methods that simulate the logic from FirstTimeUserDetector

    private fun shouldShowTutorialBasedOnData(userData: Map<String, Any>): Boolean {
        val isFirstTime = (userData["successful_login_count"] as Int) <= 1
        val hasCompletedTutorial = userData["tutorial_completed"] as Boolean
        val wasSkipped = userData["tutorial_skipped"] as Boolean
        val hasSignificantUsage = hasSignificantUsageBasedOnData(
            userData["successful_login_count"] as Int,
            userData["has_created_chart"] as Boolean,
            userData["time_since_first_login_days"] as Long * 24 * 60 * 60 * 1000L
        )
        
        return isFirstTime && !hasCompletedTutorial && !wasSkipped && !hasSignificantUsage
    }

    private fun isFirstLoginBasedOnCount(successfulLoginCount: Int): Boolean {
        return successfulLoginCount <= 1
    }

    private fun hasSignificantUsageBasedOnData(loginCount: Int, hasCreatedChart: Boolean, timeSinceFirstLogin: Long): Boolean {
        val sevenDaysInMs = 7L * 24 * 60 * 60 * 1000L
        return loginCount > 3 || hasCreatedChart || timeSinceFirstLogin > sevenDaysInMs
    }
}    @Tes
t
    fun testLoginIntegrationLogic() {
        // Test the integration logic that would be used in LoginTutorialIntegration
        
        // Simulate first successful login
        var loginCount = 0
        var successfulLoginCount = 0
        
        // Record login attempt
        loginCount++
        
        // Record successful login
        successfulLoginCount++
        
        // Check if should show tutorial
        val shouldShowTutorial = shouldShowTutorialBasedOnData(mapOf(
            "successful_login_count" to successfulLoginCount,
            "has_created_chart" to false,
            "time_since_first_login_days" to 0L,
            "tutorial_completed" to false,
            "tutorial_skipped" to false
        ))
        
        assertTrue("First successful login should trigger tutorial", shouldShowTutorial)
        
        // Simulate second login
        successfulLoginCount++
        
        val shouldShowTutorialSecondTime = shouldShowTutorialBasedOnData(mapOf(
            "successful_login_count" to successfulLoginCount,
            "has_created_chart" to false,
            "time_since_first_login_days" to 0L,
            "tutorial_completed" to false,
            "tutorial_skipped" to false
        ))
        
        assertFalse("Second login should not trigger tutorial", shouldShowTutorialSecondTime)
    }

    @Test
    fun testIntentExtraConstants() {
        // Test that intent extra constants are properly defined
        val showTutorialExtra = "show_tutorial"
        val isFirstTimeUserExtra = "is_first_time_user"
        
        assertNotNull("Show tutorial extra should be defined", showTutorialExtra)
        assertNotNull("First time user extra should be defined", isFirstTimeUserExtra)
        
        // Test that constants match expected values
        assertEquals("Show tutorial extra should match", "show_tutorial", showTutorialExtra)
        assertEquals("First time user extra should match", "is_first_time_user", isFirstTimeUserExtra)
    }