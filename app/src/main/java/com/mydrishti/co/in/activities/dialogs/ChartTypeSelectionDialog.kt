package com.mydrishti.co.`in`.activities.dialogs

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.models.ChartType
import com.mydrishti.co.`in`.databinding.DialogChartTypeSelectionBinding

class ChartTypeSelectionDialog(
    context: Context,
    private val onChartTypeSelected: (ChartType) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogChartTypeSelectionBinding
    
    // Card views that will be initialized in setupUI
    private lateinit var cardBarChartDaily: MaterialCardView
    private lateinit var cardBarChartHourly: MaterialCardView
    private lateinit var cardGaugeChart: MaterialCardView
    private lateinit var cardMetricChart: MaterialCardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request a window with no title
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        binding = DialogChartTypeSelectionBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        // Calculate dialog dimensions based on orientation
        val displayMetrics = context.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        
        // Determine if we're in landscape mode
        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        
        // Set dialog width - slightly wider in landscape mode
        val widthPercent = if (isLandscape) 0.95f else 0.90f
        val width = (screenWidth * widthPercent).toInt()
        
        // Set appropriate height constraints based on orientation
        val minHeightPercent = if (isLandscape) 0.85f else 0.65f // Higher percentage in landscape to ensure content fits
        val minHeight = (screenHeight.toFloat() * minHeightPercent).toInt()
        
        // Apply dimensions to dialog
        window?.let { window ->
            val params = window.attributes
            params.width = width
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            window.attributes = params
            
            // In landscape, we don't force a minimum height since space is limited
            if (!isLandscape) {
            binding.root.minimumHeight = minHeight
            }
        }
        
        setupUI()
    }
    
    private fun setupUI() {
        // Find all card views regardless of layout (portrait or landscape)
        try {
            cardBarChartDaily = binding.root.findViewById(R.id.card_bar_chart_daily)
            cardBarChartHourly = binding.root.findViewById(R.id.card_bar_chart_hourly)
            cardGaugeChart = binding.root.findViewById(R.id.card_gauge_chart)
            cardMetricChart = binding.root.findViewById(R.id.card_metric_chart)
            
        // Set up chart type selection buttons
            cardBarChartDaily.setOnClickListener {
            onChartTypeSelected(ChartType.BAR_DAILY)
            dismiss()
        }
        
            cardBarChartHourly.setOnClickListener {
            onChartTypeSelected(ChartType.BAR_HOURLY)
            dismiss()
        }
        
            cardGaugeChart.setOnClickListener {
            onChartTypeSelected(ChartType.GAUGE)
            dismiss()
        }
        
            cardMetricChart.setOnClickListener {
            onChartTypeSelected(ChartType.METRIC)
            dismiss()
            }
        } catch (e: Exception) {
            // Log the error but don't crash
            e.printStackTrace()
        }
        
        // Set up close button - this is directly in the binding
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
            
            // Determine if we're in landscape mode
            val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            
            // Set dialog dimensions based on orientation
            val displayMetrics = context.resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels
            
            // Adjust width based on orientation
            val widthPercent = if (isLandscape) 0.95f else 0.90f
            val width = (screenWidth * widthPercent).toInt()
            
            // Only set minimum height in portrait mode
            val minHeightPercent = if (isLandscape) 0f else 0.65f
            val minHeight = if (isLandscape) 0 else (screenHeight.toFloat() * minHeightPercent).toInt()
            
            dialog.window?.let { window ->
                window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
                window.setBackgroundDrawableResource(R.drawable.dialog_rounded_background)
                
                // Only apply minimum height in portrait mode
                if (!isLandscape && minHeight > 0) {
                val contentView = dialog.findViewById<View>(android.R.id.content)
                contentView?.minimumHeight = minHeight
                }
            }
        }
    }
}