package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Manages tutorial configuration extensibility
 * Allows adding new tutorial steps and dynamic content loading
 */
class TutorialConfigurationExtension(private val context: Context) {
    
    private var baseConfiguration: TutorialConfig? = null
    private var customSteps = mutableListOf<TutorialStep>()
    private var dynamicContent = mutableMapOf<String, String>()
    private var configurationVersion = "1.0"
    private var backwardCompatibilityEnabled = true
    
    /**
     * Loads base tutorial configuration
     */
    fun loadBaseConfiguration(): TutorialConfig {
        if (baseConfiguration == null) {
            baseConfiguration = TutorialConfigFactory.createFirstTimeUserTutorial()
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Base configuration loaded")
            }
        }
        
        return baseConfiguration!!
    }
    
    /**
     * Adds a custom tutorial step
     */
    fun addCustomStep(step: TutorialStep): Boolean {
        return try {
            // Validate step before adding
            if (validateCustomStep(step)) {
                customSteps.add(step)
                
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Custom step added: ${step.id}")
                }
                
                true
            } else {
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.w(TutorialConstants.DEBUG_LOG_TAG, "Custom step validation failed: ${step.id}")
                }
                
                false
            }
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error adding custom step", e)
            }
            
            false
        }
    }
    
    /**
     * Validates a custom tutorial step
     */
    private fun validateCustomStep(step: TutorialStep): Boolean {
        // Check required fields
        if (step.id.isBlank() || step.title.isBlank() || step.description.isBlank()) {
            return false
        }
        
        // Check for duplicate IDs
        val baseConfig = loadBaseConfiguration()
        if (baseConfig.getStepById(step.id) != null) {
            return false
        }
        
        if (customSteps.any { it.id == step.id }) {
            return false
        }
        
        // Validate animation type
        if (!isValidAnimationType(step.animationType)) {
            return false
        }
        
        // Validate duration
        if (step.duration <= 0) {
            return false
        }
        
        return true
    }
    
    /**
     * Checks if animation type is valid
     */
    private fun isValidAnimationType(animationType: AnimationType): Boolean {
        return try {
            AnimationType.values().contains(animationType)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Removes a custom tutorial step
     */
    fun removeCustomStep(stepId: String): Boolean {
        val removed = customSteps.removeAll { it.id == stepId }
        
        if (removed && TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Custom step removed: $stepId")
        }
        
        return removed
    }
    
    /**
     * Gets all custom steps
     */
    fun getCustomSteps(): List<TutorialStep> {
        return customSteps.toList()
    }
    
    /**
     * Creates extended configuration with custom steps
     */
    fun createExtendedConfiguration(): TutorialConfig {
        val baseConfig = loadBaseConfiguration()
        val allSteps = mutableListOf<TutorialStep>()
        
        // Add base steps
        allSteps.addAll(baseConfig.steps)
        
        // Add custom steps
        allSteps.addAll(customSteps)
        
        // Sort steps by order if specified
        allSteps.sortBy { it.order ?: Int.MAX_VALUE }
        
        return TutorialConfig(
            id = "${baseConfig.id}_extended",
            name = "${baseConfig.name} (Extended)",
            description = "${baseConfig.description} with custom steps",
            steps = allSteps,
            version = configurationVersion
        )
    }
    
    /**
     * Loads dynamic content from assets or network
     */
    fun loadDynamicContent(contentKey: String, source: ContentSource = ContentSource.ASSETS): String? {
        return try {
            when (source) {
                ContentSource.ASSETS -> loadContentFromAssets(contentKey)
                ContentSource.NETWORK -> loadContentFromNetwork(contentKey)
                ContentSource.CACHE -> dynamicContent[contentKey]
            }
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error loading dynamic content: $contentKey", e)
            }
            
            null
        }
    }
    
    /**
     * Loads content from assets
     */
    private fun loadContentFromAssets(contentKey: String): String? {
        return try {
            val fileName = "tutorial_content_$contentKey.json"
            val inputStream = context.assets.open(fileName)
            val content = inputStream.bufferedReader().use { it.readText() }
            
            // Cache the content
            dynamicContent[contentKey] = content
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Content loaded from assets: $contentKey")
            }
            
            content
        } catch (e: IOException) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.w(TutorialConstants.DEBUG_LOG_TAG, "Content not found in assets: $contentKey")
            }
            
            null
        }
    }
    
    /**
     * Loads content from network (placeholder implementation)
     */
    private fun loadContentFromNetwork(contentKey: String): String? {
        // This would implement actual network loading
        // For now, return null to indicate network loading not implemented
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Network content loading not implemented: $contentKey")
        }
        
        return null
    }
    
    /**
     * Caches dynamic content
     */
    fun cacheDynamicContent(contentKey: String, content: String) {
        dynamicContent[contentKey] = content
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Content cached: $contentKey")
        }
    }
    
    /**
     * Parses JSON content into tutorial steps
     */
    fun parseStepsFromJson(jsonContent: String): List<TutorialStep> {
        val steps = mutableListOf<TutorialStep>()
        
        try {
            val jsonObject = JSONObject(jsonContent)
            val stepsArray = jsonObject.getJSONArray("steps")
            
            for (i in 0 until stepsArray.length()) {
                val stepJson = stepsArray.getJSONObject(i)
                val step = parseStepFromJson(stepJson)
                
                if (step != null) {
                    steps.add(step)
                }
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Parsed ${steps.size} steps from JSON")
            }
            
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error parsing steps from JSON", e)
            }
        }
        
        return steps
    }
    
    /**
     * Parses a single step from JSON
     */
    private fun parseStepFromJson(stepJson: JSONObject): TutorialStep? {
        return try {
            val id = stepJson.getString("id")
            val title = stepJson.getString("title")
            val description = stepJson.getString("description")
            val targetViewId = stepJson.optString("targetViewId", null)
            val animationType = AnimationType.valueOf(stepJson.optString("animationType", "FADE_SCALE"))
            val duration = stepJson.optLong("duration", 3000L)
            val explanatoryText = stepJson.optString("explanatoryText", null)
            val isSkippable = stepJson.optBoolean("isSkippable", true)
            val validationRequired = stepJson.optBoolean("validationRequired", false)
            val order = stepJson.optInt("order", Int.MAX_VALUE)
            
            TutorialStep(
                id = id,
                title = title,
                description = description,
                targetViewId = targetViewId,
                animationType = animationType,
                duration = duration,
                explanatoryText = explanatoryText,
                isSkippable = isSkippable,
                validationRequired = validationRequired,
                order = order
            )
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error parsing step from JSON", e)
            }
            
            null
        }
    }
    
    /**
     * Exports configuration to JSON
     */
    fun exportConfigurationToJson(): String {
        return try {
            val config = createExtendedConfiguration()
            val jsonObject = JSONObject()
            
            jsonObject.put("id", config.id)
            jsonObject.put("name", config.name)
            jsonObject.put("description", config.description)
            jsonObject.put("version", config.version)
            
            val stepsArray = JSONArray()
            config.steps.forEach { step ->
                val stepJson = JSONObject()
                stepJson.put("id", step.id)
                stepJson.put("title", step.title)
                stepJson.put("description", step.description)
                stepJson.put("targetViewId", step.targetViewId)
                stepJson.put("animationType", step.animationType.name)
                stepJson.put("duration", step.duration)
                stepJson.put("explanatoryText", step.explanatoryText)
                stepJson.put("isSkippable", step.isSkippable)
                stepJson.put("validationRequired", step.validationRequired)
                stepJson.put("order", step.order)
                
                stepsArray.put(stepJson)
            }
            
            jsonObject.put("steps", stepsArray)
            
            jsonObject.toString(2) // Pretty print with 2-space indentation
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error exporting configuration to JSON", e)
            }
            
            "{\"error\": \"Failed to export configuration\"}"
        }
    }
    
    /**
     * Imports configuration from JSON
     */
    fun importConfigurationFromJson(jsonContent: String): Boolean {
        return try {
            val steps = parseStepsFromJson(jsonContent)
            
            // Clear existing custom steps
            customSteps.clear()
            
            // Add parsed steps as custom steps
            steps.forEach { step ->
                if (validateCustomStep(step)) {
                    customSteps.add(step)
                }
            }
            
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.d(TutorialConstants.DEBUG_LOG_TAG, "Configuration imported: ${customSteps.size} custom steps")
            }
            
            true
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error importing configuration from JSON", e)
            }
            
            false
        }
    }
    
    /**
     * Handles backward compatibility
     */
    fun handleBackwardCompatibility(oldVersion: String, newVersion: String): Boolean {
        if (!backwardCompatibilityEnabled) {
            return false
        }
        
        return try {
            when {
                oldVersion == "1.0" && newVersion == "1.1" -> {
                    // Handle 1.0 to 1.1 migration
                    migrateFrom1_0To1_1()
                }
                oldVersion == "1.1" && newVersion == "1.2" -> {
                    // Handle 1.1 to 1.2 migration
                    migrateFrom1_1To1_2()
                }
                else -> {
                    // Generic migration
                    performGenericMigration(oldVersion, newVersion)
                }
            }
        } catch (e: Exception) {
            if (TutorialConstants.DEBUG_MODE_ENABLED) {
                Log.e(TutorialConstants.DEBUG_LOG_TAG, "Error handling backward compatibility", e)
            }
            
            false
        }
    }
    
    /**
     * Migrates from version 1.0 to 1.1
     */
    private fun migrateFrom1_0To1_1(): Boolean {
        // Add default order to steps that don't have it
        customSteps.forEachIndexed { index, step ->
            if (step.order == null) {
                val updatedStep = step.copy(order = index + 1000) // Start custom steps at 1000
                customSteps[index] = updatedStep
            }
        }
        
        configurationVersion = "1.1"
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Migrated from 1.0 to 1.1")
        }
        
        return true
    }
    
    /**
     * Migrates from version 1.1 to 1.2
     */
    private fun migrateFrom1_1To1_2(): Boolean {
        // Add validation requirements to steps that need it
        customSteps.forEachIndexed { index, step ->
            if (step.id.contains("parameter") || step.id.contains("save")) {
                val updatedStep = step.copy(validationRequired = true)
                customSteps[index] = updatedStep
            }
        }
        
        configurationVersion = "1.2"
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Migrated from 1.1 to 1.2")
        }
        
        return true
    }
    
    /**
     * Performs generic migration
     */
    private fun performGenericMigration(oldVersion: String, newVersion: String): Boolean {
        // Generic migration logic
        configurationVersion = newVersion
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Generic migration from $oldVersion to $newVersion")
        }
        
        return true
    }
    
    /**
     * Validates configuration compatibility
     */
    fun validateConfigurationCompatibility(config: TutorialConfig): List<String> {
        val issues = mutableListOf<String>()
        
        // Check version compatibility
        if (config.version != configurationVersion && !backwardCompatibilityEnabled) {
            issues.add("Version mismatch: expected $configurationVersion, got ${config.version}")
        }
        
        // Check for duplicate step IDs
        val stepIds = config.steps.map { it.id }
        val duplicates = stepIds.groupBy { it }.filter { it.value.size > 1 }.keys
        if (duplicates.isNotEmpty()) {
            issues.add("Duplicate step IDs found: ${duplicates.joinToString(", ")}")
        }
        
        // Check for invalid steps
        config.steps.forEach { step ->
            if (!step.isValid()) {
                issues.add("Invalid step: ${step.id}")
            }
        }
        
        return issues
    }
    
    /**
     * Gets configuration analytics
     */
    fun getConfigurationAnalytics(): Map<String, Any> {
        val baseConfig = loadBaseConfiguration()
        
        return mapOf(
            "base_steps_count" to baseConfig.steps.size,
            "custom_steps_count" to customSteps.size,
            "total_steps_count" to (baseConfig.steps.size + customSteps.size),
            "dynamic_content_count" to dynamicContent.size,
            "configuration_version" to configurationVersion,
            "backward_compatibility_enabled" to backwardCompatibilityEnabled,
            "custom_step_ids" to customSteps.map { it.id },
            "dynamic_content_keys" to dynamicContent.keys.toList()
        )
    }
    
    /**
     * Resets configuration to base state
     */
    fun resetToBaseConfiguration() {
        customSteps.clear()
        dynamicContent.clear()
        configurationVersion = "1.0"
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Configuration reset to base state")
        }
    }
    
    /**
     * Enables or disables backward compatibility
     */
    fun setBackwardCompatibilityEnabled(enabled: Boolean) {
        backwardCompatibilityEnabled = enabled
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Backward compatibility ${if (enabled) "enabled" else "disabled"}")
        }
    }
    
    /**
     * Gets current configuration version
     */
    fun getConfigurationVersion(): String = configurationVersion
    
    /**
     * Sets configuration version
     */
    fun setConfigurationVersion(version: String) {
        configurationVersion = version
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Configuration version set to: $version")
        }
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        customSteps.clear()
        dynamicContent.clear()
        baseConfiguration = null
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Configuration extension cleaned up")
        }
    }
}

/**
 * Enum for content sources
 */
enum class ContentSource {
    ASSETS,
    NETWORK,
    CACHE
}