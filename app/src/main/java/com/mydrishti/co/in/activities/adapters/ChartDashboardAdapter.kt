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
import kotlin.math.max
import kotlin.math.abs
// No need for additional Math import

class ChartDashboardAdapter(
    private val context: Context,
    private val chartConfigs: MutableList<ChartConfig>,
    private val onChartConfigClickListener: (ChartConfig) -> Unit,
    private val onChartConfigLongClickListener: (ChartConfig, Int) -> Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_BAR_DAILY = 0
        private const val VIEW_TYPE_BAR_HOURLY = 1
        private const val VIEW_TYPE_GAUGE = 2
        private const val VIEW_TYPE_METRIC = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BAR_DAILY, VIEW_TYPE_BAR_HOURLY -> {
                val binding = ItemBarChartBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                BarChartViewHolder(binding)
            }

            VIEW_TYPE_GAUGE -> {
                val binding = ItemGaugeChartBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                GaugeChartViewHolder(binding)
            }

            VIEW_TYPE_METRIC -> {
                val binding = ItemMetricChartBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                MetricChartViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chartConfig = chartConfigs[position]

        holder.itemView.setOnClickListener {
            onChartConfigClickListener(chartConfig)
        }

        holder.itemView.setOnLongClickListener {
            onChartConfigLongClickListener(chartConfig, position)
        }

        when (holder) {
            is BarChartViewHolder -> holder.bind(chartConfig)
            is GaugeChartViewHolder -> holder.bind(chartConfig)
            is MetricChartViewHolder -> holder.bind(chartConfig)
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

    /**
     * Returns the current list of chart configurations
     */
    fun getChartConfigs(): List<ChartConfig> {
        return chartConfigs.toList()
    }

    /**
     * Handles moving an item from one position to another
     */
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
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
        return true
    }

    fun updateCharts(newChartConfigs: List<ChartConfig>) {
        val diffCallback = ChartDiffCallback(chartConfigs, newChartConfigs)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        chartConfigs.clear()
        chartConfigs.addAll(newChartConfigs)

        diffResult.dispatchUpdatesTo(this)
    }

    inner class BarChartViewHolder(private val binding: ItemBarChartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // Helper array for month names - available to all methods in this class
        private val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

        // Helper function to convert dp to pixels
        private fun dpToPx(dp: Int): Int {
            val density = context.resources.displayMetrics.density
            return (dp * density).toInt()
        }

        fun bind(chartConfig: ChartConfig) {
            binding.chartTitle.text = chartConfig.title
            binding.siteName.text = chartConfig.siteName
            binding.lastUpdated.text = formatLastUpdated(chartConfig.lastUpdated)

            // Display the unit in the dedicated unit TextView
            val unitText = binding.root.findViewById<TextView>(R.id.unitText)
            val unit = chartConfig.parameters["unit"] ?: "kWh"
            unitText.text = "Unit: $unit"

            // Setup date or month selector based on chart type
            setupSelectors(chartConfig)

            // Initially setup chart with current data
            setupBarChart(binding.barChart, chartConfig)
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

            // Set initial month display text - simplified to just show month name
            val formatter = SimpleDateFormat("MMM", Locale.getDefault())
            cal.set(Calendar.MONTH, currentSystemMonth)
            cal.set(Calendar.YEAR, currentSystemYear)
            cal.set(Calendar.DAY_OF_MONTH, 1)

            // Display current month initially - only the month name
            val initialMonthText = formatter.format(cal.time)
            binding.tvMonthDisplay.text = initialMonthText

            // Create month dropdown options
            val monthOptions = ArrayList<String>()
            val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())

            // Add all months of the year as options
            for (month in 0..11) {
                cal.set(Calendar.MONTH, month)
                val formattedMonth = monthFormatter.format(cal.time)
                monthOptions.add(formattedMonth)
            }

            // Set up click listener for the month dropdown icon
            binding.monthDropdownIcon.setOnClickListener {
                // Show month selection popup/dialog
                val popup = android.widget.PopupMenu(context, binding.monthDropdownIcon)
                for (i in monthOptions.indices) {
                    popup.menu.add(0, i, i, monthOptions[i])
                }

                // Handle month selection
                popup.setOnMenuItemClickListener { item ->
                    val selectedMonth = item.itemId
                    binding.tvMonthDisplay.text = monthOptions[selectedMonth]

                    // Generate data for selected month
                    val updatedConfig = simulateMonthlyData(
                        chartConfig,
                        selectedMonth,
                        currentSystemYear
                    )

                    // Add flag to force scroll to zero
                    val params = HashMap(updatedConfig.parameters)
                    params["forceScrollToZero"] = "true"
                    val finalConfig = updatedConfig.copy(parameters = params)

                    // Set up chart with the new data
                    setupBarChart(binding.barChart, finalConfig)
                    true
                }

                // Show the popup menu
                popup.show()
            }

            // Initial setup with current month's data
            val initialConfig = simulateMonthlyData(chartConfig, currentSystemMonth, currentSystemYear)
            setupBarChart(binding.barChart, initialConfig)
            println("Month selector setup complete with calendar icon")
        }

        private fun setupDateSelector(chartConfig: ChartConfig) {
            binding.dateSelectionLayout.visibility = View.VISIBLE
            binding.monthSelectionLayout.visibility = View.GONE

            // Get current date information
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)
            val currentHour = cal.get(Calendar.HOUR_OF_DAY)
            val currentMinute = cal.get(Calendar.MINUTE)

            // Format and display the current date in simplified format (day+month only)
            val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

            cal.set(Calendar.YEAR, currentYear)
            cal.set(Calendar.MONTH, currentMonth)
            cal.set(Calendar.DAY_OF_MONTH, currentDay)

            // Show current day in simplified format
            val initialDateText = dateFormatter.format(cal.time)
            binding.tvDateDisplay.text = initialDateText

            // Show current time
            val initialTimeText = timeFormatter.format(cal.time)
            binding.tvTimeDisplay.text = initialTimeText

            // Set up click listener for the date calendar icon
            binding.dateCalendarIcon.setOnClickListener {
                // Create a date picker dialog for date selection
                val datePickerDialog = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        // When user selects a date
                        cal.set(Calendar.YEAR, year)
                        cal.set(Calendar.MONTH, month)
                        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                        // Format and display the selected date in simplified format
                        val selectedDate = dateFormatter.format(cal.time)
                        binding.tvDateDisplay.text = selectedDate

                        // After selecting date, show time picker
                        val timePickerDialog = android.app.TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                // When user selects a time
                                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                cal.set(Calendar.MINUTE, minute)

                                // Format and display the selected time
                                val selectedTime = timeFormatter.format(cal.time)
                                binding.tvTimeDisplay.text = selectedTime

                                // Generate data for selected day and time
                                val updatedConfig = simulateHourlyData(chartConfig, dayOfMonth)

                                // Set up chart with the new data
                                setupBarChart(binding.barChart, updatedConfig)
                            },
                            currentHour,
                            currentMinute,
                            true // 24-hour format
                        )

                        // Show the time picker dialog
                        timePickerDialog.show()
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
            }

            // Initial setup with current day's data
            val initialConfig = simulateHourlyData(chartConfig, currentDay)
            setupBarChart(binding.barChart, initialConfig)
            println("Date selector setup complete with calendar icon")
        }

        private fun setupBarChart(barChart: CombinedChart, chartConfig: ChartConfig) {
            // Clear any existing data or settings
            barChart.clear()

            // Extract chart type and data from config
            val chartType = chartConfig.chartType
            val params = chartConfig.parameters

            // Determine chart type
            val isDailyChart = (chartType == ChartType.BAR_DAILY)
            val isHourlyChart = (chartType == ChartType.BAR_HOURLY)
            val isDailyData = params["isDaily"] == "true"
            val isHourlyData = params["isHourly"] == "true"

            println("Setting up chart: type=${chartType}, isDailyData=${isDailyData}, isHourlyData=${isHourlyData}")

            // Extract labels and values
            val labels = params["labels"]?.split(",")?.filter { it.isNotBlank() } ?: listOf()
            val values = params["values"]?.split(",")?.mapNotNull { it.toFloatOrNull() } ?: listOf()
            val unit = params["unit"] ?: "kWh"

            // Basic validation
            if (labels.isEmpty() || values.isEmpty() || labels.size != values.size) {
                println("ERROR: Invalid data. Labels: ${labels.size}, Values: ${values.size}")
                barChart.setNoDataText("No data available")
                barChart.invalidate()
                return
            }

            println("Chart data: ${labels.size} labels, ${values.size} values")
            println("Labels: ${labels.joinToString(", ")}")

            // Create bar entries
            val entries = values.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            // Create and configure the dataset
            val dataSet = BarDataSet(entries, chartConfig.title)
            dataSet.colors = listOf(
                ContextCompat.getColor(context, R.color.colorPrimary)
            )
            dataSet.valueTextSize = 10f
            dataSet.setDrawValues(true)

            // Value formatter - show values with 1 decimal place
            dataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.1f", value)
                }
            }

            // Calculate average for the reference line
            val average = if (values.isNotEmpty()) values.sum() / values.size else 0f

            // Create bar data with wider bars - adjusted for better visibility
            val barData = BarData(dataSet)
            barData.barWidth = 0.8f  // Adjusted from 0.7f for better bar width

            // Create average line dataset
            val avgEntries = values.mapIndexed { index, _ -> Entry(index.toFloat(), average) }
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
            xAxis.textSize = 9f  // Slightly smaller text for more labels to fit

            // Force labels for all positions
            xAxis.setLabelCount(labels.size, false)  // Force labels for all bars

            // Adjust label rotation to fit all labels
            xAxis.labelRotationAngle = 45f  // Fixed angle for better readability

            // Use a custom formatter for X-axis labels
            xAxis.valueFormatter = object : IndexAxisValueFormatter(labels) {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index >= 0 && index < labels.size) labels[index] else ""
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
            xAxis.axisMaximum = labels.size - 1 + rightPadding

            // Explicitly tell chart the bounds - this helps with scrolling
            barChart.setVisibleXRange(2f, 7.5f)
            println("Setting X axis bounds from ${xAxis.axisMinimum} to ${xAxis.axisMaximum} with ${labels.size} labels")

            // Configure Y-axis
            val leftAxis = barChart.axisLeft
            leftAxis.setDrawGridLines(true)
            leftAxis.gridColor = ContextCompat.getColor(context, android.R.color.darker_gray)
            leftAxis.gridLineWidth = 0.5f

            // Add a buffer to Y-axis max for better visualization
            val maxValue = values.maxOrNull() ?: 100f
            leftAxis.axisMaximum = maxValue * 1.2f
            leftAxis.axisMinimum = 0f // Start Y-axis at 0

            // Disable right Y-axis
            barChart.axisRight.isEnabled = false

            // Chart configuration
            barChart.description.isEnabled = false
            barChart.legend.isEnabled = true
            barChart.legend.textSize = 11f
            barChart.setDrawBorders(false)
            barChart.setDrawGridBackground(false)

            // Extra padding for label visibility
            barChart.setExtraBottomOffset(20f)  // Increased bottom padding for rotated labels
            barChart.setExtraLeftOffset(10f)
            barChart.setExtraRightOffset(10f)

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
                    labels,
                    values,
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
            val dataWidth = (barWidthPx * 1.2f * labels.size).toInt()

            // Force chart container to be wider than screen
            val chartWidth = maxOf(dataWidth, screenWidth * 2)

            println("Setting chart width: $chartWidth px for ${labels.size} bars (screen: $screenWidth)")
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
                val initialPosition = calculateInitialPosition(params, labels, 7.5f, chartType)

                // Disable animation for immediate positioning
                barChart.animateX(0)

                // First make sure the chart knows it can scroll by checking min/max
                if (barChart.xAxis.axisMaximum <= barChart.xAxis.axisMinimum) {
                    println("WARNING: Chart X-axis bounds incorrectly set - fixing...")
                    barChart.xAxis.axisMinimum = -0.5f
                    barChart.xAxis.axisMaximum = labels.size - 0.5f
                }

                // Apply initial position - IMPORTANT: this must be called AFTER invalidate
                barChart.invalidate()
                barChart.moveViewToX(initialPosition)

                // Extra check - if we have more than 8 bars, ensure scroll happened
                if (labels.size > 8) {
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

            // Get formatted name for the selected month
            val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val monthYearStr = monthYearFormatter.format(cal.time)

            // Generate consistent labels with capitalized month
            val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val labels = ArrayList<String>()
            val values = ArrayList<String>()

            // Create random number generator with consistent seed for repeatable results
            val random = Random(selectedMonth.toLong() * 31 + selectedYear)

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

                println("Added day $day: $dateLabel = ${values.last()}")
            }

            // Get today's date in the same format for highlighting
            val todayDateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val todayDateStr = todayDateFormatter.format(systemCal.time).split(" ").let {
                if (it.size >= 2) "${it[0]} ${it[1].capitalize()}" else ""
            }

            // Set up parameters map
            val params = HashMap(chartConfig.parameters)
            params["labels"] = labels.joinToString(",")
            params["values"] = values.joinToString(",")

            // Set flags for current month information
            params["isCurrentMonth"] = isCurrentMonthAndYear.toString()
            params["todayDate"] = todayDateStr
            params["unit"] = "kWh"

            // Add explicitly flag for daily data
            params["isDaily"] = "true"
            params["selectedMonth"] = selectedMonth.toString()

            // Additional debug info
            println("Generated ${labels.size} data points for $monthYearStr")
            println("Labels start: ${labels.take(3).joinToString(", ")}")
            println("Labels end: ${labels.takeLast(3).joinToString(", ")}")

            return ChartConfig(
                id = chartConfig.id,
                chartType = ChartType.BAR_DAILY,
                deviceId = chartConfig.deviceId,
                siteName = chartConfig.siteName,
                title = "Daily Energy - $monthYearStr",
                parameters = params,
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

                println("Added hour $hour: $timeLabel = ${values.last()}")
            }

            // Set up parameters
            val params = HashMap(chartConfig.parameters)
            params["labels"] = timeLabels.joinToString(",")
            params["values"] = values.joinToString(",")
            params["unit"] = "kWh"

            // Add metadata for hourly charts
            params["isHourly"] = "true"
            params["isDaily"] = "false"
            params["selectedDay"] = selectedDay.toString()
            params["dataForDay"] = isoDateStr

            // Explicitly flag if this is today's data
            params["isToday"] = isToday.toString()

            return ChartConfig(
                id = chartConfig.id,
                chartType = ChartType.BAR_HOURLY,
                deviceId = chartConfig.deviceId,
                siteName = chartConfig.siteName,
                title = "Hourly Energy - $selectedDateStr",
                parameters = params,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    inner class GaugeChartViewHolder(private val binding: ItemGaugeChartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chartConfig: ChartConfig) {
            binding.chartTitle.text = chartConfig.title
            binding.siteName.text = chartConfig.siteName
            binding.lastUpdated.text = formatLastUpdated(chartConfig.lastUpdated)

            setupGaugeChart(binding, chartConfig)
        }

        private fun setupGaugeChart(binding: ItemGaugeChartBinding, chartConfig: ChartConfig) {
            val params = chartConfig.parameters
            val minValue = params["min"]?.toFloatOrNull() ?: 0f
            val maxValue = params["max"]?.toFloatOrNull() ?: 100f
            val currentValue = params["value"]?.toFloatOrNull() ?: 0f
            val unit = params["unit"] ?: "KW"

            // Log the values for debugging
            println("GaugeChart values - title: ${chartConfig.title}, min: $minValue, max: $maxValue, current: $currentValue, params: ${params}")

            // Access views directly using findViewById
            val speedView = binding.root.findViewById<SpeedView>(R.id.speedView)
            val minValueText = binding.root.findViewById<TextView>(R.id.minValue)
            val maxValueText = binding.root.findViewById<TextView>(R.id.maxValue)
            val customUnitText = binding.root.findViewById<TextView>(R.id.customUnitText)

            // Force-set the unit text with larger text and ensure visibility
            customUnitText?.apply {
                text = unit
                textSize = 24f
                visibility = View.VISIBLE
                setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            }

            // Setup SpeedView gauge if it exists
            speedView?.let {
                // Set min/max values
                it.minSpeed = minValue
                it.maxSpeed = maxValue

                // Set current value with animation
                it.speedTo(currentValue, 1000)

                // Don't show unit in SpeedView since we're using our custom TextView
                it.unitUnderSpeedText = false
                it.unit = ""

                // Customize appearance
                it.speedTextColor = ContextCompat.getColor(context, R.color.colorPrimary)

                // Make unit text larger and bolder
                it.unitTextSize = 0f

                // Replace incorrect properties with the correct ones from SpeedView library
                it.centerCircleColor = ContextCompat.getColor(context, R.color.colorAccent)

                // Enhanced styling
                it.speedometerWidth = 25f
                it.markColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)

                // Set sections and colors
                it.clearSections()
                it.addSections(Section(0f, .3f, ContextCompat.getColor(context, R.color.colorPrimary), 30f))
                it.addSections(Section(.3f, .7f, ContextCompat.getColor(context, R.color.colorAccent), 30f))
                it.addSections(Section(.7f, 1f, ContextCompat.getColor(context, R.color.colorPrimaryDark), 30f))

                // Enable tick marks with custom settings
                it.tickNumber = 5
                it.withTremble = false
            }

            // Set min and max labels
            minValueText?.text = String.format("%.1f", minValue)
            maxValueText?.text = String.format("%.1f", maxValue)
        }
    }

    inner class MetricChartViewHolder(private val binding: ItemMetricChartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(chartConfig: ChartConfig) {
            // Set common title at the top
            binding.chartTitle.text = chartConfig.title

            // Set last updated timestamp at the bottom
            binding.lastUpdated.text = formatLastUpdated(chartConfig.lastUpdated)

            val params = chartConfig.parameters
            // Log the actual parameters for debugging
            android.util.Log.d("MetricChart", "All parameters: ${params.entries.joinToString()}")

            // Get parameter IDs from the parameters map (stored as comma-separated values)
            val parameterIdsStr = params["parameterIds"] ?: ""
            val parameterIds = parameterIdsStr.split(",").filter { it.isNotEmpty() }

            // Find the Power and Energy parameter values from the response
            // Parse numeric values from the parameter map to determine which parameter is which
            // First, try to identify parameters based on the unit if available
            val paramPairs = mutableListOf<Pair<String, String>>() // paramId to value

            // Add any numeric parameters we find (paramId -> value)
            for (entry in params.entries) {
                // Skip non-parameter entries (like timestamp, parameterIds, etc.)
                if (entry.key != "parameterIds" && entry.key != "timestamp" &&
                    entry.key.toIntOrNull() != null && entry.value.toDoubleOrNull() != null) {
                    paramPairs.add(Pair(entry.key, entry.value))
                }
            }

            android.util.Log.d("MetricChart", "Found parameters: $paramPairs")

            // If we don't have 2 parameters yet, try looking for parameterIds list
            if (paramPairs.size < 2) {
                for (id in parameterIds) {
                    val value = params[id] ?: continue
                    if (id.isNotEmpty() && value.toDoubleOrNull() != null) {
                        // Only add if not already in the list
                        if (!paramPairs.any { it.first == id }) {
                            paramPairs.add(Pair(id, value))
                        }
                    }
                }
            }

            // Default parameter types
            var powerParamType = "Power"
            var energyParamType = "Energy"

            // Get the API timestamp
            val timestamp = params["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()

            // If we have values, display them
            if (paramPairs.isNotEmpty()) {
                // Get device name - this is stored in chartConfig.siteName which contains the actual device name
                val deviceName = chartConfig.siteName // This is the full device name like "Dental College Invertor 1"

                // Handle visibility based on number of parameters
                val hasPowerParam = paramPairs.size >= 1
                val hasEnergyParam = paramPairs.size >= 2

                // Set visibility of cards based on available parameters
                binding.powerCard.visibility = if (hasPowerParam) View.VISIBLE else View.GONE
                binding.energyCard.visibility = if (hasEnergyParam) View.VISIBLE else View.GONE

                // If we have only one parameter and it's for Energy (common case), swap the visibility
                if (paramPairs.size == 1) {
                    val singleParamId = paramPairs[0].first

                    // Check if the single parameter is for Energy (usually parameter ID 184)
                    // We can infer this from the parameter value - energy values are typically larger
                    val value = paramPairs[0].second.toDoubleOrNull() ?: 0.0
                    val isLikelyEnergy = value > 1000 // Energy values usually much larger than Power values

                    if (isLikelyEnergy) {
                        // If it's likely energy, show energy card and hide power card
                        binding.powerCard.visibility = View.GONE
                        binding.energyCard.visibility = View.VISIBLE

                        // Set energy card values
                        binding.energyTitle.text = "$deviceName -\nEnergy"
                        binding.energyValue.text = formatMetricValue(paramPairs[0].second, "decimal")
                        binding.energyTimestamp.text = formatTimestamp(timestamp)
                    } else {
                        // If it's likely power, show power card and hide energy card
                        binding.powerCard.visibility = View.VISIBLE
                        binding.energyCard.visibility = View.GONE

                        // Set power card values
                        binding.powerTitle.text = "$deviceName -\nPower"
                        binding.powerValue.text = formatMetricValue(paramPairs[0].second, "decimal")
                        binding.powerValue.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                        binding.powerTimestamp.text = formatTimestamp(timestamp)
                    }
                } else if (paramPairs.size >= 2) {
                    // If we have both parameters, show both cards
                    val firstPair = paramPairs[0]
                    val secondPair = paramPairs[1]

                    // For two parameters, we assume first is Power and second is Energy
                    // Set power card values (first parameter)
                    binding.powerTitle.text = "$deviceName -\nPower"
                    binding.powerValue.text = formatMetricValue(firstPair.second, "decimal")
                    binding.powerValue.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                    binding.powerTimestamp.text = formatTimestamp(timestamp)

                    // Set energy card values (second parameter)
                    binding.energyTitle.text = "$deviceName -\nEnergy"
                    binding.energyValue.text = formatMetricValue(secondPair.second, "decimal")
                    binding.energyTimestamp.text = formatTimestamp(timestamp)
                }

                // Debug logging
                if (paramPairs.size == 1) {
                    android.util.Log.d("MetricChart", "Single parameter value: ${paramPairs[0].second} (parameter ID: ${paramPairs[0].first})")
                } else if (paramPairs.size >= 2) {
                    android.util.Log.d("MetricChart", "Power value: ${paramPairs[0].second} (parameter ID: ${paramPairs[0].first})")
                    android.util.Log.d("MetricChart", "Energy value: ${paramPairs[1].second} (parameter ID: ${paramPairs[1].first})")
                }
            } else {
                // No parameters found - set defaults
                // Use siteName from chart config which contains the actual device name
                val deviceName = chartConfig.siteName // This is the actual device name like "Dental College Invertor 1"

                // By default show both cards
                binding.powerCard.visibility = View.VISIBLE
                binding.energyCard.visibility = View.VISIBLE

                binding.powerTitle.text = "$deviceName -\nPower"
                binding.powerValue.text = "0.00"
                binding.powerValue.setTextColor(ContextCompat.getColor(context, R.color.colorAccent))
                binding.powerTimestamp.text = formatTimestamp(timestamp)

                binding.energyTitle.text = "$deviceName -\nEnergy"
                binding.energyValue.text = "0.00"
                binding.energyTimestamp.text = formatTimestamp(timestamp)

                android.util.Log.d("MetricChart", "No parameter values found")
            }

            android.util.Log.d("MetricChart", "Timestamp: $timestamp")
        }

        private fun formatMetricValue(value: String, format: String?): String {
            val numericValue = value.toDoubleOrNull() ?: return value

            // Customize formatting to match the example image
            // Power is shown as "6.36" (2 decimal places)
            // Energy is shown as "120437.74" (2 decimal places for larger numbers)
            return if (numericValue < 100) {
                // For smaller numbers like power (KW) - show 2 decimal places
                String.format("%.2f", numericValue)
            } else {
                // For larger numbers like energy (kWh) - show 2 decimal places
                String.format("%.2f", numericValue)
            }
        }

        private fun formatTimestamp(timestamp: Long): String {
            if (timestamp <= 0) return ""

            val dateFormat = SimpleDateFormat("dd/MM/yyyy, H:mm:ss", Locale.getDefault())
            return dateFormat.format(Date(timestamp)) + "\nPM"
        }
    }

    private fun formatLastUpdated(timestamp: Long): String {
        if (timestamp <= 0) return context.getString(R.string.not_updated_yet)

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return context.getString(R.string.last_updated, dateFormat.format(Date(timestamp)))
    }

    class ChartDiffCallback(
        private val oldList: List<ChartConfig>,
        private val newList: List<ChartConfig>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    // No unused methods here - duplicate methods have been removed
}