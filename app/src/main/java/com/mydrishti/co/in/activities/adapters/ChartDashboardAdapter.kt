package com.mydrishti.co.`in`.activities.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.models.ChartConfig
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.databinding.ItemBarChartBinding
import com.mydrishti.co.`in`.databinding.ItemGaugeChartBinding
import com.mydrishti.co.`in`.databinding.ItemMetricChartBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

            setupBarChart(binding.barChart, chartConfig)
        }

        private fun setupBarChart(barChart: BarChart, chartConfig: ChartConfig) {
            barChart.clear()

            val params = chartConfig.parameters
            val labels = params["labels"]?.split(",") ?: listOf()
            val values = params["values"]?.split(",")?.map { it.toFloatOrNull() ?: 0f } ?: listOf()

            // Create entries
            val entries = values.mapIndexed { index, value ->
                BarEntry(index.toFloat(), value)
            }

            if (entries.isEmpty()) {
                barChart.setNoDataText(context.getString(R.string.no_data_available))
                return
            }

            val dataSet = BarDataSet(entries, chartConfig.title)
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            dataSet.valueTextSize = 12f

            val barData = BarData(dataSet)
            barChart.data = barData

            // X-axis setup
            val xAxis = barChart.xAxis
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.setDrawGridLines(false)

            // Other chart configuration
            barChart.description.isEnabled = false
            barChart.legend.isEnabled = true
            barChart.setFitBars(true)
            barChart.animateY(1000)
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

            // Calculate percentage for the gauge visualization
            val percentage = if (maxValue > minValue)
                ((currentValue - minValue) / (maxValue - minValue)) * 100f
            else
                0f

            // Update the gauge UI
            binding.gaugeValue.text = String.format("%.1f", currentValue)
            binding.gaugeProgressBar.progress = percentage.toInt()

            // Set min and max labels
            binding.minValue.text = String.format("%.1f", minValue)
            binding.maxValue.text = String.format("%.1f", maxValue)

            // Set units if available
            binding.gaugeUnit.text = params["unit"] ?: ""
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