package com.mydrishti.co.`in`.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartData
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.databinding.ItemBarChartBinding
import com.mydrishti.co.`in`.databinding.ItemGaugeChartBinding
import com.mydrishti.co.`in`.databinding.ItemMetricChartBinding
import androidx.core.content.ContextCompat
import com.github.anastr.speedviewlib.components.Section
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Collections
import java.util.Calendar
import java.util.Random
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.github.anastr.speedviewlib.SpeedView
import android.view.View
import android.widget.ProgressBar
import kotlin.math.max
import kotlin.math.abs
import java.util.TimeZone
import com.github.mikephil.charting.utils.MPPointF
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.ScaleAnimation
import android.view.animation.OvershootInterpolator
import android.view.animation.AnimationSet
import android.view.animation.AlphaAnimation
import com.github.anastr.speedviewlib.Speedometer
import com.github.anastr.speedviewlib.components.Style
import android.util.TypedValue
import android.graphics.Typeface
import android.app.AlertDialog
import android.widget.NumberPicker
import android.view.MenuItem
import android.widget.PopupMenu
import android.content.Intent
import com.google.android.material.snackbar.Snackbar

class ChartDashboardAdapter(
    private val context: Context,
    private val chartConfigs: MutableList<ChartConfig>,
    private val onChartConfigClickListener: (ChartConfig) -> Unit,
    private val onChartConfigLongClickListener: (ChartConfig, Int) -> Boolean,
    private val onChartRefreshRequestListener: (String) -> Unit,
    private val onEditChartListener: (ChartConfig) -> Unit = { _ -> }, // Default empty implementation
    private val onDeleteChartListener: (ChartConfig) -> Unit = { _ -> } // Default empty implementation
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BAR_DAILY = 0
        private const val VIEW_TYPE_BAR_HOURLY = 1
        private const val VIEW_TYPE_GAUGE = 2
        private const val VIEW_TYPE_METRIC = 3
    }
    
    // Store chart data separately from configurations
    private val chartDataMap = mutableMapOf<String, ChartData>()

    // Cache for parameter display names to avoid repeated database lookups
    private val parameterDisplayNameCache = mutableMapOf<Int, String>()

    // Replace the long click listener with options menu listener
    private val onMoreOptionsClickListener: (ChartConfig, View, Int) -> Unit = { chartConfig, view, position ->
        showPopupMenu(chartConfig, view, position)
    }
    
    private fun showPopupMenu(chartConfig: ChartConfig, view: View, position: Int) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.menu_chart_options, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    // Call the edit listener directly
                    onEditChartListener(chartConfig)
                    true
                }
                R.id.action_delete -> {
                    // Call the delete listener directly
                    onDeleteChartListener(chartConfig)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BAR_DAILY, VIEW_TYPE_BAR_HOURLY -> {
                val binding = ItemBarChartBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                // Use 'this' as the receiver for inner class constructor  
                this.BarChartViewHolder(binding)
            }

            VIEW_TYPE_GAUGE -> {
                val binding = ItemGaugeChartBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                // Use 'this' as the receiver for inner class constructor
                this.GaugeChartViewHolder(binding)
            }

            VIEW_TYPE_METRIC -> {
                val binding = ItemMetricChartBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                // Use 'this' as the receiver for inner class constructor
                this.MetricChartViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = chartConfigs.size

    override fun getItemViewType(position: Int): Int {
        return when (chartConfigs[position].chartType) {
            ChartType.BAR_DAILY -> VIEW_TYPE_BAR_DAILY
            ChartType.BAR_HOURLY -> VIEW_TYPE_BAR_HOURLY
            ChartType.GAUGE -> VIEW_TYPE_GAUGE
            ChartType.METRIC -> VIEW_TYPE_METRIC
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chartConfig = chartConfigs[position]
        val chartData = chartDataMap[chartConfig.id]
        
        when (holder) {
            is BarChartViewHolder -> {
                if (chartConfig.chartType == ChartType.BAR_DAILY || chartConfig.chartType == ChartType.BAR_HOURLY) {
                    holder.bind(chartConfig, chartData)
                }
            }
            is GaugeChartViewHolder -> {
                if (chartConfig.chartType == ChartType.GAUGE) {
                    holder.bind(chartConfig, chartData)
                }
            }
            is MetricChartViewHolder -> {
                if (chartConfig.chartType == ChartType.METRIC) {
                    holder.bind(chartConfig, chartData)
                }
            }
        }
    }

    // Update the adapter with new chart configurations
    fun updateChartConfigs(newChartConfigs: List<ChartConfig>) {
        val diffCallback = ChartDiffCallback(chartConfigs, newChartConfigs)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        chartConfigs.clear()
        chartConfigs.addAll(newChartConfigs)
        
        diffResult.dispatchUpdatesTo(this)
        
        // Request data for each config that doesn't have data yet
        newChartConfigs.forEach { config ->
            if (chartDataMap[config.id] == null) {
                onChartRefreshRequestListener(config.id)
            }
        }
    }
    
    // Update chart data
    fun updateChartData(data: List<ChartData>) {
        // First identify which charts are getting updated so we can clear their parameter display name cache
        val updatedChartIds = data.map { it.chartId }
        
        // Clear parameter display name cache for parameters in updated charts
        val parametersToRemove = mutableSetOf<Int>()
        for (chartData in data) {
            // For gauge charts, look for parameterId
            if (chartData.parameters.containsKey("parameterId")) {
                val parameterId = chartData.parameters["parameterId"]?.toIntOrNull()
                if (parameterId != null) {
                    parametersToRemove.add(parameterId)
                }
            }
            
            // For other charts, check parameterIds list
            if (chartData.parameters.containsKey("parameterIds")) {
                val parameterIdsStr = chartData.parameters["parameterIds"] ?: ""
                parameterIdsStr.split(",").forEach { idStr ->
                    val parameterId = idStr.toIntOrNull()
                    if (parameterId != null) {
                        parametersToRemove.add(parameterId)
                    }
                }
            }
        }
        
        // Remove the identified parameters from the cache
        parametersToRemove.forEach { parameterId ->
            parameterDisplayNameCache.remove(parameterId)
            println("Cleared display name cache for parameter $parameterId")
        }
        
        // Update the data map
        data.forEach { chartData ->
            chartDataMap[chartData.chartId] = chartData
        }
        
        // Notify adapter that data has changed
        notifyDataSetChanged()
        
        println("Updated chart data for ${data.size} charts, cleared cache for ${parametersToRemove.size} parameters")
    }
    
    // Get charts for reordering
    fun getCharts(): List<ChartConfig> {
        return chartConfigs.toList()
    }

    // Move a chart in the list
    fun moveChart(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(chartConfigs, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(chartConfigs, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    // Inner class for DiffUtil
    inner class ChartDiffCallback(
        private val oldList: List<ChartConfig>,
        private val newList: List<ChartConfig>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean =
            oldList[oldPos].id == newList[newPos].id
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean =
            oldList[oldPos] == newList[newPos]
    }
    
    /**
     * Helper method to get the parameter name for the given parameter ID
     * Used to display the parameter name in the chart title
     */
    private fun getParameterName(parameterId: Int): String {
        // First check our cache to avoid repeated lookups
        if (parameterDisplayNameCache.containsKey(parameterId)) {
            return parameterDisplayNameCache[parameterId] ?: ""
        }

        // Look through all chart data for information about this parameter
        for (chartData in chartDataMap.values) {
            // Check if the chart data contains this parameter ID
            if (chartData.parameters["parameterId"]?.toIntOrNull() == parameterId) {
                // Look for the parameter display name from the API
                val displayName = chartData.parameters["parameterDisplayName"] ?: ""
                if (displayName.isNotEmpty()) {
                    // Cache and return the display name from the API
                    parameterDisplayNameCache[parameterId] = displayName
                    return displayName
                }
            }
            
            // For metric charts, the parameter ID is in the list
            if (chartData.parameters["parameterIds"]?.contains(parameterId.toString()) == true) {
                // Extract display name information for this parameter
                val displayName = chartData.parameters["displayName_$parameterId"] ?: ""
                if (displayName.isNotEmpty()) {
                    parameterDisplayNameCache[parameterId] = displayName
                    return displayName
                }
            }
        }
        
        // No display name found in the parameters
        return ""
    }

    // Bar Chart ViewHolder
    inner class BarChartViewHolder(private val binding: ItemBarChartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set up click listeners
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChartConfigClickListener(chartConfigs[position])
                }
            }

            // Replace long-click with more options button click
            binding.moreOptionsButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMoreOptionsClickListener(chartConfigs[position], it, position)
                }
            }
        }

        fun bind(chartConfig: ChartConfig, chartData: ChartData?) {
            // For bar charts, also append parameter name to device name in the subtitle
            val parameterName = getParameterName(chartConfig.parameterIds.firstOrNull() ?: 0)
            
            // Set title from user-defined chart title
            binding.chartTitle.text = chartConfig.title
            
            // Set device name with parameter name in the second line
            if (parameterName.isNotEmpty()) {
                val deviceWithParameter = "${chartConfig.deviceName} - $parameterName"
                binding.siteName.text = deviceWithParameter
            } else {
            binding.siteName.text = chartConfig.deviceName
            }
            
            // Update the 'Date' field to show the selected date (if binding.dateField exists)
            if (binding::class.java.declaredFields.any { it.name == "dateField" }) {
                var dateLabel: String? = chartData?.parameters?.get("dataForDay")
                if (dateLabel.isNullOrEmpty()) {
                    val regex = Regex("_(\\d{4})_(\\d{1,2})_(\\d{1,2})$")
                    val match = regex.find(chartConfig.id)
                    if (match != null) {
                        val (year, month, day) = match.destructured
                        dateLabel = String.format("%s-%02d-%02d", year, month.toInt() + 1, day.toInt())
                    }
                }
                val formattedDate = dateLabel?.let {
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        outputFormat.format(inputFormat.parse(it) ?: it)
                    } catch (e: Exception) { it }
                } ?: ""
                println("DEBUG: Setting dateField to $formattedDate")
                try {
                    val dateField = binding.javaClass.getDeclaredField("dateField")
                    dateField.isAccessible = true
                    (dateField.get(binding) as? TextView)?.text = formattedDate
                } catch (_: Exception) {}
            } else {
                // TODO: Add a TextView for the date in your layout and bind it here
            }
            val lastUpdatedValue = formatLastUpdated(chartData?.timestamp ?: chartConfig.lastUpdated)
            println("DEBUG: Setting lastUpdated to $lastUpdatedValue")
            binding.lastUpdated.text = lastUpdatedValue

            // Show loading state if no data is available
            if (chartData == null) {
                binding.chartProgressBar.visibility = View.VISIBLE
                binding.barChart.visibility = View.GONE
                return
            } else {
                binding.chartProgressBar.visibility = View.GONE
                binding.barChart.visibility = View.VISIBLE
            }

            // Display the unit in the dedicated unit TextView
            val unitText = binding.unitText
            val unit = chartData.parameters["unit"] ?: ""
            if (unit.isNotEmpty()) {
            unitText.text = "Unit: $unit"
                unitText.visibility = View.VISIBLE
            } else {
                unitText.text = ""
                unitText.visibility = View.GONE
            }

            // Setup date or month selector based on chart type
            setupSelectors(chartConfig)

            // Get chart parameters
            val params = chartData.parameters
            
            // Set up the chart
            setupBarChart(binding.barChart, chartConfig, params)
        }

        // Helper array for month names - available to all methods in this class
        private val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        // Helper function to convert dp to pixels
        private fun dpToPx(dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

        private fun setupSelectors(chartConfig: ChartConfig) {
            when (chartConfig.chartType) {
                ChartType.BAR_DAILY -> setupMonthSelector(chartConfig)
                ChartType.BAR_HOURLY -> setupDateSelector(chartConfig)
                else -> {
                    binding.monthSelectionLayout.visibility = View.GONE
                    binding.dateSelectionLayout.visibility = View.GONE
                }
            }
        }

        private fun setupMonthSelector(chartConfig: ChartConfig) {
            binding.monthSelectionLayout.visibility = View.VISIBLE
            binding.dateSelectionLayout.visibility = View.GONE

            // Get current date info
            val cal = Calendar.getInstance()
            val currentSystemMonth = cal.get(Calendar.MONTH)
            val currentSystemYear = cal.get(Calendar.YEAR)

            // Set initial month display text - show selected month/year if present in chartConfig.id
            val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
            val regex = Regex("_(\\d{4})_(\\d{1,2})$")
            val match = regex.find(chartConfig.id)
            val selectedMonthText = if (match != null) {
                val (year, month) = match.destructured
                formatter.format(Calendar.getInstance().apply {
                    set(Calendar.YEAR, year.toInt())
                    set(Calendar.MONTH, month.toInt())
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time)
            } else {
                formatter.format(cal.time)
            }
            binding.tvMonthDisplay.text = selectedMonthText
            
            // Set calendar icon for the month picker
            binding.monthDropdownIcon.setImageResource(R.drawable.ic_calender)

            // Set up click listener for the month dropdown icon (now year-month picker icon)
            binding.monthDropdownIcon.setOnClickListener {
                // Create a dialog with month-year picker
                val dialogView = LayoutInflater.from(context).inflate(R.layout.month_year_picker, null)
                val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
                val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
                
                // Setup month picker
                val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                monthPicker.minValue = 0
                monthPicker.maxValue = 11
                monthPicker.displayedValues = monthNames
                monthPicker.value = currentSystemMonth
                
                // Setup year picker - show 5 years back and 2 years ahead
                val minYear = currentSystemYear - 5
                val maxYear = currentSystemYear + 2
                yearPicker.minValue = minYear
                yearPicker.maxValue = maxYear
                yearPicker.value = currentSystemYear
                
                // Create dialog with app theme
                val dialogTheme = if (isNightMode()) AlertDialog.THEME_DEVICE_DEFAULT_DARK else AlertDialog.THEME_DEVICE_DEFAULT_LIGHT
                
                val alertDialog = AlertDialog.Builder(context, dialogTheme)
                    .setTitle("Select Month and Year")
                    .setView(dialogView)
                    .setPositiveButton("OK") { _, _ ->
                        // When user selects a month and year
                        val selectedMonth = monthPicker.value
                        val selectedYear = yearPicker.value
                        
                        // Update calendar with selected values
                        cal.set(Calendar.MONTH, selectedMonth)
                        cal.set(Calendar.YEAR, selectedYear)
                        
                        // Update the display
                        val selectedMonthText = formatter.format(cal.time)
                        binding.tvMonthDisplay.text = selectedMonthText

                        // Remove any existing _YYYY_M format from the ID before appending
                        val baseId = chartConfig.id.replace(Regex("_\\d{4}_\\d{1,2}$"), "")
                        val monthSpecificId = "${baseId}_${selectedYear}_${selectedMonth}"
                        
                        // Clear any existing cache for this month
                        chartDataMap.remove(monthSpecificId)

                        // Request data refresh from API - use the month-specific ID
                        println("CRITICAL DEBUG: Requesting API refresh for monthly data: $monthSpecificId")
                        onChartRefreshRequestListener(monthSpecificId)
                            
                        // --- NEW LOGIC: Update chartConfigs with new ChartConfig using the new ID ---
                        val newChartConfig = chartConfig.copy(id = monthSpecificId)
                        val newChartConfigs = chartConfigs.toMutableList()
                        val pos = adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            newChartConfigs[pos] = newChartConfig
                            updateChartConfigs(newChartConfigs)
                        }

                        // Display loading state
                        binding.chartProgressBar.visibility = View.VISIBLE
                        binding.barChart.visibility = View.GONE
                        
                        // Add a safety timeout to hide progress bar if no data comes back
                        binding.chartProgressBar.postDelayed({
                            if (binding.chartProgressBar.visibility == View.VISIBLE) {
                                println("CRITICAL DEBUG: Safety timeout: hiding progress bar after delay")
                                binding.chartProgressBar.visibility = View.GONE
                                binding.barChart.visibility = View.VISIBLE
                                
                                // Check if we received data for this month-specific chart ID
                                val monthlyData = chartDataMap[monthSpecificId]
                                if (monthlyData == null || monthlyData.parameters["no_data"] == "true") {
                                    // No data available for this month, show clear message
                                    binding.barChart.setNoDataText("No data available for ${monthNames[selectedMonth]} ${selectedYear}")
                                    binding.barChart.setNoDataTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                                    binding.barChart.invalidate()
                                    println("CRITICAL DEBUG: No data found for ${monthNames[selectedMonth]} ${selectedYear}, showing empty state")
                                }
                            }
                        }, 5000) // 5 second safety timeout
                    }
                    .setNegativeButton("Cancel", null)
                    .create()
                
                // Apply some styling to the dialog
                alertDialog.show()
                
                // Set the button colors after the dialog is shown
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.let { button ->
                    button.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.let { button ->
                    button.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                }
            }

            // Initial load of the current month's data from API (not simulated)
            val monthSpecificId = "${chartConfig.id}_${currentSystemYear}_${currentSystemMonth}"
            
            // Only refresh if we don't already have data for this month in the cache
            if (!chartDataMap.containsKey(monthSpecificId)) {
                // Request data refresh from API with month-specific ID
                println("Initial API refresh for monthly data: $monthSpecificId")
                onChartRefreshRequestListener(monthSpecificId)
                // Show loading state
                binding.chartProgressBar.visibility = View.VISIBLE
                binding.barChart.visibility = View.GONE
                
                // Add a safety timeout to hide progress bar if no data comes back
                binding.chartProgressBar.postDelayed({
                    if (binding.chartProgressBar.visibility == View.VISIBLE) {
                        println("Initial load safety timeout: hiding progress bar after delay")
                        binding.chartProgressBar.visibility = View.GONE
                        binding.barChart.visibility = View.VISIBLE
                        
                        // Check for month-specific data
                        val monthlyData = chartDataMap[monthSpecificId]
                        if (monthlyData == null || monthlyData.parameters["no_data"] == "true") {
                            // No data available for this month, show clear message
                            binding.barChart.setNoDataText("No data available for ${monthNames[currentSystemMonth]} ${currentSystemYear}")
                            binding.barChart.setNoDataTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                            binding.barChart.invalidate()
                            println("No data found for initial month ${monthNames[currentSystemMonth]} ${currentSystemYear}, showing empty state")
                        }
                    }
                }, 5000) // 5 second safety timeout
            } else {
                println("Using cached data for month: $monthSpecificId")
                // Use the existing chart data
                val chartData = chartDataMap[monthSpecificId]
                if (chartData != null) {
                    setupBarChart(binding.barChart, chartConfig, chartData.parameters)
            }
            }
            
            println("Month selector setup complete with year-month picker")
        }
        
        // Helper method to detect night mode - moved to class level
        private fun isNightMode(): Boolean {
            val nightModeFlags = context.resources.configuration.uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
            return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }

        private fun setupDateSelector(chartConfig: ChartConfig) {
            binding.dateSelectionLayout.visibility = View.VISIBLE
            binding.monthSelectionLayout.visibility = View.GONE

            // Get current date information
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)

            // Format and display the selected date if present in chartConfig.id
            val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val regex = Regex("_(\\d{4})_(\\d{1,2})_(\\d{1,2})$")
            val match = regex.find(chartConfig.id)
            val selectedDateText = if (match != null) {
                val (year, month, day) = match.destructured
                dateFormatter.format(Calendar.getInstance().apply {
                    set(Calendar.YEAR, year.toInt())
                    set(Calendar.MONTH, month.toInt())
                    set(Calendar.DAY_OF_MONTH, day.toInt())
                }.time)
            } else {
                dateFormatter.format(cal.time)
            }
            binding.tvDateDisplay.text = selectedDateText

            // Hide the time display as requested
            binding.tvTimeDisplay.visibility = View.GONE

            // Set up click listener for the date calendar icon
            binding.dateCalendarIcon.setOnClickListener {
                // Create a date picker dialog with the app's theme
                val dialogTheme = if (isNightMode()) AlertDialog.THEME_DEVICE_DEFAULT_DARK else AlertDialog.THEME_DEVICE_DEFAULT_LIGHT
                val datePickerDialog = android.app.DatePickerDialog(
                    context,
                    dialogTheme,
                    { _, year, month, dayOfMonth ->
                        // When user selects a date
                        cal.set(Calendar.YEAR, year)
                        cal.set(Calendar.MONTH, month)
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                        // Format and display the selected date in simplified format
                        val selectedDate = dateFormatter.format(cal.time)
                        binding.tvDateDisplay.text = selectedDate

                        // Create a day-specific ID for this chart and date
                        val daySpecificId = "${chartConfig.id}_${year}_${month}_${dayOfMonth}"
                        // Format the date for display messages
                        val selectedDateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.time)
                        // Clear cache for this specific day
                        chartDataMap.remove(daySpecificId)
                        println("CRITICAL DEBUG: Cleared cache for day-specific ID: $daySpecificId")
                        // Show loading state
                        binding.chartProgressBar.visibility = View.VISIBLE
                        binding.barChart.visibility = View.GONE
                        // Request API data for this date using the day-specific ID
                        println("CRITICAL DEBUG: Requesting API data for day: $year-${month+1}-$dayOfMonth (ID: $daySpecificId)")
                        onChartRefreshRequestListener(daySpecificId)
                        // --- NEW LOGIC: Update chartConfigs with new ChartConfig using the new ID ---
                        val newChartConfig = chartConfig.copy(id = daySpecificId)
                        val newChartConfigs = chartConfigs.toMutableList()
                        val pos = adapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            newChartConfigs[pos] = newChartConfig
                            updateChartConfigs(newChartConfigs)
                        }
                        // Add a safety timeout to hide progress bar if no data comes back
                        binding.chartProgressBar.postDelayed({
                            if (binding.chartProgressBar.visibility == View.VISIBLE) {
                                println("CRITICAL DEBUG: Day selection safety timeout: hiding progress bar after delay")
                                binding.chartProgressBar.visibility = View.GONE
                                binding.barChart.visibility = View.VISIBLE
                                // Check if we got data for this day
                                val dayData = chartDataMap[daySpecificId]
                                if (dayData == null || dayData.parameters["no_data"] == "true") {
                                    // No data available for this day, show clear message
                                    binding.barChart.setNoDataText("No hourly data available for $selectedDateStr")
                                    binding.barChart.setNoDataTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                                    binding.barChart.invalidate()
                                    println("CRITICAL DEBUG: No data found for day $selectedDateStr, showing empty state")
                                }
                            }
                        }, 5000) // 5 second safety timeout
                    },
                    currentYear,
                    currentMonth,
                    currentDay
                )

                // Set date picker constraints
                // Only allow selection of dates up to today
                val maxDate = Calendar.getInstance()
                datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

                // Set minimum date (e.g., 30 days ago)
                val minDate = Calendar.getInstance()
                minDate.add(Calendar.DAY_OF_MONTH, -30)
                datePickerDialog.datePicker.minDate = minDate.timeInMillis

                // Show the date picker dialog
                datePickerDialog.show()
                
                // Set button colors to match app theme
                datePickerDialog.getButton(android.app.DatePickerDialog.BUTTON_POSITIVE)?.let { button ->
                    button.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                }
                datePickerDialog.getButton(android.app.DatePickerDialog.BUTTON_NEGATIVE)?.let { button ->
                    button.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                }
            }

            // Initial load for today's data
            val todaySpecificId = "${chartConfig.id}_${currentYear}_${currentMonth}_${currentDay}"
            
            // Only load from API if data not in cache
            if (!chartDataMap.containsKey(todaySpecificId)) {
                // Show loading state
                binding.chartProgressBar.visibility = View.VISIBLE
                binding.barChart.visibility = View.GONE
                
                // Request API data for today
                println("Initial API data request for today: $currentYear-${currentMonth+1}-$currentDay (ID: $todaySpecificId)")
                onChartRefreshRequestListener(todaySpecificId)
                
                // Add a safety timeout for initial load
                binding.chartProgressBar.postDelayed({
                    if (binding.chartProgressBar.visibility == View.VISIBLE) {
                        println("Initial day load safety timeout: hiding progress bar after delay")
                        binding.chartProgressBar.visibility = View.GONE
                        binding.barChart.visibility = View.VISIBLE
                        
                        // Check if we got data for today
                        val dayData = chartDataMap[todaySpecificId]
                        if (dayData == null || dayData.parameters["no_data"] == "true") {
                            // No data available for today, show clear message
                            binding.barChart.setNoDataText("No hourly data available for today")
                            binding.barChart.setNoDataTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                            binding.barChart.invalidate()
                            println("No data found for initial day load, showing empty state")
                        }
                    }
                }, 5000) // 5 second safety timeout
            } else {
                // Use cached data for today
                println("Using cached data for today: $todaySpecificId")
                val chartData = chartDataMap[todaySpecificId]
                if (chartData != null) {
                    setupBarChart(binding.barChart, chartConfig, chartData.parameters)
                }
            }
            
            println("Date selector setup complete with calendar icon")
        }

        private fun setupBarChart(barChart: CombinedChart, chartConfig: ChartConfig, params: Map<String, String>) {
            // Check if the API returned no data
            if (params.containsKey("no_data") && params["no_data"] == "true") {
                println("No data available from API for this period")
                barChart.setNoDataText("No data available for selected period")
                barChart.setNoDataTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
                binding.chartProgressBar.visibility = View.GONE
                barChart.visibility = View.VISIBLE
                barChart.invalidate()
                return
            } else {
                binding.chartProgressBar.visibility = View.GONE
                barChart.visibility = View.VISIBLE
            }
            
            // Hide loading indicator and show chart
            binding.chartProgressBar.visibility = View.GONE
            barChart.visibility = View.VISIBLE
            
            // Get the chart type for specific processing
            val chartType = chartConfig.chartType
            
            // Detect if we're dealing with daily or hourly chart
            val isDailyChart = chartType == ChartType.BAR_DAILY
            val isHourlyChart = chartType == ChartType.BAR_HOURLY
            
            // Get the unit from parameters
            val unit = params["unit"] ?: ""
            
            // Log the unit found for debugging
            println("Using unit for bar chart: $unit")

            // Extract labels and values from API response
            val rawLabels = params["labels"]?.split(",") ?: emptyList()
            val rawValues = params["values"]?.split(",")?.map { it.toFloatOrNull() ?: 0f } ?: emptyList()

            // Get original timestamps for detailed timezone conversion
            val timestamps = params["timestamps"]?.split(",") ?: emptyList()

            // Create properly formatted labels for each chart type
            var labels = rawLabels
            
            // Create mutable versions of our values for processing
            var processedLabels = labels
            var processedValues = rawValues
            
            // For specific charts, verify data matches the selected month or day
            if (isDailyChart) {
                val timestamps = params["timestamps"]?.split(",") ?: emptyList()
                val values = params["values"]?.split(",")?.map { it.toFloatOrNull() ?: 0f } ?: emptyList()
                // Do NOT filter by local month; just display all data points for the API's UTC range
                processedLabels = timestamps.map { formatUtcToLocalDateLabel(it) }
                processedValues = values
                println("CRITICAL DEBUG: Using local daily labels: "+ processedLabels.joinToString(", "))
                println("CRITICAL DEBUG: Using daily values: "+ processedValues.joinToString(", "))
            } else if (isHourlyChart) {
                // Generate 24 hour labels
                val hourLabels = (0..23).map { String.format("%02d:00", it) }
                val hourToValue = mutableMapOf<String, Float>()
                // Map existing data to hour labels
                timestamps.forEachIndexed { idx, timestamp ->
                    val hourLabel = try {
                        val utcZdt = java.time.ZonedDateTime.parse(timestamp)
                        val istZdt = utcZdt.withZoneSameInstant(java.time.ZoneId.of("Asia/Kolkata"))
                        String.format("%02d:00", istZdt.hour)
                    } catch (e: Exception) {
                        formatTimestampToHourLabel(timestamp) // fallback
                    }
                    if (idx < rawValues.size) {
                        hourToValue[hourLabel] = rawValues[idx]
                    }
                }
                // Determine if selected day is today
                val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
                val today = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
                val selectedDay = params["selectedDay"]?.toIntOrNull()
                val selectedMonth = params["selectedMonth"]?.toIntOrNull()
                val selectedYear = params["selectedYear"]?.toIntOrNull()
                var maxHour = 23
                if (selectedDay != null && selectedMonth != null && selectedYear != null) {
                    cal.set(Calendar.YEAR, selectedYear)
                    cal.set(Calendar.MONTH, selectedMonth)
                    cal.set(Calendar.DAY_OF_MONTH, selectedDay)
                    if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        cal.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                        maxHour = today.get(Calendar.HOUR_OF_DAY)
                    }
                }
                // Build processedLabels and processedValues for chart
                processedLabels = hourLabels.subList(0, maxHour + 1)
                processedValues = processedLabels.map { hourToValue[it] ?: 0f }
                println("CRITICAL DEBUG: Final hourly X-axis labels: ${processedLabels.joinToString(", ")}")
                println("CRITICAL DEBUG: Final hourly values: ${processedValues.joinToString(", ")}")
            }

            // Basic validation
            if (processedLabels.isEmpty() || processedValues.isEmpty() || processedLabels.size != processedValues.size) {
                println("ERROR: Invalid data. Labels: ${processedLabels.size}, Values: ${processedValues.size}")
                barChart.setNoDataText("No data available")
                barChart.invalidate()
                return
            }

            // For daily charts, handle potential duplicates by consolidating identical dates
            if (isDailyChart) {
                val consolidatedData = consolidateDuplicateDates(processedLabels, processedValues)
                processedLabels = consolidatedData.first
                processedValues = consolidatedData.second
                println("After consolidation: ${processedLabels.size} unique dates (was ${labels.size})")
                
                // Log the final processed data for debugging
                println("Final labels: ${processedLabels.joinToString(", ")}")
                println("Final values: ${processedValues.joinToString(", ")}")
            }

            println("Chart data: ${processedLabels.size} labels, ${processedValues.size} values")

            // Before creating bar entries, print the processedLabels for debug
            println("CRITICAL DEBUG: X-axis labels used for chart: ${processedLabels.joinToString(", ")}")

            // Create bar entries
            val entries = processedValues.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            // Create and configure the dataset
            val dataSet = BarDataSet(entries, chartConfig.title)
            dataSet.colors = listOf(
                ContextCompat.getColor(context, R.color.colorPrimary)
            )
            dataSet.valueTextSize = 10f
            dataSet.setDrawValues(true)
            
            // Detect if we're in dark mode or light mode
            val nightModeFlags = context.resources.configuration.uiMode and 
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK
            val isNightMode = nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
            
            // Set appropriate colors based on theme
            val textColor = if (isNightMode) Color.WHITE else Color.BLACK
            val gridColor = if (isNightMode) Color.GRAY else Color.LTGRAY
            
            // Apply text color to bar values
            dataSet.valueTextColor = if (isNightMode) Color.WHITE else Color.BLACK
            dataSet.valueTypeface = Typeface.DEFAULT_BOLD

            // Value formatter - show values with 1 decimal place
            dataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.2f", value)
                }
            }

            // Calculate average for the reference line
            val average = if (processedValues.isNotEmpty()) processedValues.sum() / processedValues.size else 0f

            // Create bar data with wider bars - adjusted for better visibility
            val barData = BarData(dataSet)
            barData.barWidth = 0.8f  // Adjusted from 0.7f for better bar width

            // Create average line dataset
            val avgEntries = processedValues.mapIndexed { index, _ -> Entry(index.toFloat(), average) }
            val avgLineDataSet = LineDataSet(avgEntries, "Average")
            avgLineDataSet.color = ContextCompat.getColor(context, R.color.colorAccent)
            avgLineDataSet.lineWidth = 1.5f
            avgLineDataSet.setDrawCircles(false)
            avgLineDataSet.setDrawValues(false)
            avgLineDataSet.enableDashedLine(10f, 5f, 0f)

            // Create combined data
            val lineData = LineData(avgLineDataSet)
            val combinedData = CombinedData()
            combinedData.setData(barData)
            combinedData.setData(lineData)

            // Set drawing order
            barChart.drawOrder = arrayOf(
                CombinedChart.DrawOrder.BAR,
                CombinedChart.DrawOrder.LINE
            )

            // Apply combined data to chart
            barChart.data = combinedData

            // Configure X-axis
            val xAxis = barChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textSize = 10f  // Slightly larger text for better visibility
            
            // Apply theme-appropriate text color
            xAxis.textColor = textColor
            xAxis.labelRotationAngle = 45f  // Fixed angle for better readability
            
            // Force labels to be drawn
            xAxis.setDrawLabels(true)
            xAxis.setAvoidFirstLastClipping(true)
            
            // Force labels for all positions
            xAxis.setLabelCount(processedLabels.size, false)  // Force labels for all bars

            // Use a custom formatter for X-axis labels
            xAxis.valueFormatter = object : IndexAxisValueFormatter(processedLabels) {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < processedLabels.size) processedLabels[index] else ""
                }
            }

            // Critical: Set proper axis bounds with padding
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = combinedData.xMax + 0.5f

            // Updated to ensure proper axis bounds:
            // Set axis bounds with padding - critical for scrolling
            val leftPadding = 0.5f
            val rightPadding = 0.5f
            xAxis.axisMinimum = -leftPadding
            xAxis.axisMaximum = processedLabels.size - 1 + rightPadding

            // Explicitly tell chart the bounds - this helps with scrolling
            barChart.setVisibleXRange(2f, 7.5f)
            println("Setting X axis bounds from ${xAxis.axisMinimum} to ${xAxis.axisMaximum} with ${processedLabels.size} labels")

            // Configure Y-axis
            val leftAxis = barChart.axisLeft
            leftAxis.setDrawGridLines(true)
            leftAxis.gridColor = gridColor
            leftAxis.gridLineWidth = 0.5f
            
            // Apply theme-appropriate text color
            leftAxis.textColor = textColor
            leftAxis.textSize = 10f
            leftAxis.setDrawLabels(true)

            // Add a buffer to Y-axis max for better visualization
            val maxValue = processedValues.maxOrNull() ?: 100f
            leftAxis.axisMaximum = maxValue * 1.2f
            leftAxis.axisMinimum = 0f // Start Y-axis at 0

            // Disable right Y-axis
            barChart.axisRight.isEnabled = false

            // Chart configuration
            barChart.description.isEnabled = false
            barChart.legend.isEnabled = true
            barChart.legend.textSize = 11f
            barChart.legend.textColor = textColor  // Use theme-appropriate color for legend
            barChart.setDrawBorders(false)
            barChart.setDrawGridBackground(false)
            
            // Set background color for chart area
            barChart.setBackgroundColor(Color.TRANSPARENT)

            // Extra padding for label visibility
            barChart.setExtraBottomOffset(30f)  // Increased bottom padding for rotated labels
            barChart.setExtraLeftOffset(15f)
            barChart.setExtraRightOffset(15f)

            // Enable interactions
            barChart.isDragEnabled = true
            barChart.isScaleXEnabled = true
            barChart.isScaleYEnabled = false
            barChart.setPinchZoom(true)

            // Set up marker view for value display when tapped
            try {
                val markerView = ChartMarkerView(
                    context,
                    R.layout.view_chart_marker,
                    processedLabels,
                    processedValues,
                    average,
                    unit
                )
                markerView.chartView = barChart
                barChart.marker = markerView
            } catch (e: Exception) {
                println("Error setting up marker view: ${e.message}")
            }

            // Calculate chart width based on data points - critical for scrolling to work
            val screenWidth = context.resources.displayMetrics.widthPixels

            // Important: Make chart width much wider than screen to ensure scrolling works
            // Calculate how wide each bar should be (in pixels)
            val barWidthPx = context.resources.getDimensionPixelSize(R.dimen.bar_width_portrait)

            // Total width must be large enough for all bars with some spacing
            val dataWidth = (barWidthPx * 1.2f * processedLabels.size).toInt()

            // Force chart container to be wider than screen
            val chartWidth = maxOf(dataWidth, screenWidth * 2)

            println("Setting chart width: $chartWidth px for ${processedLabels.size} bars (screen: $screenWidth)")
            barChart.layoutParams.width = chartWidth

            // Critical: Set visible range with proper min and max
            barChart.setVisibleXRangeMaximum(7.5f)  // Show at most 8 bars (7.5 + some margin)
            barChart.setVisibleXRangeMinimum(2f)    // At least 2 bars

            // Force visibility settings to take effect
            barChart.fitScreen() // Reset any zooming
            barChart.invalidate()

            // Handle positioning of the chart view
            barChart.post {
                // Calculate where to position the chart
                val initialPosition = calculateInitialPosition(params, processedLabels, 7.5f, chartType)

                // Disable animation for immediate positioning
                barChart.animateX(0)

                // First make sure the chart knows it can scroll by checking min/max
                if (barChart.xAxis.axisMaximum <= barChart.xAxis.axisMinimum) {
                    println("WARNING: Chart X-axis bounds incorrectly set - fixing...")
                    barChart.xAxis.axisMinimum = -0.5f
                    barChart.xAxis.axisMaximum = processedLabels.size - 0.5f
                }

                // Apply initial position - IMPORTANT: this must be called AFTER invalidate
                barChart.invalidate()
                barChart.moveViewToX(initialPosition)

                // Extra check - if we have more than 8 bars, ensure scroll happened
                if (processedLabels.size > 8) {
                    // Log and correct position if needed
                    barChart.postDelayed({
                        val actualMin = barChart.lowestVisibleX
                        val actualMax = barChart.highestVisibleX
                        println("Chart positioned: showing ${actualMax - actualMin} bars from $actualMin to $actualMax")

                        if (abs(actualMin - initialPosition) > 0.5f) {
                            println("Position correction needed. Expected: $initialPosition, Actual: $actualMin")
                            // Force repositioning
                            barChart.moveViewToX(initialPosition)
                            barChart.invalidate()
                        }
                    }, 150)
                }
            }
        }

        // Add a ChartMarkerView class to handle tooltip display
        inner class ChartMarkerView(
            context: Context,
            layoutResource: Int,
            private val labels: List<String>,
            private val values: List<Float>,
            private val average: Float,
            private val unit: String
        ) : com.github.mikephil.charting.components.MarkerView(context, layoutResource) {

            private val tvTime: TextView = findViewById(R.id.tvTime)
            private val tvValue: TextView = findViewById(R.id.tvValue)
            private val tvAverage: TextView = findViewById(R.id.tvAverage)

            override fun refreshContent(e: Entry?, highlight: Highlight?) {
                if (e == null) return

                val index = e.x.toInt()
                if (index < 0 || index >= labels.size) return

                // Ensure we're using the converted local-time label 
                val timeLabel = if (index < labels.size) labels[index] else ""
                val value = if (index < values.size) values[index] else 0f

                // Format the marker view content with local time
                // For dates in format "DD MMM", display as "Date: DD MMM"
                // For times in format "HH:MM", display as "Time: HH:MM"
                val displayText = if (timeLabel.contains(":")) {
                    "Time: $timeLabel"
                } else {
                    "Date: $timeLabel"
                }
                
                tvTime.text = displayText
                
                // Display value with unit only if unit is available
                if (unit.isNotEmpty()) {
                tvValue.text = "Energy: ${String.format("%.2f", value)} $unit"
                tvAverage.text = "Average: ${String.format("%.2f", average)} $unit"
                } else {
                    tvValue.text = "Energy: ${String.format("%.2f", value)}"
                    tvAverage.text = "Average: ${String.format("%.2f", average)}"
                }

                super.refreshContent(e, highlight)
            }

            override fun getOffset(): MPPointF {
                return MPPointF(-(width / 2f), -height.toFloat())
            }
        }

        // Helper method to determine initial scroll position
        private fun calculateInitialPosition(
            params: Map<String, String>,
            labels: List<String>,
            visibleBarsCount: Float,
            chartType: ChartType
        ): Float {
            // Use constant for visible bars - we want to show 7.5 bars
            val VISIBLE_BARS = 7.5f

            // Check if we should force start at beginning (e.g. from month selector)
            if (params["forceScrollToZero"] == "true") {
                println("Force scroll to beginning (position 0)")
                return 0f
            }

            // For daily charts in current month, try to show today
            if (chartType == ChartType.BAR_DAILY && params["isCurrentMonth"] == "true") {
                val todayDateStr = params["todayDate"] ?: ""
                if (todayDateStr.isNotEmpty()) {
                    val todayIndex = labels.indexOf(todayDateStr)
                    if (todayIndex >= 0) {
                        // Center today's date if possible
                        val centeredPosition = maxOf(0f, todayIndex - (VISIBLE_BARS / 2))
                        println("Daily chart: centering today ($todayDateStr) at position $centeredPosition")
                        return centeredPosition
                    }
                }
            }

            // For hourly charts, position based on current time for today's data
            if (chartType == ChartType.BAR_HOURLY) {
                val dataForDayStr = params["dataForDay"] ?: ""
                val systemDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                if (dataForDayStr == systemDate) {
                    // For today's hourly data, show around current hour
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val position = maxOf(0f, currentHour - (VISIBLE_BARS / 2))
                    println("Hourly chart: showing current hour ($currentHour) at position $position")
                    return position
                } else {
                    // For past days' hourly data, show business hours (8 AM onward)
                    println("Hourly chart for past day: starting at position 8 (8 AM)")
                    return 8f
                }
            }

            // Default: If we have lots of data, show the most recent data
            if (labels.size > VISIBLE_BARS * 1.5) {
                val position = maxOf(0f, labels.size - VISIBLE_BARS)
                println("Default: showing most recent data at position $position")
                return position
            }

            // Otherwise show from the beginning
            println("Default: showing from beginning (position 0)")
            return 0f
        }

        private fun generateDailyLabels(): List<String> {
            val cal = Calendar.getInstance()
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)
            val formatter = SimpleDateFormat("dd-MMM", Locale.getDefault())

            val labels = ArrayList<String>()
            cal.set(Calendar.DAY_OF_MONTH, 1) // Start from first day of month

            // Generate labels for days from the 1st up to the current day only
            for (day in 1..currentDay) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                labels.add(formatter.format(cal.time))
            }

            return labels
        }

        private fun generateHourlyLabels(maxHour: Int = 23): List<String> {
            val labels = ArrayList<String>()
            for (hour in 0..maxHour) {
                // Format hours in 24-hour format (00:00 to 23:00)
                // Make sure we're not including any date parts, just the time
                labels.add(String.format("%02d:00", hour))
            }
            println("Generated ${labels.size} hourly labels from generateHourlyLabels(): ${labels.take(5).joinToString(", ")}...")
            return labels
        }

        private fun simulateMonthlyData(chartConfig: ChartConfig, selectedMonth: Int, selectedYear: Int): ChartConfig {
            // Get current system date for reference
            val systemCal = Calendar.getInstance()
            val currentYear = systemCal.get(Calendar.YEAR)
            val currentMonth = systemCal.get(Calendar.MONTH)
            val currentDay = systemCal.get(Calendar.DAY_OF_MONTH)

            println("System date: $currentYear-${currentMonth+1}")
            println("Simulating data for: $selectedYear-${selectedMonth+1}")

            // Is this the current month and year?
            val isCurrentMonthAndYear = (selectedYear == currentYear && selectedMonth == currentMonth)

            // Create calendar for the selected month
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, selectedYear)
            cal.set(Calendar.MONTH, selectedMonth)
            cal.set(Calendar.DAY_OF_MONTH, 1)

            // Determine how many days to include
            val maxDay = if (isCurrentMonthAndYear) {
                // For current month, only include days up to today
                currentDay
            } else {
                // For past months, include all days
                cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }

            println("Generating data for ${maxDay} days in ${selectedMonth+1}/${selectedYear}")

            // Get formatted name for the selected month and year
            val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val monthYearStr = monthYearFormatter.format(cal.time)

            // Generate consistent labels with capitalized month
            val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val labels = ArrayList<String>()
            val values = ArrayList<String>()
            val timestamps = ArrayList<String>() // Add timestamps list

            // Create random number generator with consistent seed for repeatable results
            // Include year in seed to ensure different years show different patterns
            val random = Random(selectedMonth.toLong() * 31 + selectedYear * 365)

            // Generate data for each day
            for (day in 1..maxDay) {
                // Set calendar to this day
                cal.set(Calendar.DAY_OF_MONTH, day)

                // Format the date consistently
                var dateLabel = dateFormatter.format(cal.time)

                // Ensure month part is capitalized (change "01 jan" to "01 Jan")
                dateLabel = dateLabel.split(" ").let {
                    if (it.size >= 2) {
                        "${it[0]} ${it[1].capitalize()}"
                    } else {
                        dateLabel
                    }
                }

                // Add to labels list
                labels.add(dateLabel)

                // Generate a realistic random value
                val baseValue = 35f + random.nextFloat() * 10

                // Make today's value stand out if this is current month
                val value = if (isCurrentMonthAndYear && day == currentDay) {
                    baseValue * 1.3f // Make today's value 30% higher
                } else {
                    baseValue
                }

                // Add to values list with 2 decimal places
                values.add(String.format(Locale.US, "%.2f", value))

                // Generate corresponding timestamp in UTC format
                cal.set(Calendar.HOUR_OF_DAY, 12) // Noon
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                utcFormat.timeZone = TimeZone.getTimeZone("UTC")
                timestamps.add(utcFormat.format(cal.time))

                println("Added day $day: $dateLabel = ${values.last()} @ ${timestamps.last()}")
            }

            // Get today's date in the same format for highlighting
            val todayDateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val todayDateStr = todayDateFormatter.format(systemCal.time).split(" ").let {
                if (it.size >= 2) "${it[0]} ${it[1].capitalize()}" else ""
            }

            // Create parameters map for the simulated data
            val params = mutableMapOf<String, String>()
            params["labels"] = labels.joinToString(",")
            params["values"] = values.joinToString(",")
            params["timestamps"] = timestamps.joinToString(",") // Add timestamps
            params["isCurrentMonth"] = isCurrentMonthAndYear.toString()
            params["todayDate"] = todayDateStr
            params["unit"] = "kWh"
            params["isDaily"] = "true"
            params["selectedMonth"] = selectedMonth.toString()
            params["selectedYear"] = selectedYear.toString()

            // Additional debug info
            println("Generated ${labels.size} data points for $monthYearStr")
            println("Labels start: ${labels.take(3).joinToString(", ")}")
            println("Labels end: ${labels.takeLast(3).joinToString(", ")}")

            // Create a month-specific chart ID to prevent cache conflicts
            val monthSpecificId = "${chartConfig.id}_${selectedYear}_${selectedMonth}"
            println("Using month-specific chart ID: $monthSpecificId")

            // Create ChartData for this simulated data
            val chartData = ChartData(
                chartId = monthSpecificId,
                chartType = ChartType.BAR_DAILY,
                parameters = params,
                timestamp = System.currentTimeMillis()
            )
            
            // Store the data in our cache
            chartDataMap[monthSpecificId] = chartData

            // Return a new ChartConfig with the same parameterIds but with month-specific ID
            return ChartConfig(
                id = monthSpecificId,
                chartType = ChartType.BAR_DAILY,
                deviceId = chartConfig.deviceId,
                deviceName = chartConfig.deviceName,
                title = "Daily Energy - $monthYearStr",
                parameterIds = chartConfig.parameterIds,
                position = chartConfig.position,
                lastUpdated = System.currentTimeMillis()
            )
        }

        private fun simulateHourlyData(chartConfig: ChartConfig, selectedDay: Int): ChartConfig {
            // Get current date/time for reference
            val systemCal = Calendar.getInstance()
            val currentYear = systemCal.get(Calendar.YEAR)
            val currentMonth = systemCal.get(Calendar.MONTH)
            val currentDay = systemCal.get(Calendar.DAY_OF_MONTH)
            val currentHour = systemCal.get(Calendar.HOUR_OF_DAY)

            // Is this for today?
            val isToday = (selectedDay == currentDay)

            // Set up calendar for selected day
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

            // Format date for display and data tracking
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val selectedDateStr = dateFormatter.format(calendar.time)

            // Format date in ISO format for API compatibility
            val isoDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val isoDateStr = isoDateFormatter.format(calendar.time)

            println("Generating hourly data for: $selectedDateStr")

            // For today, only include hours up to current hour
            val maxHour = if (isToday) currentHour else 23

            println("Including hours from 0 to $maxHour")

            // Lists to store our data
            val timeLabels = ArrayList<String>()
            val values = ArrayList<String>()
            val timestamps = ArrayList<String>() // Add timestamps list

            // Create random number generator with consistent seed
            val random = Random(currentMonth.toLong() * 100 + selectedDay.toLong())

            // Generate data for each hour
            for (hour in 0..maxHour) {
                // Format hour as HH:00
                val timeLabel = String.format("%02d:00", hour)
                timeLabels.add(timeLabel)

                // Create a realistic energy pattern throughout the day
                val baseValue = when {
                    hour <= 6 -> 2.0f + (hour * 0.25f)               // Early morning (rising)
                    hour <= 12 -> 3.5f + random.nextFloat() * 1.5f    // Morning to noon (peak)
                    hour <= 18 -> 4.0f + random.nextFloat() * 1.0f    // Afternoon (high)
                    else -> 3.0f + (23-hour) * 0.15f                  // Evening (declining)
                }

                // Format with 2 decimal places
                values.add(String.format(Locale.US, "%.2f", baseValue))

                // Generate corresponding timestamp in UTC format for this hour
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                utcFormat.timeZone = TimeZone.getTimeZone("UTC")
                timestamps.add(utcFormat.format(calendar.time))

                println("Added hour $hour: $timeLabel = ${values.last()} @ ${timestamps.last()}")
            }

            // Create parameters map for the simulated data
            val params = mutableMapOf<String, String>()
            params["labels"] = timeLabels.joinToString(",")
            params["values"] = values.joinToString(",")
            params["timestamps"] = timestamps.joinToString(",") // Add timestamps
            params["unit"] = "kWh"
            params["isHourly"] = "true"
            params["isDaily"] = "false"
            params["selectedDay"] = selectedDay.toString()
            params["dataForDay"] = isoDateStr
            params["isToday"] = isToday.toString()
            
            // Create a day-specific chart ID to prevent cache conflicts
            val daySpecificId = "${chartConfig.id}_${currentYear}_${currentMonth}_${selectedDay}"
            println("Using day-specific chart ID: $daySpecificId")
            
            // Create ChartData for this simulated data
            val chartData = ChartData(
                chartId = daySpecificId,
                chartType = ChartType.BAR_HOURLY,
                parameters = params,
                timestamp = System.currentTimeMillis()
            )
            
            // Store the data in our cache
            chartDataMap[daySpecificId] = chartData

            // Return a new ChartConfig with the same parameterIds but with day-specific ID
            return ChartConfig(
                id = daySpecificId,
                chartType = ChartType.BAR_HOURLY,
                deviceId = chartConfig.deviceId,
                deviceName = chartConfig.deviceName,
                title = "Hourly Energy - $selectedDateStr",
                parameterIds = chartConfig.parameterIds,
                position = chartConfig.position,
                lastUpdated = System.currentTimeMillis()
            )
        }

        // Convert UTC timestamps to local time for display
        private fun convertTimestampsToLocalTime(
            timestamps: List<String>, 
            originalLabels: List<String>,
            chartType: ChartType
        ): List<String> {
            val result = mutableListOf<String>()
            
            println("Converting ${timestamps.size} timestamps to local time for chart type $chartType")
            
            // Support multiple timestamp formats
            val utcFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault()),  // Format from API: 2025-05-22T08:30:00.000+00:00
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()) // Format with numeric offset
            )
            
            // Set all formats to UTC
            utcFormats.forEach { it.timeZone = TimeZone.getTimeZone("UTC") }
            
            // Format for output based on chart type
            val localFormat = if (chartType == ChartType.BAR_HOURLY) {
                SimpleDateFormat("HH:mm", Locale.getDefault())
            } else {
                // For daily charts, use dd MMM format
                SimpleDateFormat("dd MMM", Locale.getDefault())
            }
            
            // Set output format to local timezone (IST)
            localFormat.timeZone = TimeZone.getDefault()
            
            // Log the current timezone for debugging
            val currentTz = TimeZone.getDefault()
            println("Current timezone: ${currentTz.id}, offset: ${currentTz.rawOffset / 3600000.0} hours")
            
            // Track converted dates to check for duplicates
            val convertedDateTimeMap = mutableMapOf<String, List<Int>>()
            
            timestamps.forEachIndexed { index, timestamp ->
                try {
                    // Use our helper method to parse the timestamp
                    val date = parseTimestampToDate(timestamp)
                    
                    if (date != null) {
                        // Calculate local time
                        val localTime = localFormat.format(date)
                        
                        // For daily bar charts: log more details to debug date shift issues
                        if (chartType == ChartType.BAR_DAILY) {
                            val utcDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            utcDateFormat.timeZone = TimeZone.getTimeZone("UTC")
                            
                            val localDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            localDateFormat.timeZone = TimeZone.getDefault()
                            
                            val utcDate = utcDateFormat.format(date)
                            val localDate = localDateFormat.format(date)
                            
                            println("Date conversion: UTC=$utcDate, Local=$localDate (${TimeZone.getDefault().displayName})")
                            
                            // Track this date to detect duplicates
                            val indices = convertedDateTimeMap.getOrDefault(localTime, listOf())
                            convertedDateTimeMap[localTime] = indices + index
                        }
                        
                        // Add proper capitalization for month names in the resulting format
                        val formattedLocalTime = if (chartType == ChartType.BAR_DAILY) {
                            localTime.split(" ").let {
                                if (it.size >= 2) "${it[0]} ${it[1].capitalize()}" else localTime
                            }
                        } else {
                            localTime
                        }
                        
                        result.add(formattedLocalTime)
                        println("Converted timestamp: $timestamp → $formattedLocalTime")
                    } else {
                        // Fallback to original label if all parsing attempts fail
                        result.add(if (index < originalLabels.size) originalLabels[index] else "")
                        println("Failed to parse timestamp: $timestamp, using original: ${result.last()}")
                    }
                } catch (e: Exception) {
                    println("Error parsing timestamp: $timestamp, ${e.message}")
                    // Fallback to original label
                    result.add(if (index < originalLabels.size) originalLabels[index] else "")
                }
            }
            
            // Print any duplicate dates found
            if (chartType == ChartType.BAR_DAILY) {
                convertedDateTimeMap.forEach { (date, indices) ->
                    if (indices.size > 1) {
                        println("WARNING: Duplicate date found: $date appears ${indices.size} times at indices ${indices.joinToString(", ")}")
                    }
                }
            }
            
            return result
        }

        // Add this helper method to handle duplicate dates
        private fun consolidateDuplicateDates(labels: List<String>, values: List<Float>): Pair<List<String>, List<Float>> {
            // Validate inputs
            if (labels.size != values.size) {
                println("ERROR: Labels and values lists have different sizes!")
                return Pair(labels, values)
            }
            
            // Create a map to consolidate values by date
            val dateValueMap = mutableMapOf<String, Float>()
            val dateCountMap = mutableMapOf<String, Int>()
            
            // Process each date and value pair
            labels.forEachIndexed { index, date ->
                if (index < values.size) {
                    val value = values[index]
                    
                    // If this date already exists, add the value to the existing one
                    if (date in dateValueMap) {
                        dateValueMap[date] = dateValueMap[date]!! + value
                        dateCountMap[date] = dateCountMap[date]!! + 1
                        println("Combining duplicate date: $date - existing: ${dateValueMap[date]!! - value}, new: $value, total: ${dateValueMap[date]}")
                    } else {
                        dateValueMap[date] = value
                        dateCountMap[date] = 1
                    }
                }
            }
            
            // Log any duplicates found
            dateCountMap.filter { it.value > 1 }.forEach { (date, count) ->
                println("Consolidated duplicate date: $date appeared $count times with total value ${dateValueMap[date]}")
            }
            
            // Convert the map back to sorted lists
            val sortedDates = dateValueMap.keys.sorted()
            val consolidatedValues = sortedDates.map { dateValueMap[it] ?: 0f }
            
            // Log final result
            println("After consolidation: ${sortedDates.size} unique dates (was ${labels.size})")
            if (sortedDates.isNotEmpty()) {
                println("First date: ${sortedDates.first()}, Last date: ${sortedDates.last()}")
            }
            
            return Pair(sortedDates, consolidatedValues)
        }

        /**
         * Filter data points to only include those from the selected month/year
         * Returns (filteredTimestamps, filteredValues)
         */
        private fun filterDataPointsByMonth(
            timestamps: List<String>,
            values: List<Float>,
            selectedMonth: Int,
            selectedYear: Int
        ): Pair<List<String>, List<Float>> {
            if (timestamps.isEmpty() || values.isEmpty()) {
                return Pair(timestamps, values)
            }
            
            val filteredTimestamps = mutableListOf<String>()
            val filteredValues = mutableListOf<Float>()
            
            timestamps.forEachIndexed { index, timestamp ->
                if (index >= values.size) return@forEachIndexed
                
                try {
                    // Parse timestamp to check month/year
                    val cal = parseTimestampToCalendar(timestamp)
                    if (cal != null) {
                        val month = cal.get(Calendar.MONTH)
                        val year = cal.get(Calendar.YEAR)
                        
                        // Only include if month/year matches selected month/year
                        if (month == selectedMonth && year == selectedYear) {
                            filteredTimestamps.add(timestamp)
                            filteredValues.add(values[index])
                        } else {
                            println("Skipping data point with date ${cal.time}: month $month != $selectedMonth or year $year != $selectedYear")
                        }
                    }
                } catch (e: Exception) {
                    println("Error parsing timestamp $timestamp: ${e.message}")
                }
            }
            
            return Pair(filteredTimestamps, filteredValues)
        }
        
        /**
         * Parse a timestamp to Calendar in local timezone
         */
        private fun parseTimestampToCalendar(timestamp: String): Calendar? {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ssXXX"
            )
            
            for (formatStr in formats) {
                try {
                    val format = SimpleDateFormat(formatStr, Locale.getDefault())
                    format.timeZone = TimeZone.getTimeZone("UTC")
                    
                    // Special handling for +00:00 format
                    val parsableTimestamp = if (timestamp.contains("+00:00")) {
                        timestamp.replace("+00:00", "Z")
                    } else {
                        timestamp
                    }
                    
                    val date = format.parse(parsableTimestamp)
                    if (date != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        return calendar
                    }
                } catch (e: Exception) {
                    // Try next format
                }
            }
            
            return null
        }
        
        /**
         * Format a timestamp to "DD MMM" format in IST timezone
         */
        private fun formatTimestampToDateLabel(timestamp: String): String {
            try {
                val date = parseTimestampToDate(timestamp)
                if (date == null) {
                    println("CRITICAL DEBUG: Could not parse timestamp: $timestamp")
                    return ""
                }
                val ist = TimeZone.getTimeZone("Asia/Kolkata")
                val formatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                formatter.timeZone = ist
                val label = formatter.format(date)
                val formattedLabel = label.split(" ").let {
                    if (it.size >= 2) "${it[0]} ${it[1].capitalize()}" else label
                }
                println("CRITICAL DEBUG: UTC $timestamp → IST $formattedLabel (IST offset: ${ist.rawOffset / 3600000.0}h)")
                return formattedLabel
            } catch (e: Exception) {
                println("CRITICAL DEBUG: Error formatting date label: ${e.message}")
                return ""
            }
        }
        
        /**
         * Format a timestamp to "HH:00" format in IST timezone
         */
        private fun formatTimestampToHourLabel(timestamp: String): String {
            try {
                val date = parseTimestampToDate(timestamp)
                if (date == null) {
                    println("CRITICAL DEBUG: Could not parse timestamp: $timestamp")
                    return ""
                }
                val ist = TimeZone.getTimeZone("Asia/Kolkata")
                val calendar = Calendar.getInstance(ist)
                calendar.time = date
                val hourFormat = String.format("%02d:00", calendar.get(Calendar.HOUR_OF_DAY))
                println("CRITICAL DEBUG: UTC $timestamp → IST hour $hourFormat (IST offset: ${ist.rawOffset / 3600000.0}h)")
                return hourFormat
            } catch (e: Exception) {
                println("CRITICAL DEBUG: Error formatting hour label: ${e.message}")
                return ""
            }
        }

        /**
         * Filter data points to only include those from the selected day (in IST)
         */
        private fun filterDataPointsByDay(
            timestamps: List<String>,
            values: List<Float>,
            selectedDay: Int,
            selectedMonth: Int,
            selectedYear: Int
        ): Pair<List<String>, List<Float>> {
            if (timestamps.isEmpty() || values.isEmpty()) {
                return Pair(timestamps, values)
            }
            val filteredTimestamps = mutableListOf<String>()
            val filteredValues = mutableListOf<Float>()
            val ist = TimeZone.getTimeZone("Asia/Kolkata")
            val localCal = Calendar.getInstance(ist)
            timestamps.forEachIndexed { index, timestamp ->
                if (index >= values.size) return@forEachIndexed
                try {
                    val date = parseTimestampToDate(timestamp) ?: return@forEachIndexed
                    localCal.time = date
                    val day = localCal.get(Calendar.DAY_OF_MONTH)
                    val month = localCal.get(Calendar.MONTH)
                    val year = localCal.get(Calendar.YEAR)
                    println("CRITICAL DEBUG: UTC $timestamp → IST $day-${month+1}-$year (selected: $selectedDay-${selectedMonth+1}-$selectedYear)")
                    if (day == selectedDay && month == selectedMonth && year == selectedYear) {
                        filteredTimestamps.add(timestamp)
                        filteredValues.add(values[index])
                    }
                } catch (e: Exception) {
                    println("CRITICAL DEBUG: Error filtering by day: ${e.message}")
                }
            }
            return Pair(filteredTimestamps, filteredValues)
        }

        // Helper method to parse a timestamp string to a Date object
        private fun parseTimestampToDate(timestamp: String): Date? {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ssXXX"
            )
            
            for (formatStr in formats) {
                try {
                    val format = SimpleDateFormat(formatStr, Locale.getDefault())
                    format.timeZone = TimeZone.getTimeZone("UTC")
                    
                    // Special handling for +00:00 format
                    val parsableTimestamp = if (timestamp.contains("+00:00")) {
                        timestamp.replace("+00:00", "Z")
                    } else {
                        timestamp
                    }
                    
                    return format.parse(parsableTimestamp)
                } catch (e: Exception) {
                    // Try next format
                }
            }
            
            return null
        }

        // Add this helper function to the same class (e.g., as a private function)
        private fun formatUtcToLocalDateLabel(utcTimestamp: String): String {
            return try {
                val utcZdt = java.time.ZonedDateTime.parse(utcTimestamp)
                val localZdt = utcZdt.withZoneSameInstant(java.time.ZoneId.systemDefault())
                val formatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM")
                localZdt.format(formatter)
            } catch (e: Exception) {
                utcTimestamp // fallback
            }
        }
    }

    inner class GaugeChartViewHolder(private val binding: ItemGaugeChartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set up click listeners
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChartConfigClickListener(chartConfigs[position])
                }
            }

            // Replace long-click with more options button click
            binding.moreOptionsButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMoreOptionsClickListener(chartConfigs[position], it, position)
                }
            }
        }

        fun bind(chartConfig: ChartConfig, chartData: ChartData?) {
            // For gauge charts, append the parameter name to the device name for the subtitle line
            val parameterName = getParameterName(chartConfig.parameterIds.firstOrNull() ?: 0)
            
            // Set title from user-defined chart title
            binding.chartTitle.text = chartConfig.title

            // Set device name with parameter in the second line
            if (parameterName.isNotEmpty()) {
                val deviceWithParameter = "${chartConfig.deviceName} - $parameterName"
                binding.siteName.text = deviceWithParameter
            } else {
            binding.siteName.text = chartConfig.deviceName
            }
            
            binding.lastUpdated.text = formatLastUpdated(chartConfig.lastUpdated)

            if (chartData == null) {
                // Show loading state
                binding.gaugeContainer.visibility = View.GONE
                // We need to add a loading indicator to the gauge chart layout
                return
            } else {
                binding.gaugeContainer.visibility = View.VISIBLE
            }

            // Get chart parameters
            val params = chartData.parameters
            
            // Set up the gauge
            setupGaugeChart(binding, params)
        }
        
        private fun updateGaugeWithData(binding: ItemGaugeChartBinding, chartData: ChartData, chartConfig: ChartConfig) {
            // Get chart parameters
            val params = chartData.parameters
            
            // Get unit from API data
            val unit = params["unit"] ?: ""

            // Get numeric values from parameters
            val value = params["value"]?.toDoubleOrNull() ?: 0.0
            val minValue = params["min"]?.toDoubleOrNull() ?: 0.0
            val maxValue = params["max"]?.toDoubleOrNull() ?: 45.0  // Standard 0-45 scale

            // Format title: "Device Name - Parameter Name"
            val parameterId = params["parameterId"]?.toIntOrNull() ?: 0
            val parameterName = getParameterName(parameterId)
            val titleWithParameter = "${chartConfig.deviceName} - $parameterName"
            binding.chartTitle.text = titleWithParameter

            // Format timestamp
            val timestamp = params["timestamp"] ?: ""
            binding.lastUpdated.text = formatTimestamp(timestamp)

            // Get the SpeedView (gauge)
            val gauge = binding.speedView

            // Set gauge range (always use our standard 0-45 scale for consistency)
            gauge.minSpeed = 0f
            gauge.maxSpeed = 45f
            
            // Update unit text in the gauge only if unit is available
            gauge.unit = unit

            // Read values from parameters or use defaults
            val lowValue = params["lowValue"]?.toFloatOrNull() ?: 15f
            val highValue = params["highValue"]?.toFloatOrNull() ?: 30f

            // Set up color sections for the gauge - use standard colors and ranges for consistency
            // Keep our standard 0-15-30-45 scale for color sections
            setupGaugeColorSections(gauge)

            // Set current speed (value)
            val speedValue = value.toFloat()
            
            // Check if value is within our gauge scale
            val validatedSpeed = when {
                speedValue < 0f -> 0f
                speedValue > 45f -> 45f
                else -> speedValue
            }
            
            // Setting speed to 0 first forces a redraw when setting to the actual value
            gauge.speedTo(0f, 0)
            gauge.speedTo(validatedSpeed, 1000)

            // Add a formatted text representing the actual value, with or without unit
            if (unit.isNotEmpty()) {
                binding.gaugeValue.text = String.format("%.2f %s", value, unit)
            } else {
                binding.gaugeValue.text = String.format("%.2f", value)
            }
            
            println("Gauge updated - Value: $value${if (unit.isEmpty()) "" else " $unit"}, Range: $minValue to $maxValue")
        }

        private fun setupGaugeChart(binding: ItemGaugeChartBinding, params: Map<String, String>) {
            try {
            // Extract values with fallbacks to prevent using incorrect values
            val minValue = params["min"]?.toFloatOrNull() ?: 0f
                val maxValue = params["max"]?.toFloatOrNull() ?: 45f
                
                // Ensure max is greater than min
                val safeMinValue = minValue
                val safeMaxValue = if (maxValue <= minValue) minValue + 1f else maxValue
                
                val currentValue = params["value"]?.toFloatOrNull()?.coerceIn(safeMinValue, safeMaxValue) ?: safeMinValue
                val lowValue = params["lowValue"]?.toFloatOrNull()
                val lowLowValue = params["lowLowValue"]?.toFloatOrNull()
                val highValue = params["highValue"]?.toFloatOrNull()
                val highHighValue = params["highHighValue"]?.toFloatOrNull()
                val rawValue = params["raw_value"]?.toDoubleOrNull()
                val multiplier = params["multiplier"]?.toFloatOrNull() ?: 1f
                val unit = params["unit"] ?: ""

            // Log the values for debugging
                println("GaugeChart values from API - min: $safeMinValue, max: $safeMaxValue, current: $currentValue")
                println("Additional thresholds - lowLow: $lowLowValue, low: $lowValue, high: $highValue, highHigh: $highHighValue")
                println("Multiplier: $multiplier, raw value: $rawValue")

            // Access SpeedView and other views from binding
            val speedView = binding.speedView
            val minValueText = binding.minValue
            val maxValueText = binding.maxValue
            
            // Access value display TextViews
            val gaugeValue = binding.gaugeValue
            val gaugeUnit = binding.gaugeUnit
            
            // Hide bottom value that we don't need
            binding.bottomValue.visibility = View.GONE
            binding.bottomValueUnit.visibility = View.GONE
            
                // Format the value consistently - exactly 2 decimal places (match website format)
            val formattedValue = String.format(Locale.US, "%.2f", currentValue)
            
            // Configure the SpeedView to match the minimalist blue gauge design
            with(speedView) {
                    try {
                        // IMPORTANT: Use our standard scale for all gauge functionality
                        // This is critical - ALWAYS use the 0-45 scale for gauge sections AND needle
                        val standardMaxValue = 45f
                        val standardMinValue = 0f
                        
                        // Set min/max speed to our STANDARD scale values (not the API values)
                        // This ensures the needle position matches our color sections
                        minSpeed = standardMinValue
                        maxSpeed = standardMaxValue
                        
                        // Clear any existing sections
                        clearSections()
                        
                        // Set speedometer attributes before adding sections
                        // This helps prevent layout issues
                setStartDegree(180)
                setEndDegree(360)
                speedometerMode = Speedometer.Mode.TOP
                        speedometerWidth = dpToPx(16).toFloat() // Slightly thicker for the taller meter
                        
                        // Use our standard fixed tick values that match our color sections
                        val standardTickValues = arrayOf(0, 8, 15, 23, 30, 38, 45)
                        
                        // ALWAYS use our standard 0-15-30 threshold sections for ALL gauges
                        // This ensures consistent color bands across all gauge charts
                        println("Using standard 0-15-30 threshold sections for ALL gauges")
                        setupGaugeColorSections(this)
                
                // Style the tick marks
                        tickNumber = standardTickValues.size
                tickPadding = 28f // Adjusted padding for taller gauge
                
                        // Set custom tick labels - ALWAYS show our standard scale values
                        // This ensures the tick labels match our color section boundaries
                        onPrintTickLabel = { i, _ ->
                            try {
                                // Simply display our standard tick values
                                // This ensures the labels align with our color sections
                                standardTickValues[i].toString()
                            } catch (e: Exception) {
                                // Fallback to tick index
                                i.toString()
                            }
                }
                
                // Configure the needle
                withTremble = false // Disable tremble for a steady needle
                speedTextColor = Color.TRANSPARENT // Hide built-in speed text since we're using our own
                unitTextColor = Color.TRANSPARENT // Hide built-in unit text
                
                // Make the needle blue as in the reference image - use the available properties
                with(indicator) {
                    color = Color.parseColor("#1E90FF") // Blue color for the needle
                    width = 22f // Slightly thicker needle for taller gauge
                }
                centerCircleColor = Color.parseColor("#333333") // Dark gray center
                    } catch (e: Exception) {
                        println("Error during speedView configuration: ${e.message}")
                    }
                    
                    try {
                        // CRITICAL: For needle positioning, we need to directly use the actual value 
                        // on our standard scale - don't normalize it from the API's scale
                        // This ensures the needle directly shows the actual value on our standard 0-45 scale
                        
                        // DIRECT VALUE APPROACH: Use the actual current value directly
                        // If the value is 15.85, we want the needle exactly at 15.85
                        val directScaleValue = currentValue.coerceIn(0f, 45f)
                        
                        // Log all values to debug the positioning
                        println("Gauge value: $currentValue, using direct value: $directScaleValue")
                        println("Gauge min-max: $safeMinValue-$safeMaxValue, standard scale: 0-45")
                        
                        // Set the needle directly to the actual value without animation first
                        speedTo(directScaleValue, 0)
                        
                        // Then apply a smooth animation after a short delay
                        postDelayed({
                            try {
                                // Animate to the direct value
                                speedTo(directScaleValue, 1000)
                            } catch (e: Exception) {
                                println("Error animating gauge: ${e.message}")
                                // If animation fails, make sure value is set anyway
                                speedTo(directScaleValue, 0)
                            }
                        }, 100)
                    } catch (e: Exception) {
                        println("Error setting gauge value: ${e.message}")
                    }
                }
                
                // First set up the text values (before setting visibility)
            gaugeValue.text = formattedValue
            gaugeValue.setTextColor(Color.parseColor("#FFD700")) // Golden yellow color
            
                // Only set unit text if unit is available
                if (unit.isNotEmpty()) {
            gaugeUnit.text = unit
                    gaugeUnit.visibility = View.VISIBLE
                } else {
                    gaugeUnit.visibility = View.GONE
                }
            gaugeUnit.setTextColor(Color.GRAY)
            
            // Hide the min/max label views as they're not in the reference image
            minValueText.visibility = View.GONE
            maxValueText.visibility = View.GONE
            
                // Show the value container only after setting up text values
                binding.valueContainer.visibility = View.VISIBLE
                
                try {
                    // Apply a fade-in for the values - wrapped in try/catch to prevent layout crashes
            val fadeIn = AlphaAnimation(0.0f, 1.0f)
                    fadeIn.duration = 300  // Reduced duration for better performance
                    fadeIn.startOffset = 200  // Reduced offset for faster response
                    fadeIn.fillAfter = true  // Ensure animation state is maintained
                    binding.valueContainer.clearAnimation()  // Clear any existing animations first
            binding.valueContainer.startAnimation(fadeIn)
                } catch (e: Exception) {
                    println("Animation error: ${e.message}")
                    // If animation fails, make sure view is still visible
                    binding.valueContainer.visibility = View.VISIBLE
                }
            
            // Log completion
                println("Semi-circular gauge chart setup complete with value: $currentValue, displayed as: $formattedValue")
            } catch (e: Exception) {
                // Top-level error handling for the entire function
                println("Fatal error in setupGaugeChart: ${e.message}")
                e.printStackTrace()
                
                // Make sure at least the value is displayed even if gauge fails
                try {
                    // Format value and show in text view
                    val value = params["value"]?.toFloatOrNull() ?: 0f
                    val formattedValue = String.format(Locale.US, "%.2f", value)
                    binding.gaugeValue.text = formattedValue
                    binding.gaugeUnit.text = params["unit"] ?: ""
                    binding.valueContainer.visibility = View.VISIBLE
                } catch (e2: Exception) {
                    // Last resort error handling
                    println("Even fallback value display failed: ${e2.message}")
                }
            }
        }

        // Helper method to add default gauge sections when API doesn't provide proper thresholds
        private fun SpeedView.addDefaultSections(width: Float) {
            try {
                // Create sections using values from the document with fixed colors:
                // Red (minValue to lowLowValue), Gold (lowLowValue to lowValue),
                // Green (lowValue to highValue), Gold (highValue to highHighValue), 
                // Red (highHighValue to maxValue)
                val sections = arrayOf(
                    // Section 1: Red (0 to lowLowValue - using 8 as the standard threshold)
                    Section(0f/45f, 8f/45f, Color.parseColor("#f87357"), width),
                    // Section 2: Gold (lowLowValue to lowValue - using 8-15 as the standard threshold)
                    Section(8f/45f, 15f/45f, Color.parseColor("#FFD700"), width),
                    // Section 3: Green (lowValue to highValue - using 15-30 as the standard threshold)
                    Section(15f/45f, 30f/45f, Color.parseColor("#8af857"), width),
                    // Section 4: Gold (highValue to highHighValue - using 30-38 as the standard threshold)
                    Section(30f/45f, 38f/45f, Color.parseColor("#FFD700"), width),
                    // Section 5: Red (highHighValue to maxValue - using 38-45 as the standard threshold)
                    Section(38f/45f, 45f/45f, Color.parseColor("#f87357"), width)
                )
                
                println("Creating gauge sections based on document: Red(0-8), Gold(8-15), Green(15-30), Gold(30-38), Red(38-45)")
                
                clearSections() // Clear any existing sections first
                addSections(*sections) // Add all sections at once using spread operator
                
                println("Added standard sections with ranges from documentation")
            } catch (e: Exception) {
                println("Error adding standard sections: ${e.message}")
                // Try adding one by one if adding all at once fails
                try {
                    clearSections()
                    
                    // Section 1: Red (0 to lowLowValue - using 8 as the standard threshold)
                    addSections(Section(0f/45f, 8f/45f, Color.parseColor("#f87357"), width))
                    // Section 2: Gold (lowLowValue to lowValue - using 8-15 as the standard threshold)
                    addSections(Section(8f/45f, 15f/45f, Color.parseColor("#FFD700"), width))
                    // Section 3: Green (lowValue to highValue - using 15-30 as the standard threshold)
                    addSections(Section(15f/45f, 30f/45f, Color.parseColor("#8af857"), width))
                    // Section 4: Gold (highValue to highHighValue - using 30-38 as the standard threshold)
                    addSections(Section(30f/45f, 38f/45f, Color.parseColor("#FFD700"), width))
                    // Section 5: Red (highHighValue to maxValue - using 38-45 as the standard threshold)
                    addSections(Section(38f/45f, 45f/45f, Color.parseColor("#f87357"), width))
                    
                    println("Added sections one by one successfully")
                } catch (e2: Exception) {
                    println("Error adding sections one by one: ${e2.message}")
                    // Last resort - try with simplified sections that still follow color pattern
                    try {
                        clearSections()
                        
                        // Simplified fallback sections
                        addSections(Section(0f/45f, 15f/45f, Color.parseColor("#f87357"), width))  // Red (0-15)
                        addSections(Section(15f/45f, 30f/45f, Color.parseColor("#8af857"), width)) // Green (15-30)
                        addSections(Section(30f/45f, 45f/45f, Color.parseColor("#f87357"), width)) // Red (30-45)
                        
                        println("Using simplified fallback sections")
                    } catch (e3: Exception) {
                        println("Even simplified sections failed: ${e3.message}")
                    }
                }
            }
        }

        // Helper method to convert dp to pixels
        private fun dpToPx(dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

        // Add formatTimestamp helper method
        private fun formatTimestamp(timestamp: String): String {
            if (timestamp.isEmpty()) return context.getString(R.string.not_updated_yet)
            
            try {
                // Try to parse the timestamp as a date
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
                dateFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = dateFormat.parse(timestamp)
                
                if (date != null) {
                    val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    outputFormat.timeZone = TimeZone.getDefault()
                    return outputFormat.format(date)
                }
            } catch (e: Exception) {
                println("Error formatting timestamp: $timestamp - ${e.message}")
            }
            
            // Return original if parsing fails
            return timestamp
        }
        
        // Add setupGaugeColorSections method
        private fun setupGaugeColorSections(gauge: SpeedView) {
            try {
                // Create sections using values from the document with fixed colors:
                // Red (minValue to lowLowValue), Gold (lowLowValue to lowValue),
                // Green (lowValue to highValue), Gold (highValue to highHighValue), 
                // Red (highHighValue to maxValue)
                val width = dpToPx(16).toFloat()
                
                // Clear any existing sections first
                gauge.clearSections()
                
                // Standard 0-45 scale with sections matching the document
                // Section 1: Red (0 to lowLowValue - using 8 as the standard threshold)
                gauge.addSections(Section(0f/45f, 8f/45f, Color.parseColor("#f87357"), width))
                
                // Section 2: Gold (lowLowValue to lowValue - using 8-15 as the standard threshold)
                gauge.addSections(Section(8f/45f, 15f/45f, Color.parseColor("#FFD700"), width)) // Gold color
                
                // Section 3: Green (lowValue to highValue - using 15-30 as the standard threshold)
                gauge.addSections(Section(15f/45f, 30f/45f, Color.parseColor("#8af857"), width))
                
                // Section 4: Gold (highValue to highHighValue - using 30-38 as the standard threshold)
                gauge.addSections(Section(30f/45f, 38f/45f, Color.parseColor("#FFD700"), width)) // Gold color
                
                // Section 5: Red (highHighValue to maxValue - using 38-45 as the standard threshold)
                gauge.addSections(Section(38f/45f, 45f/45f, Color.parseColor("#f87357"), width))
                
                println("Added gauge sections based on document: Red(0-8), Gold(8-15), Green(15-30), Gold(30-38), Red(38-45)")
            } catch (e: Exception) {
                println("Error adding gauge sections: ${e.message}")
                // Try fallback with simpler sections if adding all fails
                try {
                    gauge.clearSections()
                    val width = dpToPx(16).toFloat()
                    
                    // Simplified fallback sections
                    gauge.addSections(Section(0f/45f, 15f/45f, Color.parseColor("#f87357"), width))  // Red
                    gauge.addSections(Section(15f/45f, 30f/45f, Color.parseColor("#8af857"), width)) // Green
                    gauge.addSections(Section(30f/45f, 45f/45f, Color.parseColor("#f87357"), width)) // Red
                    
                    println("Using simplified gauge sections as fallback")
                } catch (e2: Exception) {
                    println("Even simplified gauge sections failed: ${e2.message}")
                }
            }
        }
    }

    inner class MetricChartViewHolder(private val binding: ItemMetricChartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set up click listeners
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onChartConfigClickListener(chartConfigs[position])
                }
            }

            // Replace long-click with more options button click
            binding.moreOptionsButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMoreOptionsClickListener(chartConfigs[position], it, position)
                }
            }
        }

        fun bind(chartConfig: ChartConfig, chartData: ChartData?) {
            // For metric charts, use the user-defined chart title
            binding.chartTitle.text = chartConfig.title
            
            binding.lastUpdated.text = formatLastUpdated(chartConfig.lastUpdated)

            if (chartData == null) {
                // Show loading state
                binding.cardsContainer.visibility = View.GONE
                // We could add a loading indicator to the metric chart layout
                return
            } else {
                binding.cardsContainer.visibility = View.VISIBLE
            }

            // Get chart parameters
            val params = chartData.parameters
            
            // Set up the metrics with dynamic cards
            setupMetricChart(binding, params, chartConfig.deviceName)
        }

        private fun setupMetricChart(binding: ItemMetricChartBinding, params: Map<String, String>, deviceName: String) {
            // Log the actual parameters for debugging
            android.util.Log.d("MetricChart", "All parameters: ${params.entries.joinToString()}")

            // Get parameter IDs from the parameters map (stored as comma-separated values)
            val parameterIdsStr = params["parameterIds"] ?: ""
            val parameterIds = parameterIdsStr.split(",").filter { it.isNotEmpty() }

            // Clear existing cards
            binding.cardsContainer.removeAllViews()

            // Find all parameter values from the response
            // Store as paramId to value pairs
            val paramPairs = mutableListOf<Pair<String, String>>() // paramId to value

            // Add any numeric parameters we find (paramId -> value)
            for (entry in params.entries) {
                // Skip non-parameter entries (like timestamp, parameterIds, etc.)
                if (entry.key != "parameterIds" && entry.key != "timestamp" &&
                    entry.key.toIntOrNull() != null && entry.value.toDoubleOrNull() != null) {
                    paramPairs.add(Pair(entry.key, entry.value))
                }
            }

            // Get timestamp from parameters
            val timestamp = params["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            
            // Sort parameters to ensure consistent display order
            // Power parameters first, then Energy parameters, then others
            val sortedParams = paramPairs.sortedWith(compareBy(
                // Sort by parameter type (Power first, then Energy, then others)
                { pair ->
                    val paramId = pair.first.toIntOrNull() ?: 0
                    val paramName = getParameterName(paramId).lowercase()
                    when {
                        paramName.contains("power") -> 0
                        paramName.contains("energy") -> 1
                        else -> 2
                    }
                },
                // Then by parameter ID
                { it.first.toIntOrNull() ?: 0 }
            ))
            
            // Process each parameter and create a card for it
            sortedParams.forEach { (paramId, valueStr) ->
                val parameterId = paramId.toIntOrNull() ?: 0
                val paramName = getParameterName(parameterId)
                val unitKey = "unit_$parameterId"
                val unit = params[unitKey] ?: ""
                
                // Skip parameters where we couldn't get name or value
                if (paramName.isEmpty()) return@forEach
                
                // Create a new card view for this parameter by inflating our custom layout
                val cardView = LayoutInflater.from(context).inflate(
                    R.layout.item_metric_card,
                    binding.cardsContainer,
                    false
                ) as androidx.cardview.widget.CardView
                
                // Set up the card views
                val metricTitle = cardView.findViewById<TextView>(R.id.metricTitle)
                val metricValue = cardView.findViewById<TextView>(R.id.metricValue)
                val metricUnit = cardView.findViewById<TextView>(R.id.metricUnit)
                val metricTimestamp = cardView.findViewById<TextView>(R.id.metricTimestamp)
                
                // Set the parameter name with device name
                metricTitle.text = "$deviceName -\n$paramName"
                
                // Set the value
                metricValue.text = formatMetricValue(valueStr, "decimal")
                
                // Apply color based on the value boundaries from the document
                try {
                    // Get threshold values for this parameter
                    val value = valueStr.toDoubleOrNull()
                    
                    // Check if value is numeric
                    if (value != null) {
                        // Add logging to see if we have any parameters related to thresholds
                        val paramKeys = params.keys.filter { it.contains(parameterId.toString()) }
                        android.util.Log.d("MetricChart", "Parameter $parameterId ($paramName): value=$value")
                        android.util.Log.d("MetricChart", "Available param keys: $paramKeys")
                        
                        // Get boundaries for this parameter with logging
                        val minValueKey = "minValue_$parameterId"
                        val lowLowValueKey = "lowLowValue_$parameterId"
                        val lowValueKey = "lowValue_$parameterId" 
                        val highValueKey = "highValue_$parameterId"
                        val highHighValueKey = "highHighValue_$parameterId"
                        val maxValueKey = "maxValue_$parameterId"
                        
                        val minValue = params[minValueKey]?.toDoubleOrNull() 
                        val lowLowValue = params[lowLowValueKey]?.toDoubleOrNull()
                        val lowValue = params[lowValueKey]?.toDoubleOrNull()
                        val highValue = params[highValueKey]?.toDoubleOrNull() 
                        val highHighValue = params[highHighValueKey]?.toDoubleOrNull()
                        val maxValue = params[maxValueKey]?.toDoubleOrNull()
                        
                        android.util.Log.d("MetricChart", "Looking for keys: $minValueKey, $lowLowValueKey, $lowValueKey, $highValueKey, $highHighValueKey, $maxValueKey")
                        android.util.Log.d("MetricChart", "Found values: min=$minValue, lowLow=$lowLowValue, low=$lowValue, high=$highValue, highHigh=$highHighValue, max=$maxValue")
                        
                        // Check if we have any threshold values
                        val hasThresholds = (minValue != null || lowLowValue != null || lowValue != null || 
                                            highValue != null || highHighValue != null || maxValue != null)
                        
                        if (!hasThresholds) {
                            android.util.Log.d("MetricChart", "No thresholds found, using fallback thresholds")
                            
                            // Use different fallback thresholds based on parameter name (power vs energy)
                            val isPower = paramName.lowercase().contains("power")
                            val isEnergy = paramName.lowercase().contains("energy")
                            
                            // Create fallback thresholds based on the current value
                            // This makes color visible immediately even without proper API thresholds
                            val fallbackMinValue = 0.0
                            val fallbackMaxValue: Double
                            
                            if (isPower) {
                                fallbackMaxValue = when {
                                    value > 10.0 -> value * 1.5
                                    value > 1.0 -> 10.0
                                    else -> 2.0
                                }
                                
                                // Calculate thresholds based on fallback range
                                val fallbackLowLowValue = fallbackMaxValue * 0.2
                                val fallbackLowValue = fallbackMaxValue * 0.3
                                val fallbackHighValue = fallbackMaxValue * 0.7
                                val fallbackHighHighValue = fallbackMaxValue * 0.8
                                
                                // Apply color based on where the value falls within the fallback range
                                when {
                                    (value >= fallbackMinValue && value <= fallbackLowLowValue) || 
                                    (value >= fallbackHighHighValue && value <= fallbackMaxValue) -> {
                                        metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_red))
                                        android.util.Log.d("MetricChart", "Using RED color (fallback thresholds)")
                                    }
                                    (value > fallbackLowLowValue && value <= fallbackLowValue) || 
                                    (value >= fallbackHighValue && value < fallbackHighHighValue) -> {
                                        metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_gold))
                                        android.util.Log.d("MetricChart", "Using GOLD color (fallback thresholds)")
                                    }
                                    (value > fallbackLowValue && value < fallbackHighValue) -> {
                                        metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_green))
                                        android.util.Log.d("MetricChart", "Using GREEN color (fallback thresholds)")
                                    }
                                    else -> {
                                        metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_default))
                                        android.util.Log.d("MetricChart", "Using DEFAULT color (fallback thresholds)")
                                    }
                                }
                            } else {
                                // For energy or other values, just use accent color
                                metricValue.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                                android.util.Log.d("MetricChart", "Using accent color for Energy/Other parameter")
                            }
                        } else {
                            // Use API provided thresholds (if any are available)
                            // Fill in missing values with reasonable defaults
                            val finalMinValue = minValue ?: 0.0
                            val finalLowLowValue = lowLowValue ?: (value * 0.3)
                            val finalLowValue = lowValue ?: (value * 0.5)
                            val finalHighValue = highValue ?: (value * 1.5)
                            val finalHighHighValue = highHighValue ?: (value * 1.8)
                            val finalMaxValue = maxValue ?: (value * 2.0)
                            
                            android.util.Log.d("MetricChart", "Using thresholds: min=$finalMinValue, lowLow=$finalLowLowValue, " + 
                                              "low=$finalLowValue, high=$finalHighValue, highHigh=$finalHighHighValue, max=$finalMaxValue")
                            
                            // Apply color according to document rules
                            when {
                                // Red: If value between minValue and lowLowValue OR between highHighValue and maxValue
                                (value >= finalMinValue && value <= finalLowLowValue) || 
                                (value >= finalHighHighValue && value <= finalMaxValue) -> {
                                    metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_red))
                                    android.util.Log.d("MetricChart", "Using RED color")
                                }
                                
                                // Gold: If value between lowLowValue and lowValue OR between highValue and highHighValue
                                (value > finalLowLowValue && value <= finalLowValue) || 
                                (value >= finalHighValue && value < finalHighHighValue) -> {
                                    metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_gold))
                                    android.util.Log.d("MetricChart", "Using GOLD color")
                                }
                                
                                // Green: If value strictly higher than lowValue and strictly lower than highValue
                                (value > finalLowValue && value < finalHighValue) -> {
                                    metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_green))
                                    android.util.Log.d("MetricChart", "Using GREEN color")
                                }
                                
                                // Default/Black: If value exceeds the maxValue or minValue
                                else -> {
                                    metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_default))
                                    android.util.Log.d("MetricChart", "Using DEFAULT color")
                                }
                            }
                        }
                    } else {
                        // Default/Black: If value is string / not numerical
                        metricValue.setTextColor(ContextCompat.getColor(context, R.color.metric_default))
                        android.util.Log.d("MetricChart", "Value is not numeric, using DEFAULT color")
                    }
                } catch (e: Exception) {
                    // Fallback to previous implementation if there's an error
                    android.util.Log.e("MetricChart", "Error applying color: ${e.message}")
                    
                    when {
                        paramName.lowercase().contains("power") -> 
                            metricValue.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                        paramName.lowercase().contains("energy") -> 
                            metricValue.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        paramName.lowercase().contains("status") ->
                            metricValue.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        else -> 
                            metricValue.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }
                }
                
                // Set unit if available
                if (unit.isNotEmpty()) {
                    metricUnit.text = unit
                    metricUnit.visibility = View.VISIBLE
                } else {
                    metricUnit.visibility = View.GONE
                }
                
                // Set timestamp
                metricTimestamp.text = formatTimestamp(timestamp)

                // Add the card to the container
                binding.cardsContainer.addView(cardView)
                
                android.util.Log.d("MetricChart", "Added card for parameter $parameterId: $paramName = $valueStr $unit")
            }
            
            // If no parameters were added, show a message
            if (sortedParams.isEmpty()) {
                val textView = TextView(context)
                textView.text = "No parameters data available"
                textView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                textView.gravity = android.view.Gravity.CENTER
                textView.setPadding(16, 16, 16, 16)
                binding.cardsContainer.addView(textView)
            }
        }

        private fun formatMetricValue(value: String, format: String?): String {
            val numericValue = value.toDoubleOrNull() ?: return value

            // Customize formatting based on size of number
            return when {
                numericValue >= 100000 -> String.format("%.1f", numericValue) // 1 decimal place for very large numbers
                numericValue >= 100 -> String.format("%.2f", numericValue) // 2 decimal places for large numbers
                else -> String.format("%.2f", numericValue) // 2 decimal places for small numbers
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            if (timestamp <= 0) return ""

            // Ensure we're formatting in local timezone
            val dateFormat = SimpleDateFormat("dd/MM/yyyy, H:mm:ss", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getDefault() // Explicitly set to local timezone
            return dateFormat.format(Date(timestamp))
        }
    }

    private fun formatLastUpdated(timestamp: Long): String {
        if (timestamp <= 0) return context.getString(R.string.not_updated_yet)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault() // Explicitly set to local timezone
        return context.getString(R.string.last_updated, dateFormat.format(Date(timestamp)))
    }
}