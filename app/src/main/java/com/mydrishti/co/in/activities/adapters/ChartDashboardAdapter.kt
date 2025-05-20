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
                ChartType.BAR_DAILY -> setupMonthPicker(chartConfig)
                ChartType.BAR_HOURLY -> setupDatePicker(chartConfig)
                else -> {
                    binding.monthSelectionLayout.visibility = View.GONE
                    binding.dateSelectionLayout.visibility = View.GONE
                }
            }
        }

        private fun setupDatePicker(chartConfig: ChartConfig) {
            binding.dateSelectionLayout.visibility = View.VISIBLE
            binding.monthSelectionLayout.visibility = View.GONE

            // Get the current date and time for reference
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)
            val currentHour = cal.get(Calendar.HOUR_OF_DAY)

            // Format and display the initial date
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvDateDisplay.text = dateFormatter.format(cal.time)

            // Setup date picker dialog
            binding.btnDatePicker.setOnClickListener {
                val datePickerDialog = android.app.DatePickerDialog(
                    context,
                    { _, year, month, dayOfMonth ->
                        // When date is selected
                        val selectedCal = Calendar.getInstance()
                        selectedCal.set(year, month, dayOfMonth)

                        // Update displayed date
                        binding.tvDateDisplay.text = dateFormatter.format(selectedCal.time)

                        // Create a new chart config specifically for hourly data
                        val newChartConfig = ChartConfig(
                            id = chartConfig.id,
                            chartType = ChartType.BAR_HOURLY, // Force HOURLY type
                            deviceId = chartConfig.deviceId,
                            siteName = chartConfig.siteName,
                            title = chartConfig.title,
                            parameters = HashMap(chartConfig.parameters),
                            lastUpdated = System.currentTimeMillis()
                        )

                        // Generate hourly data for the selected day
                        val updatedChartConfig = simulateHourlyData(newChartConfig, dayOfMonth)

                        // Debug output
                        println("Date picker selected: ${dateFormatter.format(selectedCal.time)}")

                        // Setup and display the chart
                        setupBarChart(binding.barChart, updatedChartConfig)

                        // Position chart to show appropriate time range
                        val isCurrentDay = (year == currentYear && month == currentMonth && dayOfMonth == currentDay)
                        if (isCurrentDay) {
                            // For current day, center the view on the current hour
                            val scrollPosition = Math.max(0, currentHour - 3) // Center current hour with 6 bars in view
                            binding.barChart.post {
                                binding.barChart.moveViewToX(scrollPosition.toFloat())
                            }
                        } else {
                            // For other days, start in the morning (8am)
                            binding.barChart.post {
                                binding.barChart.moveViewToX(6f) // Start view at 6am (show 6am-12pm)
                            }
                        }
                    },
                    currentYear,
                    currentMonth,
                    currentDay
                )

                // Set the date picker's max date to today
                datePickerDialog.datePicker.maxDate = cal.timeInMillis

                // Set the date picker's min date to first day of current month (for demo)
                val minCal = Calendar.getInstance()
                minCal.set(currentYear, currentMonth, 1)
                datePickerDialog.datePicker.minDate = minCal.timeInMillis

                datePickerDialog.show()
            }

            // Create a new chart config specifically for hourly data
            val newChartConfig = ChartConfig(
                id = chartConfig.id,
                chartType = ChartType.BAR_HOURLY, // Force HOURLY type
                deviceId = chartConfig.deviceId,
                siteName = chartConfig.siteName,
                title = "Hourly Energy - Today", // Clear title for today
                parameters = HashMap(chartConfig.parameters),
                lastUpdated = System.currentTimeMillis()
            )

            // Initial setup - show current day data with hourly time labels
            val updatedChartConfig = simulateHourlyData(newChartConfig, currentDay)
            println("Initial hourly setup for current day: $currentDay, current hour: $currentHour")
            setupBarChart(binding.barChart, updatedChartConfig)

            // Initial scroll position - center view around current hour
            val scrollPosition = Math.max(0, currentHour - 3) // Position to show current hour centered
            binding.barChart.post {
                binding.barChart.moveViewToX(scrollPosition.toFloat())
            }
        }

        private fun setupMonthPicker(chartConfig: ChartConfig) {
            binding.monthSelectionLayout.visibility = View.VISIBLE
            binding.dateSelectionLayout.visibility = View.GONE

            // Get current date info
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)

            // Initial month display
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.tvMonthDisplay.text = formatter.format(cal.time)

            // Setup month picker
            binding.btnMonthPicker.setOnClickListener {
                // Create custom dialog for month picking
                val dialog = android.app.AlertDialog.Builder(context)
                    .setTitle("Select Month")
                    .create()

                // Inflate a custom view with month picker
                val monthPickerView = LayoutInflater.from(context).inflate(R.layout.month_year_picker, null)

                // Find month and year pickers in the view
                val monthPicker = monthPickerView.findViewById<android.widget.NumberPicker>(R.id.monthPicker)
                val yearPicker = monthPickerView.findViewById<android.widget.NumberPicker>(R.id.yearPicker)

                // Setup the month picker
                val monthNames = arrayOf("January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December")
                monthPicker.minValue = 0
                monthPicker.maxValue = 11
                monthPicker.displayedValues = monthNames
                monthPicker.value = currentMonth

                // Setup the year picker (limit to current year for demo)
                yearPicker.minValue = currentYear - 5 // Allow going back 5 years
                yearPicker.maxValue = currentYear
                yearPicker.value = currentYear

                // Setup dialog buttons
                dialog.setView(monthPickerView)
                dialog.setButton(android.app.AlertDialog.BUTTON_POSITIVE, "OK") { _, _ ->
                    val selectedMonth = monthPicker.value
                    val selectedYear = yearPicker.value

                    // Set calendar to selected month/year
                    val selectedCal = Calendar.getInstance()
                    selectedCal.set(Calendar.YEAR, selectedYear)
                    selectedCal.set(Calendar.MONTH, selectedMonth)

                    // Update the displayed month text
                    binding.tvMonthDisplay.text = formatter.format(selectedCal.time)

                    // Update chart with selected month's data
                    val updatedChartConfig = simulateMonthlyData(chartConfig, selectedMonth, selectedYear)
                    setupBarChart(binding.barChart, updatedChartConfig)

                    // Position the chart to show the current day (or latest day in past months)
                    if (selectedMonth == currentMonth && selectedYear == currentYear) {
                        // For current month, position to show the current day
                        val daysInView = 6.0f // Show 6 bars in portrait mode
                        val scrollPosition = Math.max(0, (currentDay - daysInView).toInt())
                        binding.barChart.moveViewToX(scrollPosition.toFloat())
                    } else {
                        // For past months, position to show the last 6 days
                        val lastDay = selectedCal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val scrollPosition = Math.max(0, lastDay - 6)
                        binding.barChart.moveViewToX(scrollPosition.toFloat())
                    }
                }

                dialog.setButton(android.app.AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                dialog.show()
            }

            // Initial default - show current month data
            val updatedChartConfig = simulateMonthlyData(chartConfig, currentMonth, currentYear)
            setupBarChart(binding.barChart, updatedChartConfig)

            // Scroll to show current day in the initial view (with 6 bars visible)
            val scrollPosition = Math.max(0, currentDay - 3) // Center the current day
            binding.barChart.post {
                binding.barChart.moveViewToX(scrollPosition.toFloat())
            }
        }

        private fun setupBarChart(barChart: CombinedChart, chartConfig: ChartConfig) {
            barChart.clear()

            // Explicitly check chart type
            val isDailyChart = chartConfig.chartType == ChartType.BAR_DAILY
            val isHourlyChart = chartConfig.chartType == ChartType.BAR_HOURLY

            // Get screen/device information
            val resources = context.resources
            val displayMetrics = resources.displayMetrics
            val orientation = resources.configuration.orientation
            val isLandscape = orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // Get chart data from parameters
            val params = chartConfig.parameters

            // Get current date info for reference
            val currentCal = Calendar.getInstance()
            val todayDateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val todayDateStr = todayDateFormatter.format(currentCal.time)
            println("TODAY DATE FORMAT (from system): $todayDateStr")

            // Check if this is hourly or daily data
            val isHourlyData = isHourlyChart || params["isHourly"] == "true" || params["showHourOnly"] == "true"
            val isDailyData = isDailyChart || params["isDaily"] == "true"

            // Check if chart should include today
            val includeToday = params["includeToday"] == "true"
            val todayDate = params["todayDate"] ?: todayDateStr
            println("Using todayDate: $todayDate (from params or system)")

            // Get raw labels first
            val rawLabels = params["labels"]?.split(",") ?: listOf()

            // Debug raw labels
            println("RAW LABELS: ${rawLabels.size} labels, last few: ${rawLabels.takeLast(3).joinToString(", ")}")

            // Process labels based on chart type
            val labels = if (isHourlyData) {
                // For hourly data, ensure every label is in HH:00 format
                rawLabels.mapIndexed { index, label ->
                    if (label.contains(":") && !label.contains("-")) {
                        // Already has time format, leave as is
                        label
                    } else {
                        // Convert to hour format
                        "${index.toString().padStart(2, '0')}:00"
                    }
                }
            } else if (isDailyData) {
                // For daily charts, ensure proper date formatting
                val processedLabels = rawLabels.mapIndexed { index, label ->
                    if (label.trim().isEmpty()) {
                        // If label is empty for some reason, use day number
                        "${index + 1} ${monthNames[currentCal.get(Calendar.MONTH)]}"
                    } else if (label.contains("-") && label.length > 5) {
                        // Convert YYYY-MM-DD to DD MMM
                        try {
                            val parts = label.split("-")
                            if (parts.size >= 3) {
                                val monthIndex = parts[1].toInt() - 1
                                val monthName = if (monthIndex in 0..11) monthNames[monthIndex] else ""
                                "${parts[2]} $monthName"
                            } else {
                                label // Keep original if can't parse
                            }
                        } catch (e: Exception) {
                            label // Keep original if can't parse
                        }
                    } else {
                        // Already in proper format, keep as is
                        label
                    }
                }.toMutableList()

                // For daily chart - ENSURE TODAY IS INCLUDED if it's the current month
                if (includeToday && todayDate.isNotEmpty() && !processedLabels.contains(todayDate)) {
                    println("ADDING TODAY: Today's date ($todayDate) was not in the labels, adding it")
                    processedLabels.add(todayDate)
                }

                processedLabels
            } else {
                // Default case - use labels as is
                rawLabels
            }

            // Ensure values list matches labels
            val rawValues = params["values"]?.split(",")?.map { it.toFloatOrNull() ?: 0f } ?: mutableListOf()
            val values = if (labels.size > rawValues.size) {
                // We have more labels than values (probably added today), add values to match
                val valuesList = rawValues.toMutableList()
                // Add zeros or simulate values for added labels
                for (i in rawValues.size until labels.size) {
                    // If this is today's label that we added, generate a realistic value
                    if (i == labels.size - 1 && labels[i] == todayDate) {
                        // Add a random value similar to other entries for today
                        val averageValue = if (rawValues.isNotEmpty()) rawValues.sum() / rawValues.size else 35f
                        // Make today's value stand out more
                        val todayValue = averageValue + 10f + Random().nextFloat() * 5f
                        println("ADDING VALUE FOR TODAY: $todayValue")
                        valuesList.add(todayValue)
                    } else {
                        valuesList.add(0f)  // Default for other added labels
                    }
                }
                valuesList
            } else if (rawValues.size > labels.size) {
                // More values than labels, trim values
                rawValues.subList(0, labels.size)
            } else {
                // Equal size, use as is
                rawValues
            }

            val unit = params["unit"] ?: "kWh"  // Use energy unit by default

            // Log the values for debugging
            println("Chart type: ${if (isDailyChart) "DAILY" else if (isHourlyChart) "HOURLY" else "UNKNOWN"}")
            println("Labels after processing: ${labels.takeLast(5).joinToString(", ")}")
            println("Values after processing: ${values.takeLast(5).joinToString(", ")}")
            println("Total labels: ${labels.size}, Total values: ${values.size}")

            // Create entries
            val entries = values.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            if (entries.isEmpty()) {
                barChart.setNoDataText(context.getString(R.string.no_data_available))
                return
            }

            // Final check for hourly data - ensure our entries match processed labels
            if (isHourlyData && entries.size > labels.size) {
                println("WARNING: Entries count (${entries.size}) greater than labels count (${labels.size}). Trimming entries.")
                val trimmedEntries = entries.take(labels.size)
                if (trimmedEntries.isEmpty()) {
                    barChart.setNoDataText(context.getString(R.string.no_data_available))
                    return
                }
            }

            val dataSet = BarDataSet(entries, chartConfig.title)

            // Set nicer colors
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

            // Customize value text appearance
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)

            // Add barDataSet styling
            dataSet.setDrawValues(true)
            dataSet.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return String.format("%.2f", value)  // Show 2 decimal places, unit is shown separately
                }
            }

            // Calculate average for the average line
            val average = if (values.isNotEmpty()) values.sum() / values.size else 0f

            // Create a combined data object that includes both bar and line data
            val barData = BarData(dataSet)

            // Use consistent bar width for both daily and hourly charts
            // Make bars narrower to leave space for labels
            barData.barWidth = if (isLandscape) 0.7f else 0.5f

            // Create average line dataset
            val avgEntries = values.mapIndexed { index, _ ->
                Entry(index.toFloat(), average)
            }
            val avgLineDataSet = LineDataSet(avgEntries, "Energy Average")
            avgLineDataSet.color = ContextCompat.getColor(context, R.color.colorAccent)
            avgLineDataSet.lineWidth = 2f
            avgLineDataSet.setDrawCircles(false)
            avgLineDataSet.setDrawValues(false)
            avgLineDataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            avgLineDataSet.enableDashedLine(10f, 5f, 0f)  // Make it a dashed line
            avgLineDataSet.axisDependency = YAxis.AxisDependency.LEFT

            // Create line data and combined data
            val lineData = LineData(avgLineDataSet)
            val combinedData = CombinedData()
            combinedData.setData(barData)
            combinedData.setData(lineData)

            // Set the drawing order for the combined chart
            barChart.drawOrder = arrayOf(
                CombinedChart.DrawOrder.BAR,
                CombinedChart.DrawOrder.LINE
            )

            // Set combined data to combined chart
            barChart.data = combinedData

            // X-axis setup
            val xAxis = barChart.xAxis

            // Important: For hourly data, verify each label is in HH:00 format
            println("FINAL LABELS: Using ${labels.size} labels for X-axis")
            println("Last few labels: ${labels.takeLast(5).joinToString(", ")}")

            // Set up formatting for the X-axis with custom formatter to handle all cases
            xAxis.valueFormatter = object : IndexAxisValueFormatter(labels) {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    // Make sure we don't go out of bounds
                    return if (index >= 0 && index < labels.size) {
                        val label = labels[index]
                        // Special highlighting for today's date in daily charts
                        if (isDailyData && label == todayDate) {
                            // Return the label with some marker if needed
                            label
                        } else {
                            label
                        }
                    } else {
                        ""
                    }
                }
            }
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(context, R.color.colorPrimary)
            xAxis.textSize = 10f  // Smaller text size for labels

            // Further X-axis customizations to ensure labels are properly displayed
            xAxis.setCenterAxisLabels(false) // Align labels with bars, not between them
            xAxis.setLabelCount(labels.size, false) // Show all labels

            // In landscape mode, ensure there's enough space for all labels
            if (isLandscape) {
                // Set width based on label count - wider for more labels
                val minWidthPerLabel = 100 // dp
                val totalMinWidth = labels.size * minWidthPerLabel
                val widthInPixels = dpToPx(totalMinWidth)
                if (widthInPixels > barChart.width) {
                    barChart.layoutParams.width = widthInPixels
                }
            }

            // Calculate the right width for the chart based on the number of bars
            // Use same bar width calculation for both daily and hourly charts for consistency
            val barWidthDp = if (isLandscape) 100 else 80

            // Calculate total chart width based on number of bars
            // Ensure it's wide enough for all bars, but not too narrow
            val chartWidth = kotlin.math.max(screenWidth.toFloat(), (labels.size * barWidthDp).toFloat()).toInt()
            barChart.layoutParams.width = chartWidth

            println("Chart width calculation: ${labels.size} bars * $barWidthDp dp = $chartWidth px")

            // Adjust height in landscape mode to fill more of the screen
            if (isLandscape) {
                // In landscape, make the chart taller - about 70% of screen height
                barChart.layoutParams.height = (screenHeight * 0.7).toInt()
            } else {
                // Default height in portrait mode
                barChart.layoutParams.height = resources.getDimensionPixelSize(R.dimen.chart_height_portrait)
            }

            // Set fixed number of visible bars based on orientation
            val visibleBarsCount = if (isLandscape) {
                // In landscape, show exactly 12 bars
                12f
            } else {
                // In portrait, always show exactly 6 bars for both chart types
                6f
            }

            // Configure visibility and scrolling
            barChart.setVisibleXRangeMaximum(visibleBarsCount)
            barChart.setVisibleXRangeMinimum(visibleBarsCount) // Force exactly this number of bars to show
            barChart.isDragEnabled = true         // Enable dragging/scrolling
            barChart.setScaleEnabled(false)       // Disable scaling to maintain fixed bar count
            barChart.setPinchZoom(false)          // Disable pinch zoom to maintain fixed bar count
            barChart.setDragOffsetX(10f)          // Smoother scrolling

            // Adjust label rotation based on orientation and chart type
            xAxis.labelRotationAngle = if (isLandscape) {
                // In landscape, use less rotation for better readability
                20f
            } else {
                // Less rotation in portrait mode for better visibility
                30f
            }

            // Add more space below X-axis for the labels
            xAxis.setYOffset(12f)

            // Add spacing between labels
            xAxis.setSpaceMin(0.2f)
            xAxis.setSpaceMax(0.2f)

            // Make sure labels are fully visible
            xAxis.setAvoidFirstLastClipping(true)

            // Set label count based on how many we want to show
            val labelCount = if (isLandscape) {
                // In landscape, show more labels - one per bar
                visibleBarsCount.toInt()
            } else {
                // In portrait, show 6 labels (one per bar)
                6
            }
            xAxis.setLabelCount(labelCount, false)

            // Position chart view based on current date/time
            // Use the parameters from chart config directly (avoid variable redeclaration)
            if (isHourlyData) {
                // For hourly data, center around current hour
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val scrollPosition = Math.max(0, currentHour - (visibleBarsCount / 2).toInt())
                barChart.moveViewToX(scrollPosition.toFloat())
                println("HOURLY CHART: Scrolling to position $scrollPosition to show current hour: $currentHour")
            } else if (isDailyData) {
                // For daily charts, try to display today
                // First check if today's date is in the labels
                val todayIndex = labels.indexOf(todayDate)
                println("DAILY CHART: Looking for today's date: $todayDate")

                if (todayIndex >= 0) {
                    // Today's date found in labels - scroll to it
                    val scrollPosition = Math.max(0, todayIndex - (visibleBarsCount / 2).toInt())
                    println("DAILY CHART: Found today at index $todayIndex, scrolling to position $scrollPosition")
                    barChart.post {
                        barChart.moveViewToX(scrollPosition.toFloat())
                        // Only highlight if the index is valid and we have data
                        if (todayIndex >= 0 && entries.size > todayIndex) {
                            try {
                                // Highlight today's bar
                                val todayHighlight = Highlight(todayIndex.toFloat(), 0, 0)
                                barChart.highlightValue(todayHighlight, true)
                            } catch (e: Exception) {
                                println("Failed to highlight today's bar: ${e.message}")
                            }
                        }
                    }
                } else {
                    // Today not found, try using the currentDayIndex parameter
                    val currentDayIndex = chartConfig.parameters["currentDayIndex"]?.toIntOrNull() ?: -1
                    if (currentDayIndex >= 0 && currentDayIndex < labels.size) {
                        // Current day index is valid, use it
                        val scrollPosition = Math.max(0, currentDayIndex - (visibleBarsCount / 2).toInt())
                        println("DAILY CHART: Using currentDayIndex=$currentDayIndex, scrolling to position $scrollPosition")
                        barChart.post {
                            barChart.moveViewToX(scrollPosition.toFloat())
                            // Only highlight if the index is valid and we have data
                            if (currentDayIndex >= 0 && entries.size > currentDayIndex) {
                                try {
                                    // Highlight today's bar
                                    val dayHighlight = Highlight(currentDayIndex.toFloat(), 0, 0)
                                    barChart.highlightValue(dayHighlight, true)
                                } catch (e: Exception) {
                                    println("Failed to highlight current day bar: ${e.message}")
                                }
                            }
                        }
                    } else {
                        // Neither today's date nor current day index found, show last section
                        val scrollPosition = Math.max(0, labels.size - visibleBarsCount.toInt())
                        println("DAILY CHART: Today not found, showing last $visibleBarsCount days at position $scrollPosition")
                        barChart.post {
                            barChart.moveViewToX(scrollPosition.toFloat())
                        }
                    }
                }
            } else {
                // Default behavior - show most recent data
                println("DEFAULT SCROLL: Showing most recent data")
                if (labels.size > visibleBarsCount) {
                    barChart.moveViewToX((labels.size - visibleBarsCount).toFloat())
                }
            }

            // Y-axis setup
            val leftAxis = barChart.axisLeft
            leftAxis.setDrawGridLines(true)
            leftAxis.gridColor = ContextCompat.getColor(context, android.R.color.darker_gray)
            leftAxis.gridLineWidth = 0.5f
            leftAxis.textColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)

            // Disable right y-axis
            val rightAxis = barChart.axisRight
            rightAxis.isEnabled = false

            // Other chart configuration
            barChart.description.isEnabled = false
            barChart.legend.isEnabled = true
            barChart.legend.textColor = ContextCompat.getColor(context, R.color.colorPrimary)
            // Configure combined chart specific settings
            barChart.setDrawGridBackground(false)
            barChart.setDrawBarShadow(false)
            barChart.setHighlightFullBarEnabled(false)

            // Set extra offsets to ensure labels are fully visible
            barChart.setExtraOffsets(10f, 5f, 10f, 20f)

            // Remove chart border
            barChart.setDrawBorders(false)

            // Add animation
            barChart.animateY(1000)

            // Setup highlighting and touch events
            barChart.setHighlightPerTapEnabled(true)
            barChart.isHighlightPerDragEnabled = true

            // Configure highlight appearance
            val highlightColor = ContextCompat.getColor(context, R.color.colorAccent)
            barChart.data.setHighlightEnabled(true)

            // Setup custom marker view for better tooltip display
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
                e.printStackTrace()
                // Fallback to default marker if custom one fails
                println("Failed to create custom marker view: ${e.message}")
            }

            // Setup listener for value selections
            barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    // The marker view will handle displaying the data
                }

                override fun onNothingSelected() {
                    // Do nothing when nothing is selected
                }
            })

            // Configure line datasets
            for (dataSet in barChart.data.dataSets) {
                if (dataSet is LineDataSet) {
                    dataSet.enableDashedHighlightLine(10f, 5f, 0f)
                    dataSet.setDrawHorizontalHighlightIndicator(true)
                    dataSet.setDrawVerticalHighlightIndicator(true)
                }
            }

            // Update the chart
            barChart.invalidate()
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

        private fun simulateMonthlyData(chartConfig: ChartConfig, selectedMonth: Int, selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)): ChartConfig {
            // Get current date info
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)

            // Debug current date to verify what the system thinks "today" is
            println("SYSTEM DATE: Current date is ${currentDay} ${monthNames[currentMonth]} ${currentYear}")

            // Set calendar to the selected month
            cal.set(Calendar.YEAR, selectedYear)
            cal.set(Calendar.MONTH, selectedMonth)
            cal.set(Calendar.DAY_OF_MONTH, 1)

            // For current month, make sure to include today. For past months, show all days.
            val maxDay = if (selectedYear == currentYear && selectedMonth == currentMonth) {
                // IMPORTANT: Make sure to include today
                println("CURRENT MONTH: Should include day $currentDay")
                currentDay // Only up to today for current month
            } else {
                println("PAST MONTH: Including all days")
                cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            }

            // Check if we're viewing the current month and mark today's date
            val isCurrentMonth = (selectedYear == currentYear && selectedMonth == currentMonth)
            val todayIndex = if (isCurrentMonth) currentDay - 1 else -1 // 0-indexed

            println("Selected: Year=$selectedYear, Month=$selectedMonth (Current: Year=$currentYear, Month=$currentMonth)")
            println("Showing $maxDay days for ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)}")
            println("Today is day ${currentDay}, index ${todayIndex}")

            // Generate simulated energy values for each day
            val random = Random((selectedMonth.toLong() * 31L + selectedYear.toLong())) // Use seed for consistent random values
            val values = ArrayList<String>()

            // Debug check again
            println("LOOP CHECK: Will generate data for days 1 to $maxDay")

            for (day in 1..maxDay) {
                // Create somewhat realistic looking data with some randomness
                // Give today a slightly higher value to make it more visible
                val value = if (isCurrentMonth && day == currentDay) {
                    // Make today's value stand out slightly
                    38f + random.nextFloat() * 15
                } else {
                    35f + random.nextFloat() * 10  // Base value between 35-45
                }
                values.add(String.format(Locale.US, "%.2f", value))
            }

            // Generate labels for the selected month including ALL days up to maxDay
            // Use a readable date format that clearly shows day and month
            val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
            val labels = ArrayList<String>()

            for (day in 1..maxDay) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                val dateLabel = dateFormatter.format(cal.time)
                labels.add(dateLabel)
                println("ADDED DAY $day: $dateLabel") // Debug each day being added
            }

            // Debug the generated labels in detail
            println("Generated ${labels.size} date labels for month: ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)}")
            println("First 7 labels: ${labels.take(7).joinToString(", ")}")
            println("Last 7 labels: ${labels.takeLast(7).joinToString(", ")}")
            if (labels.size > 0) println("Last label: ${labels.last()}")

            // Format the month name for the title
            val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val monthYearStr = monthYearFormatter.format(cal.time)

            // Create a copy of the chart config with updated labels and values
            val params = HashMap(chartConfig.parameters)
            params["labels"] = labels.joinToString(",")
            params["values"] = values.joinToString(",")

            // Add parameters to highlight current day if applicable
            if (isCurrentMonth) {
                params["currentDayIndex"] = todayIndex.toString()
                params["showCurrentDay"] = "true"
                // Explicitly mark that today should be shown
                params["includeToday"] = "true"

                // Store today's date in a consistent format to make it easier to find
                val todayDateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
                cal.set(Calendar.DAY_OF_MONTH, currentDay)
                val todayDateStr = todayDateFormatter.format(cal.time)
                params["todayDate"] = todayDateStr

                println("TODAY'S DATE SET TO: $todayDateStr")
            }

            // Add a flag to indicate this is daily data
            params["isDaily"] = "true"
            params["dateFormat"] = "dd MMM"

            return ChartConfig(
                id = chartConfig.id,
                chartType = ChartType.BAR_DAILY, // Force DAILY type
                deviceId = chartConfig.deviceId,
                siteName = chartConfig.siteName,
                title = "Daily Energy - $monthYearStr", // Update title with month and year
                parameters = params,
                lastUpdated = System.currentTimeMillis()
            )
        }

        private fun simulateHourlyData(chartConfig: ChartConfig, selectedDay: Int): ChartConfig {
            // Get current date info
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

            // Set calendar to the selected day
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

            // Format the selected day for display
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val selectedDateStr = dateFormatter.format(calendar.time)

            println("Generating hourly data for day: $selectedDay, date: $selectedDateStr")

            // For today, only show up to the current hour. For previous days, show all 24 hours.
            val maxHour = if (selectedDay == currentDay && currentMonth == calendar.get(Calendar.MONTH) && currentYear == calendar.get(Calendar.YEAR)) {
                currentHour // Only up to current hour for today
            } else {
                23 // All hours for previous days
            }

            // Create a random generator with a seed based on the day for consistent data
            val random = Random((currentMonth.toLong() * 100L + selectedDay.toLong()))

            val values = ArrayList<String>()
            val timeLabels = ArrayList<String>()

            // Generate time labels and values for each hour up to maxHour
            for (hour in 0..maxHour) {
                // Create hourly energy values with a realistic pattern
                val baseValue = when {
                    hour <= 6 -> 2.0f + (hour * 0.25f) // Early morning (rising)
                    hour <= 12 -> 3.5f + random.nextFloat() * 1.5f // Morning to noon (peak)
                    hour <= 18 -> 4.0f + random.nextFloat() * 1.0f // Afternoon (high)
                    else -> 3.0f + (23-hour) * 0.15f // Evening (declining)
                }

                values.add(String.format(Locale.US, "%.2f", baseValue))

                // CRITICAL: Use simple hour format for time to ensure it displays properly
                // Store ONLY the hour format (no date parts)
                timeLabels.add(String.format("%02d:00", hour))
            }

            println("Generated ${timeLabels.size} hourly labels: ${timeLabels.joinToString(", ")}")

            // Create a copy of the chart config with updated labels and values
            val params = HashMap(chartConfig.parameters)

            // This is the critical part - explicitly store labels as pure time strings
            params["labels"] = timeLabels.joinToString(",")
            params["values"] = values.joinToString(",")
            params["selectedDay"] = selectedDay.toString()
            params["isHourly"] = "true"  // Add a flag to identify this as hourly data

            // Add more explicit flags to ensure hourly format
            params["showHourOnly"] = "true"
            params["labelType"] = "time"

            return ChartConfig(
                id = chartConfig.id,
                chartType = ChartType.BAR_HOURLY, // Force the chart type to BAR_HOURLY
                deviceId = chartConfig.deviceId,
                siteName = chartConfig.siteName,
                title = "Hourly Energy - $selectedDateStr", // Update title with date
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
            binding.chartTitle.text = chartConfig.title
            binding.siteName.text = chartConfig.siteName
            binding.lastUpdated.text = formatLastUpdated(chartConfig.lastUpdated)

            val params = chartConfig.parameters

            binding.metricLabel.text = params["label"] ?: ""
            binding.metricValue.text = formatMetricValue(
                params["value"] ?: "0",
                params["format"]
            )
            binding.metricUnit.text = params["unit"] ?: ""
        }

        private fun formatMetricValue(value: String, format: String?): String {
            val numericValue = value.toDoubleOrNull() ?: return value

            return when (format) {
                "percent" -> String.format("%.1f%%", numericValue)
                "decimal" -> String.format("%.2f", numericValue)
                "integer" -> String.format("%d", numericValue.toInt())
                "currency" -> String.format("$%.2f", numericValue)
                else -> value
            }
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