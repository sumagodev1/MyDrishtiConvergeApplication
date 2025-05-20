package com.mydrishti.co.`in`.activities.adapters

import android.content.Context

import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.mydrishti.co.`in`.R

/**
 * Custom marker view for displaying chart values when a bar is clicked
 */
class ChartMarkerView : MarkerView {
    private var labels: List<String> = listOf()
    private var values: List<Float> = listOf()
    private var average: Float = 0f
    private var unit: String = ""

    private lateinit var tvDate: TextView
    private lateinit var tvValue: TextView
    private lateinit var tvAverage: TextView

    // Required constructor for XML inflation
    constructor(context: Context, attrs: android.util.AttributeSet?) : super(context, R.layout.view_chart_marker) {
        // Initialize with default values
        try {
            tvDate = findViewById(R.id.tvDate)
            tvValue = findViewById(R.id.tvValue)
            tvAverage = findViewById(R.id.tvAverage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    constructor(
        context: Context,
        layoutResource: Int,
        labels: List<String>,
        values: List<Float>,
        average: Float,
        unit: String
    ) : super(context, layoutResource) {
        this.labels = labels
        this.values = values
        this.average = average
        this.unit = unit

        // Initialize views
        tvDate = findViewById(R.id.tvDate)
        tvValue = findViewById(R.id.tvValue)
        tvAverage = findViewById(R.id.tvAverage)
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        try {
            e?.let {
                val index = it.x.toInt()
                if (index >= 0 && index < values.size) {
                    val label = if (index < labels.size) labels[index] else ""
                    val value = values[index]

                    if (::tvDate.isInitialized && ::tvValue.isInitialized && ::tvAverage.isInitialized) {
                        // Format the label differently based on whether it contains time or date
                        val formattedLabel = if (label.matches(Regex("\\d{2}:\\d{2}"))) {
                            // This is a time in 24-hour format
                            "Time: $label"
                        } else if (label.contains("-")) {
                            // This is a date with day-month format
                            "Date: $label"
                        } else {
                            // Generic label
                            "Label: $label"
                        }

                        tvDate.text = formattedLabel
                        tvValue.text = "Energy: ${String.format("%.2f", value)} $unit"
                        tvAverage.text = "Average: ${String.format("%.2f", average)} $unit"
                    }
                }
            }
            super.refreshContent(e, highlight)
        } catch (ex: Exception) {
            // Handle any exceptions that might occur during refresh
            ex.printStackTrace()
        }
    }

    override fun getOffset(): MPPointF {
        // Center the marker on top of the highlighted value
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}