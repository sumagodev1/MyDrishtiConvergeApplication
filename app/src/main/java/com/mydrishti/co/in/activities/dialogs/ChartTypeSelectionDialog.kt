package com.mydrishti.co.`in`.activities.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.databinding.DialogChartTypeSelectionBinding

class ChartTypeSelectionDialog(
    context: Context,
    private val onChartTypeSelected: (ChartType) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogChartTypeSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request a window with no title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        binding = DialogChartTypeSelectionBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        // Calculate dialog dimensions
        val displayMetrics = context.resources.displayMetrics
        val width = (displayMetrics.widthPixels * 0.90).toInt()
        
        // Set minimum height to 80% of screen height if needed
        val screenHeight = displayMetrics.heightPixels
        val minHeight = (screenHeight * 0.65).toInt() // Set minimum height to 65% of screen height
        
        // Apply dimensions to dialog
        window?.let { window ->
            val params = window.attributes
            params.width = width
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
            
            // Force a minimum height by applying padding if needed
            binding.root.minimumHeight = minHeight
        }
        
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
            
            val dialog = AlertDialog.Builder(context)
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
                .create()
                
            // Configure dialog width and background
            dialog.show()
            
            // Set dialog dimensions
            val displayMetrics = context.resources.displayMetrics
            val width = (displayMetrics.widthPixels * 0.90).toInt()
            val screenHeight = displayMetrics.heightPixels
            val minHeight = (screenHeight * 0.65).toInt() // Set minimum height to 65% of screen height
            
            dialog.window?.let { window ->
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                window.setBackgroundDrawableResource(R.drawable.dialog_rounded_background)
                
                // Apply minimum height if needed
                val contentView = dialog.findViewById<View>(android.R.id.content)
                contentView?.minimumHeight = minHeight
            }
        }
    }
}