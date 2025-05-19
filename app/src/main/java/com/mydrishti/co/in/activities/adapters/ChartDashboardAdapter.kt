package com.mydrishti.co.`in`.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
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
import android.widget.TextView
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

            setupBarChart(binding.barChart, chartConfig)
        }

        private fun setupBarChart(barChart: BarChart, chartConfig: ChartConfig) {
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
                    return String.format("%.1f", value)  // Show just the value, unit is shown separately
                }
            }

            val barData = BarData(dataSet)

            // Add spacing between bars
            barData.barWidth = 0.7f

            barChart.data = barData

            // X-axis setup
            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)
            xAxis.textColor = ContextCompat.getColor(context, R.color.colorPrimary)
            xAxis.textSize = 12f

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
            barChart.setFitBars(true)

            // Center the chart
            barChart.setExtraOffsets(12f, 10f, 12f, 10f)

            // Remove chart border
            barChart.setDrawBorders(false)

            // Add animation
            barChart.animateY(1000)

            // Update the chart
            barChart.invalidate()
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