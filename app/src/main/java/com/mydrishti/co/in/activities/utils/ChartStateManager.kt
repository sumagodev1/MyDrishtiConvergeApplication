package com.mydrishti.co.`in`.activities.utils

/**
 * Utility class to manage chart states across configuration changes
 * Maintains selected dates and chart configurations to prevent reset during orientation changes
 */
object ChartStateManager {
    // Map of chart ID to selected month and year
    private val selectedMonths = mutableMapOf<String, Pair<Int, Int>>() // chart base ID -> (month, year)
    
    // Map of chart ID to selected day information
    private val selectedDays = mutableMapOf<String, Triple<Int, Int, Int>>() // chart base ID -> (day, month, year)
    
    // Flag to track if charts were refreshed via SwipeRefreshLayout
    private var wasRefreshedViaSwipe = false
    
    /**
     * Save selected month for a chart
     * @param baseChartId The base chart ID (without date suffix)
     * @param month Selected month (0-11)
     * @param year Selected year
     */
    fun saveSelectedMonth(baseChartId: String, month: Int, year: Int) {
        selectedMonths[baseChartId] = Pair(month, year)
    }
    
    /**
     * Get saved month selection for a chart
     * @param baseChartId The base chart ID (without date suffix)
     * @return Pair<month, year> or null if no selection saved
     */
    fun getSelectedMonth(baseChartId: String): Pair<Int, Int>? {
        // If charts were refreshed via swipe, don't return saved state
        // This forces charts to show the latest data
        if (wasRefreshedViaSwipe) {
            return null
        }
        return selectedMonths[baseChartId]
    }
    
    /**
     * Save selected day for a chart
     * @param baseChartId The base chart ID (without date suffix)
     * @param day Selected day
     * @param month Selected month (0-11)
     * @param year Selected year
     */
    fun saveSelectedDay(baseChartId: String, day: Int, month: Int, year: Int) {
        selectedDays[baseChartId] = Triple(day, month, year)
    }
    
    /**
     * Get saved day selection for a chart
     * @param baseChartId The base chart ID (without date suffix)
     * @return Triple<day, month, year> or null if no selection saved
     */
    fun getSelectedDay(baseChartId: String): Triple<Int, Int, Int>? {
        // If charts were refreshed via swipe, don't return saved state
        // This forces charts to show the latest data
        if (wasRefreshedViaSwipe) {
            return null
        }
        return selectedDays[baseChartId]
    }
    
    /**
     * Extract the base chart ID from a chart ID with date suffix
     * @param fullChartId Chart ID with optional date suffix
     * @return Base chart ID without date suffix
     */
    fun getBaseChartId(fullChartId: String): String {
        // Regex pattern to match date suffixes: _YYYY_MM or _YYYY_MM_DD
        val regex = Regex("_(\\d{4})_(\\d{1,2})(?:_(\\d{1,2}))?$")
        return regex.replace(fullChartId, "")
    }
    
    /**
     * Set refresh mode to indicate charts were refreshed via SwipeRefreshLayout
     * This will cause the next calls to getSelectedMonth/Day to return null
     * which forces charts to show the latest data
     */
    fun setRefreshedViaSwipe(wasRefreshed: Boolean) {
        wasRefreshedViaSwipe = wasRefreshed
    }
    
    /**
     * Clear all saved selections
     */
    fun clearAllSelections() {
        selectedMonths.clear()
        selectedDays.clear()
        wasRefreshedViaSwipe = false
    }
} 