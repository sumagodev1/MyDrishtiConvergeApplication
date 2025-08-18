package com.mydrishti.co.`in`.activities.utils

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Utility class for validating date ranges and preventing invalid date selections
 * Fixes the decade loop bug and restricts future dates
 */
object DateRangeValidator {

    private const val TAG = "DateRangeValidator"

    /**
     * Validate if a date is within acceptable bounds
     * @param date The date to validate
     * @param allowFuture Whether to allow future dates
     * @param maxYearsBack Maximum years back from current date
     * @return True if date is valid
     */
    fun isValidDate(
        date: LocalDate,
        allowFuture: Boolean = false,
        maxYearsBack: Int = 5
    ): Boolean {
        return CrashReportingManager.safeExecute(
            operation = {
                val currentDate = LocalDate.now()
                val minDate = currentDate.minusYears(maxYearsBack.toLong())
                val maxDate = if (allowFuture) currentDate.plusYears(1) else currentDate

                date.isAfter(minDate.minusDays(1)) && date.isBefore(maxDate.plusDays(1))
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error validating date", exception)
            },
            defaultValue = false
        ) ?: false
    }

    /**
     * Validate if a year-month combination is valid
     * @param year The year
     * @param month The month (1-12)
     * @param allowFuture Whether to allow future months
     * @return True if year-month is valid
     */
    fun isValidYearMonth(
        year: Int,
        month: Int,
        allowFuture: Boolean = false
    ): Boolean {
        return CrashReportingManager.safeExecute(
            operation = {
                if (month < 1 || month > 12) return@safeExecute false
                if (year < 1900 || year > 2100) return@safeExecute false

                val currentYearMonth = YearMonth.now()
                val targetYearMonth = YearMonth.of(year, month)

                if (allowFuture) {
                    // Allow up to 1 year in the future
                    targetYearMonth.isBefore(currentYearMonth.plusYears(1).plusMonths(1))
                } else {
                    // Only allow current month and past months
                    !targetYearMonth.isAfter(currentYearMonth)
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error validating year-month", exception)
            },
            defaultValue = false
        ) ?: false
    }

    /**
     * Get valid year range for date pickers
     * @param maxYearsBack Maximum years back from current year
     * @param allowFuture Whether to allow future years
     * @return Pair of (minYear, maxYear)
     */
    fun getValidYearRange(
        maxYearsBack: Int = 5,
        allowFuture: Boolean = false
    ): Pair<Int, Int> {
        return CrashReportingManager.safeExecute(
            operation = {
                val currentYear = LocalDate.now().year
                val minYear = currentYear - maxYearsBack
                val maxYear = if (allowFuture) currentYear + 1 else currentYear
                Pair(minYear, maxYear)
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error getting valid year range", exception)
            },
            defaultValue = Pair(2020, 2025)
        ) ?: Pair(2020, 2025)
    }

    /**
     * Get valid month range for a specific year
     * @param year The year
     * @param allowFuture Whether to allow future months
     * @return Pair of (minMonth, maxMonth) where months are 1-12
     */
    fun getValidMonthRange(
        year: Int,
        allowFuture: Boolean = false
    ): Pair<Int, Int> {
        return CrashReportingManager.safeExecute(
            operation = {
                val currentDate = LocalDate.now()
                val currentYear = currentDate.year
                val currentMonth = currentDate.monthValue

                // Add debug logging
                println("DateRangeValidator: Getting month range for year $year, current year is $currentYear, current month is $currentMonth")

                val result = when {
                    year < currentYear -> {
                        // Past year - all months are valid
                        println("DateRangeValidator: Year $year is in the past, allowing all 12 months")
                        Pair(1, 12)
                    }
                    year == currentYear -> {
                        // Current year - months up to current month (or future if allowed)
                        val maxMonth = if (allowFuture) 12 else currentMonth
                        println("DateRangeValidator: Year $year is current year, allowing months 1 to $maxMonth")
                        Pair(1, maxMonth)
                    }
                    year > currentYear && allowFuture -> {
                        // Future year - limited months if allowed
                        if (year == currentYear + 1) {
                            println("DateRangeValidator: Year $year is next year, allowing all 12 months")
                            Pair(1, 12) // Allow all months for next year
                        } else {
                            println("DateRangeValidator: Year $year is far future, allowing only January")
                            Pair(1, 1) // Only January for years beyond next year
                        }
                    }
                    else -> {
                        // Future year not allowed
                        println("DateRangeValidator: Year $year is future but not allowed, defaulting to January")
                        Pair(1, 1)
                    }
                }
                
                // Validate the result
                if (result.first < 1 || result.first > 12 || result.second < 1 || result.second > 12 || result.first > result.second) {
                    println("DateRangeValidator: Invalid month range $result, using fallback")
                    Pair(1, 12)
                } else {
                    result
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error getting valid month range for year $year", exception)
            },
            defaultValue = Pair(1, 12)
        ) ?: Pair(1, 12)
    }

    /**
     * Validate and fix a date to ensure it's within valid bounds
     * @param date The date to validate and fix
     * @param allowFuture Whether to allow future dates
     * @return Valid date within bounds
     */
    fun validateAndFixDate(
        date: LocalDate,
        allowFuture: Boolean = false
    ): LocalDate {
        return CrashReportingManager.safeExecute(
            operation = {
                val currentDate = LocalDate.now()
                val minDate = currentDate.minusYears(5)
                val maxDate = if (allowFuture) currentDate.plusYears(1) else currentDate

                when {
                    date.isBefore(minDate) -> minDate
                    date.isAfter(maxDate) -> maxDate
                    else -> date
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error validating and fixing date", exception)
            },
            defaultValue = LocalDate.now()
        ) ?: LocalDate.now()
    }

    /**
     * Check if a date selection would cause a decade loop
     * @param currentYear Current selected year
     * @param newYear New year being selected
     * @return True if this would cause a decade loop
     */
    fun wouldCauseDecadeLoop(currentYear: Int, newYear: Int): Boolean {
        return CrashReportingManager.safeExecute(
            operation = {
                val yearDifference = kotlin.math.abs(newYear - currentYear)
                // If jumping more than 50 years, it might be a decade loop
                yearDifference > 50
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error checking decade loop", exception)
            },
            defaultValue = false
        ) ?: false
    }

    /**
     * Get safe year values for NumberPicker to prevent decade loops
     * @param allowFuture Whether to allow future years
     * @return Array of valid year values
     */
    fun getSafeYearValues(allowFuture: Boolean = false): Array<String> {
        return CrashReportingManager.safeExecute(
            operation = {
                val (minYear, maxYear) = getValidYearRange(allowFuture = allowFuture)
                val years = mutableListOf<String>()
                
                // Validate year range
                if (minYear > maxYear) {
                    println("DateRangeValidator: Invalid year range ($minYear > $maxYear), using default range")
                    for (year in 2020..2025) {
                        years.add(year.toString())
                    }
                } else {
                    for (year in minYear..maxYear) {
                        if (year > 0) { // Ensure positive years only
                            years.add(year.toString())
                        }
                    }
                }
                
                // Ensure we have at least one year
                if (years.isEmpty()) {
                    println("DateRangeValidator: No valid years found, using current year as fallback")
                    years.add(LocalDate.now().year.toString())
                }
                
                // Filter out any empty strings (safety check)
                val filteredYears = years.filter { it.isNotEmpty() }
                if (filteredYears.isEmpty()) {
                    println("DateRangeValidator: All years were empty, using default")
                    return@safeExecute arrayOf("2025")
                }
                
                println("DateRangeValidator: Generated ${filteredYears.size} year values from $minYear to $maxYear")
                filteredYears.toTypedArray()
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error getting safe year values", exception)
                println("DateRangeValidator: Exception in getSafeYearValues: ${exception.message}")
            },
            defaultValue = arrayOf("2020", "2021", "2022", "2023", "2024", "2025")
        ) ?: arrayOf("2020", "2021", "2022", "2023", "2024", "2025")
    }

    /**
     * Get safe month values for NumberPicker
     * @param year The selected year
     * @param allowFuture Whether to allow future months
     * @return Array of valid month names
     */
    fun getSafeMonthValues(year: Int, allowFuture: Boolean = false): Array<String> {
        return CrashReportingManager.safeExecute(
            operation = {
                val monthNames = arrayOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                
                println("DateRangeValidator: Getting safe month values for year $year")
                
                val (minMonth, maxMonth) = getValidMonthRange(year, allowFuture)
                val validMonths = mutableListOf<String>()
                
                println("DateRangeValidator: Month range for year $year is $minMonth to $maxMonth")
                
                // Ensure we have valid month bounds and handle edge cases
                val safeMinMonth = minMonth.coerceIn(1, 12)
                val safeMaxMonth = maxMonth.coerceIn(safeMinMonth, 12) // Ensure max >= min
                
                // Validate that we have a proper range
                if (safeMinMonth > safeMaxMonth) {
                    println("DateRangeValidator: Invalid month range ($safeMinMonth > $safeMaxMonth), using all months")
                    return@safeExecute monthNames
                }
                
                for (month in safeMinMonth..safeMaxMonth) {
                    if (month in 1..12) {
                        val monthName = monthNames[month - 1]
                        if (monthName.isNotEmpty()) { // Ensure no empty strings
                            validMonths.add(monthName)
                            println("DateRangeValidator: Added month $monthName for year $year")
                        }
                    }
                }
                
                // Ensure we always have at least one valid month
                if (validMonths.isEmpty()) {
                    println("DateRangeValidator: No valid months found for year $year, using all months as fallback")
                    return@safeExecute monthNames
                }
                
                // Filter out any null or empty values (safety check)
                val filteredMonths = validMonths.filter { it.isNotEmpty() }
                if (filteredMonths.isEmpty()) {
                    println("DateRangeValidator: All months were empty, using default months")
                    return@safeExecute monthNames
                }
                
                println("DateRangeValidator: Final month list for year $year: ${filteredMonths.joinToString(", ")}")
                filteredMonths.toTypedArray()
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error getting safe month values for year $year", exception)
                println("DateRangeValidator: Exception in getSafeMonthValues: ${exception.message}")
            },
            defaultValue = arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
        ) ?: arrayOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }

    /**
     * Convert month name to month number
     * @param monthName The month name (e.g., "January")
     * @return Month number (1-12)
     */
    fun monthNameToNumber(monthName: String): Int {
        return CrashReportingManager.safeExecute(
            operation = {
                val monthNames = arrayOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                
                val index = monthNames.indexOf(monthName)
                if (index >= 0) index + 1 else 1
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error converting month name to number", exception)
            },
            defaultValue = 1
        ) ?: 1
    }

    /**
     * Convert month number to month name
     * @param monthNumber The month number (1-12)
     * @return Month name
     */
    fun monthNumberToName(monthNumber: Int): String {
        return CrashReportingManager.safeExecute(
            operation = {
                val monthNames = arrayOf(
                    "January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"
                )
                
                if (monthNumber in 1..12) {
                    monthNames[monthNumber - 1]
                } else {
                    "January"
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error converting month number to name", exception)
            },
            defaultValue = "January"
        ) ?: "January"
    }

    /**
     * Get current date components
     * @return Triple of (year, month, day)
     */
    fun getCurrentDateComponents(): Triple<Int, Int, Int> {
        return CrashReportingManager.safeExecute(
            operation = {
                val currentDate = LocalDate.now()
                Triple(currentDate.year, currentDate.monthValue, currentDate.dayOfMonth)
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error getting current date components", exception)
            },
            defaultValue = Triple(2024, 1, 1)
        ) ?: Triple(2024, 1, 1)
    }
}