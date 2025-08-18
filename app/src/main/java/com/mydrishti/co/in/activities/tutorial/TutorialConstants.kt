package com.mydrishti.co.`in`.activities.tutorial

/**
 * Constants used throughout the tutorial system
 */
object TutorialConstants {
    
    // Debug configuration
    const val DEBUG_MODE_ENABLED = true
    const val DEBUG_LOG_TAG = "TutorialSystem"
    
    // Tutorial step IDs
    const val STEP_WELCOME_FAB = "welcome_fab"
    const val STEP_CHART_TYPE_SELECTION = "chart_type_selection"
    const val STEP_SITE_SELECTION = "site_selection"
    const val STEP_PARAMETER_SELECTION = "parameter_selection"
    const val STEP_SAVE_CHART = "save_chart"
    const val STEP_COMPLETION_CELEBRATION = "completion_celebration"
    const val STEP_TUTORIAL_COMPLETION = "tutorial_completion"
    
    // Animation durations
    const val ANIMATION_DURATION_SHORT = 300L
    const val ANIMATION_DURATION_MEDIUM = 500L
    const val ANIMATION_DURATION_LONG = 1000L
    const val ANIMATION_DURATION_CELEBRATION = 2000L
    
    // Tutorial configuration
    const val TUTORIAL_CONFIG_VERSION = "1.0"
    const val MAX_TUTORIAL_STEPS = 10
    const val TUTORIAL_TIMEOUT_MS = 30000L // 30 seconds
    
    // SharedPreferences keys
    const val PREF_TUTORIAL_COMPLETED = "tutorial_completed"
    const val PREF_TUTORIAL_SKIPPED = "tutorial_skipped"
    const val PREF_FIRST_TIME_USER = "first_time_user"
    const val PREF_TUTORIAL_STEP_COMPLETED = "tutorial_step_completed_"
    const val PREF_TUTORIAL_VERSION = "tutorial_version"
    
    // View IDs (these should match your actual layout IDs)
    const val VIEW_ID_FAB_ADD_CHART = "fab_add_chart"
    const val VIEW_ID_CARD_GAUGE_CHART = "card_gauge_chart"
    const val VIEW_ID_RECYCLER_VIEW_SITES = "recyclerViewSites"
    const val VIEW_ID_BTN_SAVE_CHART = "btnSaveChart"
    
    // Error messages
    const val ERROR_TUTORIAL_MANAGER_NULL = "TutorialManager is null"
    const val ERROR_TARGET_VIEW_NOT_FOUND = "Target view not found"
    const val ERROR_ANIMATION_FAILED = "Animation failed"
    const val ERROR_CONFIGURATION_INVALID = "Configuration is invalid"
    
    // Tutorial flow states
    const val STATE_NOT_STARTED = "not_started"
    const val STATE_IN_PROGRESS = "in_progress"
    const val STATE_COMPLETED = "completed"
    const val STATE_SKIPPED = "skipped"
    const val STATE_ERROR = "error"
}