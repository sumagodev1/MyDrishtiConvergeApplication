package com.mydrishti.co.`in`.activities.tutorial

/**
 * Factory for creating tutorial configurations
 */
object TutorialConfigFactory {
    
    /**
     * Creates the first-time user tutorial configuration
     */
    fun createFirstTimeUserTutorial(): TutorialConfig {
        val steps = listOf(
            // Step 1: Welcome and FAB highlight
            TutorialStep(
                id = TutorialConstants.STEP_WELCOME_FAB,
                title = "Welcome to MyDrishti!",
                description = "Let's create your first chart. Tap the + button to get started.",
                targetViewId = TutorialConstants.VIEW_ID_FAB_ADD_CHART,
                animationType = AnimationType.PULSE,
                duration = 4000L,
                explanatoryText = "The + button allows you to create new charts and visualizations.",
                isSkippable = true,
                validationRequired = false,
                order = 1
            ),
            
            // Step 2: Chart type selection
            TutorialStep(
                id = TutorialConstants.STEP_CHART_TYPE_SELECTION,
                title = "Choose Chart Type",
                description = "Select 'Gauge Chart' for your first visualization.",
                targetViewId = TutorialConstants.VIEW_ID_CARD_GAUGE_CHART,
                animationType = AnimationType.PULSE,
                duration = 4000L,
                explanatoryText = "Gauge charts are perfect for displaying single values with visual indicators.",
                isSkippable = true,
                validationRequired = true,
                order = 2
            ),
            
            // Step 3: Site selection
            TutorialStep(
                id = TutorialConstants.STEP_SITE_SELECTION,
                title = "Select Data Source",
                description = "Choose a site to get data for your chart.",
                targetViewId = TutorialConstants.VIEW_ID_RECYCLER_VIEW_SITES,
                animationType = AnimationType.FADE_SCALE,
                duration = 4000L,
                explanatoryText = "Sites represent different data sources you can visualize in your charts.",
                isSkippable = true,
                validationRequired = true,
                order = 3
            ),
            
            // Step 4: Parameter selection
            TutorialStep(
                id = TutorialConstants.STEP_PARAMETER_SELECTION,
                title = "Configure Parameters",
                description = "Set up the parameters for your chart.",
                targetViewId = null,
                animationType = AnimationType.FADE_SCALE,
                duration = 4000L,
                explanatoryText = "Parameters define what data to display and how to display it.",
                isSkippable = true,
                validationRequired = true,
                order = 4
            ),
            
            // Step 5: Save chart
            TutorialStep(
                id = TutorialConstants.STEP_SAVE_CHART,
                title = "Save Your Chart",
                description = "Tap 'Save' to add your chart to the dashboard.",
                targetViewId = TutorialConstants.VIEW_ID_BTN_SAVE_CHART,
                animationType = AnimationType.PULSE,
                duration = 4000L,
                explanatoryText = "Saving your chart makes it available on your dashboard for future viewing.",
                isSkippable = false,
                validationRequired = true,
                order = 5
            ),
            
            // Step 6: Completion celebration
            TutorialStep(
                id = TutorialConstants.STEP_COMPLETION_CELEBRATION,
                title = "🎉 Congratulations!",
                description = "You've successfully created your first chart!",
                targetViewId = null,
                animationType = AnimationType.CELEBRATION,
                duration = 3000L,
                explanatoryText = "You can now create more charts and explore all the features MyDrishti has to offer.",
                isSkippable = false,
                validationRequired = false,
                order = 6
            )
        )
        
        return TutorialConfig(
            id = "first_time_user_tutorial",
            name = "First Time User Tutorial",
            description = "Guides new users through creating their first chart",
            steps = steps,
            version = TutorialConstants.TUTORIAL_CONFIG_VERSION
        )
    }
    
    /**
     * Creates a minimal tutorial configuration for testing
     */
    fun createTestTutorial(): TutorialConfig {
        val steps = listOf(
            TutorialStep(
                id = "test_step_1",
                title = "Test Step 1",
                description = "This is a test step",
                animationType = AnimationType.FADE,
                duration = 2000L,
                order = 1
            ),
            TutorialStep(
                id = "test_step_2",
                title = "Test Step 2",
                description = "This is another test step",
                animationType = AnimationType.SCALE,
                duration = 2000L,
                order = 2
            )
        )
        
        return TutorialConfig(
            id = "test_tutorial",
            name = "Test Tutorial",
            description = "A simple tutorial for testing purposes",
            steps = steps,
            version = TutorialConstants.TUTORIAL_CONFIG_VERSION
        )
    }
    
    /**
     * Creates an empty tutorial configuration
     */
    fun createEmptyTutorial(): TutorialConfig {
        return TutorialConfig(
            id = "empty_tutorial",
            name = "Empty Tutorial",
            description = "An empty tutorial configuration",
            steps = emptyList(),
            version = TutorialConstants.TUTORIAL_CONFIG_VERSION
        )
    }
}