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

            // Create month options only up to the current month
            val cal = Calendar.getInstance()
            val currentMonth = cal.get(Calendar.MONTH)
            val currentYear = cal.get(Calendar.YEAR)

            println("Setting up month selector - current month: ${currentMonth+1}, year: $currentYear")

            val monthOptions = ArrayList<String>()
            val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())

            // Only show months up to the current month
            cal.set(Calendar.DAY_OF_MONTH, 1)
            for (month in 0..currentMonth) { // January (0) to current month
                cal.set(Calendar.MONTH, month)
                val formattedMonth = formatter.format(cal.time)
                monthOptions.add(formattedMonth)
                println("Added month option: $formattedMonth")
            }

            // Create and set adapter
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, monthOptions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.monthSelector.adapter = adapter

            // Set default to current month
            binding.monthSelector.setSelection(currentMonth)

            // Set listener for month selection changes
            binding.monthSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Refresh chart with selected month
                    val selectedMonth = position
                    val selectedMonthCal = Calendar.getInstance()
                    selectedMonthCal.set(Calendar.YEAR, currentYear)
                    selectedMonthCal.set(Calendar.MONTH, selectedMonth)

                    // Update the chart data for the selected month
                    // In a real implementation, this would trigger an API call to get new data
                    // For this prototype, we'll simulate data for the selected month
                    val updatedChartConfig = simulateMonthlyData(chartConfig, selectedMonth)
                    setupBarChart(binding.barChart, updatedChartConfig)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
        }

        private fun setupDateSelector(chartConfig: ChartConfig) {
            binding.dateSelectionLayout.visibility = View.VISIBLE
            binding.monthSelectionLayout.visibility = View.GONE

            // Get the current date for reference
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)

            // Debug log
            println("Setting up date selector - current date: ${currentYear}-${currentMonth+1}-${currentDay}")

            val dateOptions = ArrayList<String>()
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

            // Only include days up to the current date
            val tempCal = Calendar.getInstance()
            tempCal.set(Calendar.YEAR, currentYear)
            tempCal.set(Calendar.MONTH, currentMonth)

            println("Days in current month up to today: $currentDay")

            // Add days only up to the current day
            tempCal.set(Calendar.DAY_OF_MONTH, 1) // Start from first day of month
            for (day in 1..currentDay) {
                tempCal.set(Calendar.DAY_OF_MONTH, day)
                val formattedDate = formatter.format(tempCal.time)
                dateOptions.add(formattedDate)
                println("Added date option: $formattedDate")
            }

            // Create and set adapter
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, dateOptions)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.dateSelector.adapter = adapter

            // Set default to current day (but ensure we don't exceed the list size)
            val defaultPosition = minOf(currentDay - 1, dateOptions.size - 1)
            println("Setting default date selection to position: $defaultPosition (day ${defaultPosition + 1})")
            binding.dateSelector.setSelection(defaultPosition) // -1 because array is 0-indexed

            // Set listener for date selection changes
            binding.dateSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    // Refresh chart with selected date
                    // In a real implementation, this would trigger an API call to get new data
                    // For this prototype, we'll simulate data for the selected date
                    val daySelected = position + 1  // +1 because day starts at 1
                    val updatedChartConfig = simulateHourlyData(chartConfig, daySelected)
                    setupBarChart(binding.barChart, updatedChartConfig)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
        }

        private fun setupBarChart(barChart: CombinedChart, chartConfig: ChartConfig) {
            barChart.clear()

            val params = chartConfig.parameters
            val labels = params["labels"]?.split(",") ?: listOf()
            val values = params["values"]?.split(",")?.map { it.toFloatOrNull() ?: 0f } ?: listOf()
            val unit = params["unit"] ?: "kWh"  // Use energy unit by default

            // Log the values for debugging
            println("BarChart values - title: ${chartConfig.title}, values: $values, labels: $labels, unit: $unit")

            // Create entries
            val entries = values.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            if (entries.isEmpty()) {
                barChart.setNoDataText(context.getString(R.string.no_data_available))
                return
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

            // Add spacing between bars
            barData.barWidth = 0.6f

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

            // Determine if we're dealing with a daily or hourly chart
            val isDailyChart = chartConfig.chartType == ChartType.BAR_DAILY

            // Log for debugging
            println("Chart type: ${if (isDailyChart) "Daily" else "Hourly"}")
            println("Labels from params: $labels")

            // Create proper date/time labels for X-axis
            val displayLabels = if (labels.isNotEmpty()) {
                // Use the labels provided in the parameters
                labels
            } else if (isDailyChart) {
                // Generate daily labels for the current month up to today
                generateDailyLabels()
            } else {
                // For hourly charts, check if we need to limit hours
                // Get params from title to determine if this is current day
                val isCurrentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH) ==
                        chartConfig.parameters["selectedDay"]?.toIntOrNull() ?: -1

                // If current day, only show hours up to current hour
                if (isCurrentDay) {
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    generateHourlyLabels(currentHour)
                } else {
                    // For past days, show all hours
                    generateHourlyLabels()
                }
            }

            // Log for debugging
            println("Display labels: $displayLabels")

            // Set up formatting for the X-axis
            xAxis.valueFormatter = IndexAxisValueFormatter(displayLabels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(context, R.color.colorPrimary)
            xAxis.textSize = 10f  // Smaller text size for hourly labels

            // Configure chart for horizontal scrolling
            barChart.setVisibleXRangeMaximum(8f) // Show only 8 bars at a time for better visibility
            barChart.setVisibleXRangeMinimum(4f)  // At least 4 bars visible
            barChart.isDragEnabled = true         // Enable dragging/scrolling
            barChart.setScaleEnabled(true)        // Enable scaling
            barChart.setPinchZoom(true)           // Enable pinch zoom
            barChart.setDragOffsetX(10f)          // Smoother scrolling

            // Adjust label count and appearance based on chart type
            if (isDailyChart) {
                // For daily charts, show labels with rotation for better readability
                xAxis.labelRotationAngle = 45f
                xAxis.setLabelCount(8, false)
            } else {
                // For hourly charts, make hours more visible
                xAxis.labelRotationAngle = 60f  // More rotation to prevent overlap
                xAxis.setLabelCount(6, false)
            }

            // For hourly charts, reduce the visible range to show fewer bars at once
            if (!isDailyChart) {
                barChart.setVisibleXRangeMaximum(6f)  // Show 6 hours at a time for hourly chart
            }

            // Start the view showing the most recent data (right side of chart)
            if (displayLabels.size > 8) {
                barChart.moveViewToX(displayLabels.size - 8f)
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

            // Center the chart
            barChart.setExtraOffsets(12f, 10f, 12f, 10f)

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
                    displayLabels,
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

            // Setup listener for value selections - no need for Toast since we have marker view
            barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    // The marker view will handle displaying the data
                }

                override fun onNothingSelected() {
                    // Do nothing when nothing is selected
                }
            })

            // Skip setting highlight colors directly as it's causing issues
            // Just configure line datasets for now
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
                labels.add(String.format("%02d:00", hour))
            }
            return labels
        }

        private fun simulateMonthlyData(chartConfig: ChartConfig, selectedMonth: Int): ChartConfig {
            val cal = Calendar.getInstance()
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH)
            val currentDay = cal.get(Calendar.DAY_OF_MONTH)

            // Set calendar to the selected month
            cal.set(Calendar.YEAR, currentYear)
            cal.set(Calendar.MONTH, selectedMonth)
            cal.set(Calendar.DAY_OF_MONTH, 1)

            // Determine the maximum day to show based on whether it's the current month or not
            val maxDay = if (selectedMonth == currentMonth) {
                // If it's the current month, only show up to today
                currentDay
            } else if (selectedMonth < currentMonth) {
                // If it's a past month, show all days
                cal.getActualMaximum(Calendar.DAY_OF_MONTH)
            } else {
                // Future months (should not happen, but just in case)
                0
            }

            // Generate simulated energy values for each day
            val random = Random()
            val baseValue = 40f  // Base energy value
            val values = ArrayList<String>()

            for (day in 1..maxDay) {
                // Create somewhat realistic looking data with some randomness
                val value = baseValue + random.nextFloat() * 10 - 5  // +/- 5 from base
                values.add(String.format(Locale.US, "%.2f", value))
            }

            // Generate labels for the selected month up to the appropriate day
            val dateFormatter = SimpleDateFormat("dd-MMM", Locale.getDefault())
            val labels = ArrayList<String>()

            for (day in 1..maxDay) {
                cal.set(Calendar.DAY_OF_MONTH, day)
                labels.add(dateFormatter.format(cal.time))
            }

            // Create a copy of the chart config with updated labels and values
            val params = HashMap(chartConfig.parameters)
            params["labels"] = labels.joinToString(",")
            params["values"] = values.joinToString(",")

            return ChartConfig(
                id = chartConfig.id,
                chartType = chartConfig.chartType,
                deviceId = chartConfig.deviceId,
                siteName = chartConfig.siteName,
                title = chartConfig.title,
                parameters = params,
                lastUpdated = System.currentTimeMillis()
            )
        }

        private fun simulateHourlyData(chartConfig: ChartConfig, selectedDay: Int): ChartConfig {
            // Generate simulated hourly energy values
            val random = Random()
            val values = ArrayList<String>()
            val timeLabels = ArrayList<String>()

            // Create calendar for the selected day
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

            // Set calendar to the selected day
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

            // Format the selected day for display - include year for clarity
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val selectedDateStr = dateFormatter.format(calendar.time)

            println("Generating hourly data for day: $selectedDay, date: $selectedDateStr")
            println("Current day: $currentDay, current hour: $currentHour")

            // Determine max hour to show - if today, only show up to current hour
            val maxHour = if (selectedDay == currentDay) {
                currentHour
            } else {
                23 // Show all hours for past days
            }

            println("Max hour to show: $maxHour")

            // Generate time labels and values for each hour up to maxHour
            for (hour in 0..maxHour) {
                // Add energy values with different patterns based on time of day
                if (hour <= 5) {
                    // Morning hours (midnight to 5am) - lower values
                    values.add(String.format(Locale.US, "%.2f", random.nextFloat() * 2))
                } else if (hour <= 17) {
                    // Working hours (6am to 5pm) - higher values
                    values.add(String.format(Locale.US, "%.2f", 3f + random.nextFloat() * 2))
                } else {
                    // Evening hours (6pm to 11pm) - medium values
                    values.add(String.format(Locale.US, "%.2f", 1f + random.nextFloat() * 2))
                }

                // Format hours in 24-hour format (00:00 to 23:00)
                timeLabels.add(String.format("%02d:00", hour))
            }

            println("Generated hourly labels: ${timeLabels.joinToString(", ")}")
            println("Generated ${timeLabels.size} hours of data")

            // Create a copy of the chart config with updated labels and values
            val params = HashMap(chartConfig.parameters)
            params["labels"] = timeLabels.joinToString(",")
            params["values"] = values.joinToString(",")
            params["selectedDay"] = selectedDay.toString()

            return ChartConfig(
                id = chartConfig.id,
                chartType = chartConfig.chartType,
                deviceId = chartConfig.deviceId,
                siteName = chartConfig.siteName,
                title = chartConfig.title + " - " + selectedDateStr,  // Add selected date to title
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
}