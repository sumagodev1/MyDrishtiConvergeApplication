package com.mydrishti.co.`in`.activities.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.chartapp.R
import com.example.chartapp.databinding.DialogChartTypeSelectionBinding
import com.example.chartapp.models.ChartType
import com.mydrishti.co.`in`.R

class ChartTypeSelectionDialog(
    context: Context,
    private val onChartTypeSelected: (ChartType) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogChartTypeSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogChartTypeSelectionBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        setupUI()
    }
    
    private fun setupUI() {
        // Set up chart type selection buttons
        binding.cardBarChartDaily.setOnClickListener {
            onChartTypeSelected(ChartType.BAR_DAILY)
            dismiss()
        }
        
        binding.cardBarChartHourly.setOnClickListener {
            onChartTypeSelected(ChartType.BAR_HOURLY)
            dismiss()
        }
        
        binding.cardGaugeChart.setOnClickListener {
            onChartTypeSelected(ChartType.GAUGE)
            dismiss()
        }
        
        binding.cardMetricChart.setOnClickListener {
            onChartTypeSelected(ChartType.METRIC)
            dismiss()
        }
        
        // Set up close button
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }
    
    companion object {
        /**
         * Alternative way to show the dialog using AlertDialog
         */
        fun showAsAlertDialog(context: Context, onChartTypeSelected: (ChartType) -> Unit) {
            val items = arrayOf(
                "Daily Bar Chart",
                "Hourly Bar Chart",
                "Gauge Chart",
                "Metric Chart"
            )
            
            AlertDialog.Builder(context)
                .setTitle(R.string.select_chart_type)
                .setItems(items) { dialog, which ->
                    val chartType = when (which) {
                        0 -> ChartType.BAR_DAILY
                        1 -> ChartType.BAR_HOURLY
                        2 -> ChartType.GAUGE
                        3 -> ChartType.METRIC
                        else -> throw IllegalArgumentException("Invalid chart type selection")
                    }
                    onChartTypeSelected(chartType)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }
}