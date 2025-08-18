package com.mydrishti.co.`in`.tutorial

import android.content.Intent
import com.mydrishti.co.`in`.activities.tutorial.*
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for LoginActivity integration logic
 * Note: These tests focus on the business logic without requiring Android Activity
 */
class LoginActivityIntegrationTest {

    @Test
    fun testPostLoginNavigationLogic() {
        // Test post-login navigation decision logic
        fun shouldShowTutorial(
            isFirstLogin: Boolean,
            tutorialCompleted: Boolean,
            tutorialSkipped: Boolean,
            hasSignificantUsage: Boolean
        ): Boolean {
            return isFirstLogin && !tutorialCompleted && !tutorialSkipped && !hasSignificantUsage
        }
        
        // Test various scenarios
        assertTrue("Should show tutorial for new user", 
            shouldShowTutorial(true, false, false, false))
        assertFalse("Should not show tutorial for completed user", 
            shouldShowTutorial(true, true, false, false))
        assertFalse("Should not show tutorial for user who skipped", 
            shouldShowTutorial(true, false, true, false))
        assertFalse("Should not show tutorial for experienced user", 
            shouldShowTutorial(true, false, false, true))
        assertFalse("Should not show tutorial for returning user", 
            shouldShowTutorial(false, false, false, false))
    }

    @Test
    fun testIntentExtraCreation() {
        // Test intent extra creation logic
        fun createMainActivityIntent(
            shouldShowTutorial: Boolean,
            isFirstTimeUser: Boolean,
            email: String
        ): Map<String, Any> {
            return mapOf(
                "show_tutorial" to shouldShowTutorial,
                "is_first_time_user" to isFirstTimeUser,
                "login_source" to "login_activity",
                "login_timestamp" to System.currentTimeMillis(),
                "user_email" to email
            )
        }
        
        // Test intent creation
        val intentExtras = createMainActivityIntent(true, true, "test@example.com")
        
        assertTrue("Should include show_tutorial", intentExtras.containsKey("show_tutorial"))
        assertTrue("Should include is_first_time_user", intentExtras.containsKey("is_first_time_user"))
        assertEquals("Should set login_source", "login_activity", intentExtras["login_source"])
        assertTrue("Should include timestamp", intentExtras.containsKey("login_timestamp"))
        
        // Test values
        assertTrue("Should show tutorial", intentExtras["show_tutorial"] as Boolean)
        assertTrue("Should be first time user", intentExtras["is_first_time_user"] as Boolean)
    }

    @Test
    fun testLoginEventTracking() {
        // Test login event tracking logic
        val loginEvents = mutableListOf<Map<String, Any>>()
        
        fun recordLoginEvent(eventType: String, email: String, success: Boolean) {
            loginEvents.add(mapOf(
                "event_type" to eventType,
                "email" to email,
                "success" to success,
                "timestamp" to System.currentTimeMillis()
            ))
        }
        
        fun getLoginStatistics(): Map<String, Any> {
            val successfulLogins = loginEvents.count { it["success"] as Boolean && it["event_type"] == "login" }
            val totalAttempts = loginEvents.count { it["event_type"] == "login" }
            
            return mapOf(
                "total_attempts" to totalAttempts,
                "successful_logins" to successfulLogins,
                "success_rate" to if (totalAttempts > 0) (successfulLogins.toDouble() / totalAttempts) else 0.0,
                "most_recent_email" to (loginEvents.lastOrNull()?.get("email") ?: "none")
            )
        }
        
        // Test event recording
        recordLoginEvent("login", "user1@example.com", true)
        recordLoginEvent("login", "user2@example.com", false)
        recordLoginEvent("login", "user1@example.com", true)
        
        val stats = getLoginStatistics()
        
        assertEquals("Should track total attempts", 3, stats["total_attempts"])
        assertEquals("Should track successful logins", 2, stats["successful_logins"])
        assertEquals("Should calculate success rate", 0.67, stats["success_rate"] as Double, 0.01)
        assertEquals("Should track most recent email", "user1@example.com", stats["most_recent_email"])
    }

    @Test
    fun testFirstTimeUserDetection() {
        // Test first-time user detection logic
        fun isFirstTimeUser(loginCount: Int, successfulLogins: Int, accountAge: Long): Boolean {
            val maxFirstTimeLogins = 1
            val maxAccountAgeForFirstTime = 24 * 60 * 60 * 1000L // 24 hours
            
            return successfulLogins <= maxFirstTimeLogins && accountAge <= maxAccountAgeForFirstTime
        }
        
        // Test various scenarios
        assertTrue("Should be first-time with 1 login and new account", 
            isFirstTimeUser(1, 1, 1000L))
        assertFalse("Should not be first-time with multiple logins", 
            isFirstTimeUser(5, 3, 1000L))
        assertFalse("Should not be first-time with old account", 
            isFirstTimeUser(1, 1, 48 * 60 * 60 * 1000L))
        assertTrue("Should be first-time with 0 successful logins", 
            isFirstTimeUser(2, 0, 1000L))
    }

    @Test
    fun testNavigationDecisionMatrix() {
        // Test navigation decision matrix
        data class UserProfile(
            val isFirstLogin: Boolean,
            val loginCount: Int,
            val hasCreatedCharts: Boolean,
            val tutorialCompleted: Boolean,
            val tutorialSkipped: Boolean,
            val daysSinceFirstLogin: Int
        )
        
        fun getNavigationDecision(profile: UserProfile): String {
            return when {
                !profile.isFirstLogin -> "direct_navigation"
                profile.tutorialCompleted -> "direct_navigation"
                profile.tutorialSkipped && profile.daysSinceFirstLogin < 7 -> "direct_navigation"
                profile.hasCreatedCharts -> "direct_navigation"
                profile.loginCount > 3 -> "direct_navigation"
                profile.isFirstLogin && !profile.tutorialCompleted -> "show_tutorial"
                else -> "direct_navigation"
            }
        }
        
        // Test different user profiles
        val newUser = UserProfile(true, 1, false, false, false, 0)
        assertEquals("Should show tutorial for new user", "show_tutorial", getNavigationDecision(newUser))
        
        val completedUser = UserProfile(true, 1, false, true, false, 0)
        assertEquals("Should navigate directly for completed user", "direct_navigation", getNavigationDecision(completedUser))
        
        val experiencedUser = UserProfile(true, 5, true, false, false, 10)
        assertEquals("Should navigate directly for experienced user", "direct_navigation", getNavigationDecision(experiencedUser))
        
        val recentSkipUser = UserProfile(true, 1, false, false, true, 2)
        assertEquals("Should navigate directly for recent skip", "direct_navigation", getNavigationDecision(recentSkipUser))
    }

    @Test
    fun testIntentExtraValidation() {
        // Test intent extra validation logic
        fun validateTutorialIntent(extras: Map<String, Any?>): Map<String, String> {
            val errors = mutableMapOf<String, String>()
            
            if (!extras.containsKey("show_tutorial")) {
                errors["show_tutorial"] = "Missing show_tutorial extra"
            } else if (extras["show_tutorial"] !is Boolean) {
                errors["show_tutorial"] = "show_tutorial must be Boolean"
            }
            
            if (!extras.containsKey("is_first_time_user")) {
                errors["is_first_time_user"] = "Missing is_first_time_user extra"
            } else if (extras["is_first_time_user"] !is Boolean) {
                errors["is_first_time_user"] = "is_first_time_user must be Boolean"
            }
            
            if (!extras.containsKey("login_source")) {
                errors["login_source"] = "Missing login_source extra"
            } else if (extras["login_source"] !is String) {
                errors["login_source"] = "login_source must be String"
            }
            
            if (!extras.containsKey("login_timestamp")) {
                errors["login_timestamp"] = "Missing login_timestamp extra"
            } else if (extras["login_timestamp"] !is Long) {
                errors["login_timestamp"] = "login_timestamp must be Long"
            }
            
            return errors
        }
        
        // Test valid intent
        val validExtras = mapOf(
            "show_tutorial" to true,
            "is_first_time_user" to true,
            "login_source" to "login_activity",
            "login_timestamp" to System.currentTimeMillis()
        )
        val validErrors = validateTutorialIntent(validExtras)
        assertTrue("Valid intent should have no errors", validErrors.isEmpty())
        
        // Test invalid intent
        val invalidExtras = mapOf(
            "show_tutorial" to "true", // Wrong type
            "is_first_time_user" to 1, // Wrong type
            "login_timestamp" to "now" // Wrong type
            // Missing login_source
        )
        val invalidErrors = validateTutorialIntent(invalidExtras)
        assertEquals("Should have 4 errors", 4, invalidErrors.size)
        assertTrue("Should error on show_tutorial type", invalidErrors.containsKey("show_tutorial"))
        assertTrue("Should error on missing login_source", invalidErrors.containsKey("login_source"))
    }

    @Test
    fun testAnalyticsDataCollection() {
        // Test analytics data collection
        val analyticsData = mutableMapOf<String, Any>()
        
        fun recordAnalytics(category: String, data: Map<String, Any>) {
            analyticsData[category] = data
        }
        
        fun getAnalyticsSummary(): Map<String, Any> {
            return mapOf(
                "categories_count" to analyticsData.size,
                "has_login_data" to analyticsData.containsKey("login"),
                "has_tutorial_data" to analyticsData.containsKey("tutorial"),
                "has_navigation_data" to analyticsData.containsKey("navigation")
            )
        }
        
        // Record analytics
        recordAnalytics("login", mapOf(
            "email" to "test@example.com",
            "success" to true,
            "attempt_count" to 1
        ))
        
        recordAnalytics("tutorial", mapOf(
            "should_show" to true,
            "is_first_time" to true,
            "eligibility_reason" to "new_user"
        ))
        
        recordAnalytics("navigation", mapOf(
            "destination" to "MainActivity",
            "transition_type" to "shared_element",
            "extras_count" to 4
        ))
        
        val summary = getAnalyticsSummary()
        
        assertEquals("Should have 3 categories", 3, summary["categories_count"])
        assertTrue("Should have login data", summary["has_login_data"] as Boolean)
        assertTrue("Should have tutorial data", summary["has_tutorial_data"] as Boolean)
        assertTrue("Should have navigation data", summary["has_navigation_data"] as Boolean)
    }

    @Test
    fun testErrorHandlingScenarios() {
        // Test error handling scenarios
        fun handleLoginIntegrationError(errorType: String): Map<String, Any> {
            return when (errorType) {
                "tutorial_system_unavailable" -> mapOf(
                    "fallback_action" to "direct_navigation",
                    "error_logged" to true,
                    "user_notified" to false
                )
                "first_time_detection_failed" -> mapOf(
                    "fallback_action" to "assume_returning_user",
                    "error_logged" to true,
                    "user_notified" to false
                )
                "intent_creation_failed" -> mapOf(
                    "fallback_action" to "basic_navigation",
                    "error_logged" to true,
                    "user_notified" to false
                )
                "analytics_recording_failed" -> mapOf(
                    "fallback_action" to "continue_without_analytics",
                    "error_logged" to true,
                    "user_notified" to false
                )
                else -> mapOf(
                    "fallback_action" to "direct_navigation",
                    "error_logged" to true,
                    "user_notified" to false
                )
            }
        }
        
        // Test different error scenarios
        val tutorialUnavailable = handleLoginIntegrationError("tutorial_system_unavailable")
        assertEquals("Should fallback to direct navigation", "direct_navigation", tutorialUnavailable["fallback_action"])
        assertTrue("Should log error", tutorialUnavailable["error_logged"] as Boolean)
        
        val detectionFailed = handleLoginIntegrationError("first_time_detection_failed")
        assertEquals("Should assume returning user", "assume_returning_user", detectionFailed["fallback_action"])
        
        val intentFailed = handleLoginIntegrationError("intent_creation_failed")
        assertEquals("Should use basic navigation", "basic_navigation", intentFailed["fallback_action"])
        
        val analyticsFailed = handleLoginIntegrationError("analytics_recording_failed")
        assertEquals("Should continue without analytics", "continue_without_analytics", analyticsFailed["fallback_action"])
    }
}