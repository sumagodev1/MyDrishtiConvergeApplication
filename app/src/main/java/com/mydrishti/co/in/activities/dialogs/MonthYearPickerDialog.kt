package com.mydrishti.co.`in`.activities.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.utils.CrashReportingManager
import com.mydrishti.co.`in`.activities.utils.DateRangeValidator
import com.mydrishti.co.`in`.activities.utils.DialogLayoutManager
import java.time.LocalDate

/**
 * Enhanced Month/Year picker dialog that prevents decade loops and invalid date selections
 * Fixes the month selection getting stuck in decade loop issue
 */
class MonthYearPickerDialog : DialogFragment() {

    private var onDateSelectedListener: ((year: Int, month: Int) -> Unit)? = null
    private var initialYear: Int = LocalDate.now().year
    private var initialMonth: Int = LocalDate.now().monthValue
    private var allowFuture: Boolean = false
    private var title: String = "Select Month and Year"

    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker

    companion object {
        private const val TAG = "MonthYearPickerDialog"
        
        fun newInstance(
            initialYear: Int = LocalDate.now().year,
            initialMonth: Int = LocalDate.now().monthValue,
            allowFuture: Boolean = false,
            title: String = "Select Month and Year"
        ): MonthYearPickerDialog {
            return MonthYearPickerDialog().apply {
                this.initialYear = initialYear
                this.initialMonth = initialMonth
                this.allowFuture = allowFuture
                this.title = title
            }
        }
    }

    fun setOnDateSelectedListener(listener: (year: Int, month: Int) -> Unit) {
        this.onDateSelectedListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return CrashReportingManager.safeExecute(
            operation = {
                val context = requireContext()
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.month_year_picker, null)

                setupPickers(view, context)

                val dialog = AlertDialog.Builder(context)
                    .setTitle(title)
                    .setView(view)
                    .setPositiveButton("OK") { _, _ ->
                        handleDateSelection()
                    }
                    .setNegativeButton("Cancel", null)
                    .create()

                // Configure dialog for proper button visibility
                DialogLayoutManager.configureDialog(dialog, ensureButtonVisibility = true)
                
                // Apply rounded corners to the dialog
                dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_background)
                
                // Apply width constraints with margins
                dialog.window?.let { window ->
                    val layoutParams = window.attributes
                    val displayMetrics = context.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val marginInPx = (32 * displayMetrics.density).toInt() // 32dp margins on each side
                    
                    layoutParams.width = screenWidth - (marginInPx * 2) // Subtract margins from both sides
                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                    window.attributes = layoutParams
                }
                
                dialog
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error creating dialog", exception)
            }
        ) ?: super.onCreateDialog(savedInstanceState)
    }

    private fun setupPickers(view: View, context: Context) {
        CrashReportingManager.safeExecute(
            operation = {
                monthPicker = view.findViewById(R.id.monthPicker)
                yearPicker = view.findViewById(R.id.yearPicker)

                setupYearPicker(context)
                setupMonthPicker(context)
                
                // Set initial values
                setInitialValues()
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error setting up pickers", exception)
            }
        )
    }

    private fun setupYearPicker(context: Context) {
        CrashReportingManager.safeExecute(
            operation = {
                val yearValues = DateRangeValidator.getSafeYearValues(allowFuture)
                
                yearPicker.apply {
                    // Validate yearValues array before using it
                    val safeYearValues = yearValues.filter { !it.isNullOrEmpty() }.toTypedArray()
                    if (safeYearValues.isEmpty()) {
                        println("MonthYearPickerDialog: No valid year values, using fallback")
                        return@safeExecute
                    }
                    
                    // Clear any existing displayed values first
                    displayedValues = null
                    
                    minValue = 0
                    maxValue = (safeYearValues.size - 1).coerceAtLeast(0)
                    
                    // Ensure valid range
                    if (maxValue < minValue) {
                        println("MonthYearPickerDialog: Invalid year range, using fallback")
                        return@safeExecute
                    }
                    
                    displayedValues = safeYearValues
                    wrapSelectorWheel = false // Prevent wrapping to avoid decade loops
                    
                    // Set change listener to update month picker when year changes
                    setOnValueChangedListener { _, _, newVal ->
                        try {
                            if (newVal >= 0 && newVal < safeYearValues.size) {
                                val selectedYear = safeYearValues[newVal].toInt()
                                updateMonthPickerForYear(selectedYear, context)
                            }
                        } catch (e: Exception) {
                            println("MonthYearPickerDialog: Error in year change listener: ${e.message}")
                            CrashReportingManager.logError(TAG, "Error in year change listener", e)
                        }
                    }
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error setting up year picker", exception)
            }
        )
    }

    private fun setupMonthPicker(context: Context) {
        CrashReportingManager.safeExecute(
            operation = {
                monthPicker.apply {
                    wrapSelectorWheel = false // Prevent wrapping
                }
                
                // Initialize with current year's months
                updateMonthPickerForYear(initialYear, context)
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error setting up month picker", exception)
            }
        )
    }

    private fun updateMonthPickerForYear(year: Int, context: Context) {
        CrashReportingManager.safeExecute(
            operation = {
                println("MonthYearPickerDialog: Updating month picker for year $year")
                
                val monthValues = DateRangeValidator.getSafeMonthValues(year, allowFuture)
                
                println("MonthYearPickerDialog: Got ${monthValues.size} months for year $year: ${monthValues.joinToString(", ")}")
                
                // Ensure we have at least one month
                if (monthValues.isEmpty()) {
                    println("MonthYearPickerDialog: No months available for year $year, using fallback")
                    return@safeExecute
                }
                
                monthPicker.apply {
                    // Safely update the month picker
                    try {
                        // Validate monthValues array before using it
                        val safeMonthValues = monthValues.filter { !it.isNullOrEmpty() }.toTypedArray()
                        if (safeMonthValues.isEmpty()) {
                            println("MonthYearPickerDialog: No valid month values, skipping update")
                            return@safeExecute
                        }
                        
                        // Clear any existing displayed values first to prevent conflicts
                        displayedValues = null
                        
                        // Set the range
                        minValue = 0
                        maxValue = (safeMonthValues.size - 1).coerceAtLeast(0)
                        
                        // Ensure maxValue is not less than minValue
                        if (maxValue < minValue) {
                            println("MonthYearPickerDialog: Invalid range (max < min), skipping update")
                            return@safeExecute
                        }
                        
                        // Set the displayed values
                        displayedValues = safeMonthValues
                        
                        // Try to maintain the current month selection if valid
                        val currentMonthName = DateRangeValidator.monthNumberToName(initialMonth)
                        val currentMonthIndex = safeMonthValues.indexOf(currentMonthName)
                        
                        println("MonthYearPickerDialog: Looking for month $currentMonthName (month $initialMonth), found at index $currentMonthIndex")
                        
                        if (currentMonthIndex >= 0 && currentMonthIndex <= maxValue) {
                            value = currentMonthIndex
                            println("MonthYearPickerDialog: Set month picker to index $currentMonthIndex (${safeMonthValues[currentMonthIndex]})")
                        } else {
                            // Default to the last available month (usually current month)
                            val safeIndex = maxValue.coerceAtLeast(0)
                            value = safeIndex
                            println("MonthYearPickerDialog: Set month picker to last available month at index $safeIndex (${safeMonthValues[safeIndex]})")
                        }
                        
                        // Force refresh the picker
                        invalidate()
                        
                    } catch (e: Exception) {
                        println("MonthYearPickerDialog: Exception updating month picker: ${e.message}")
                        CrashReportingManager.logError(TAG, "Exception in month picker update", e)
                        
                        // Emergency fallback - reset to a safe state
                        try {
                            displayedValues = null
                            minValue = 0
                            maxValue = 0
                            value = 0
                        } catch (fallbackException: Exception) {
                            println("MonthYearPickerDialog: Even fallback failed: ${fallbackException.message}")
                        }
                    }
                }
                
                println("MonthYearPickerDialog: Successfully updated month picker for year $year")
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error updating month picker for year $year", exception)
                println("MonthYearPickerDialog: Failed to update month picker for year $year: ${exception.message}")
            }
        )
    }

    private fun setInitialValues() {
        CrashReportingManager.safeExecute(
            operation = {
                // Set initial year
                val yearValues = DateRangeValidator.getSafeYearValues(allowFuture)
                val initialYearIndex = yearValues.indexOf(initialYear.toString())
                if (initialYearIndex >= 0) {
                    yearPicker.value = initialYearIndex
                } else {
                    // Default to current year or closest valid year
                    val currentYear = LocalDate.now().year
                    val currentYearIndex = yearValues.indexOf(currentYear.toString())
                    yearPicker.value = if (currentYearIndex >= 0) currentYearIndex else yearValues.size - 1
                }

                // Set initial month (this will be handled by updateMonthPickerForYear)
                val selectedYear = yearValues[yearPicker.value].toInt()
                val monthValues = DateRangeValidator.getSafeMonthValues(selectedYear, allowFuture)
                val initialMonthName = DateRangeValidator.monthNumberToName(initialMonth)
                val initialMonthIndex = monthValues.indexOf(initialMonthName)
                if (initialMonthIndex >= 0) {
                    monthPicker.value = initialMonthIndex
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error setting initial values", exception)
            }
        )
    }

    private fun handleDateSelection() {
        CrashReportingManager.safeExecute(
            operation = {
                val yearValues = DateRangeValidator.getSafeYearValues(allowFuture)
                val selectedYear = yearValues[yearPicker.value].toInt()
                
                val monthValues = DateRangeValidator.getSafeMonthValues(selectedYear, allowFuture)
                val selectedMonthName = monthValues[monthPicker.value]
                val selectedMonth = DateRangeValidator.monthNameToNumber(selectedMonthName)

                // Validate the selection
                if (DateRangeValidator.isValidYearMonth(selectedYear, selectedMonth, allowFuture)) {
                    onDateSelectedListener?.invoke(selectedYear, selectedMonth)
                } else {
                    // Fallback to current date if invalid
                    val (currentYear, currentMonth, _) = DateRangeValidator.getCurrentDateComponents()
                    onDateSelectedListener?.invoke(currentYear, currentMonth)
                    
                    CrashReportingManager.logWarning(
                        TAG,
                        "Invalid date selection, using current date as fallback",
                        mapOf(
                            "selectedYear" to selectedYear,
                            "selectedMonth" to selectedMonth,
                            "fallbackYear" to currentYear,
                            "fallbackMonth" to currentMonth
                        )
                    )
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error handling date selection", exception)
                // Fallback to current date
                val (currentYear, currentMonth, _) = DateRangeValidator.getCurrentDateComponents()
                onDateSelectedListener?.invoke(currentYear, currentMonth)
            }
        )
    }

    override fun onStart() {
        super.onStart()
        
        // Apply additional dialog configuration after showing
        CrashReportingManager.safeExecute(
            operation = {
                dialog?.let { dialog ->
                    DialogLayoutManager.fixExistingDialogButtons(dialog)
                    
                    // Customize button colors based on theme
                    if (dialog is AlertDialog) {
                        // Detect if we're in dark mode
                        val isDarkMode = (requireContext().resources.configuration.uiMode and 
                            android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                            android.content.res.Configuration.UI_MODE_NIGHT_YES
                        
                        // Set Cancel button to red color
                        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        cancelButton?.setTextColor(android.graphics.Color.parseColor("#F44336"))
                        
                        // Set OK button color based on theme
                        val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        val okButtonColor = if (isDarkMode) {
                            android.graphics.Color.WHITE // White in dark mode
                        } else {
                            android.graphics.Color.BLACK // Black in light mode
                        }
                        okButton?.setTextColor(okButtonColor)
                    }
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error in onStart", exception)
            }
        )
    }
}