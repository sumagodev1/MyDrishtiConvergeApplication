package com.mydrishti.co.`in`.activities

import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.anastr.speedviewlib.SpeedView
import com.github.anastr.speedviewlib.components.Section

import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.databinding.ActivityGaugeShowcaseBinding
import java.util.Locale

class GaugeShowcaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGaugeShowcaseBinding
    
    // Define min and max values for the gauge
    private val minValue = 0f
    private val maxValue = 20f
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGaugeShowcaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupGauge()
        setupSeekBar()
    }
    
    private fun setupGauge() {
        val speedView = binding.speedView
        val gaugeValue = binding.gaugeValue
        val gaugeUnit = binding.gaugeUnit
        val minValueText = binding.minValue
        val maxValueText = binding.maxValue
        
        // Set min and max labels
        minValueText.text = String.format(Locale.US, "%.2f", minValue)
        maxValueText.text = String.format(Locale.US, "%.2f", maxValue)
        
        // Set unit text
        gaugeUnit.text = "KW"
        
        // Configure the SpeedView
        with(speedView) {
            // Set min and max values
            minSpeed = minValue
            maxSpeed = maxValue
            
            // Set unit text
            unit = "KW"
            
            // Configure for exactly 2 decimal places using speedTextListener
            speedTextListener = { speed ->
                String.format(Locale.US, "%.2f", speed)
            }
            
            // Create color sections
            clearSections()
            
            // Set speedometer width for better visibility
            speedometerWidth = 60f
            
            // Green section (0-65%) - using thicker sections
            addSections(Section(0f, 0.65f, ContextCompat.getColor(context, R.color.chart_green)))
            
            // Yellow section (65-85%) - using thicker sections
            addSections(Section(0.65f, 0.85f, ContextCompat.getColor(context, R.color.chart_yellow)))
            
            // Red section (85-100%) - using thicker sections
            addSections(Section(0.85f, 1f, ContextCompat.getColor(context, R.color.chart_red)))

            // Set initial value
            val initialValue = 10.5f
            speedTo(initialValue)
            gaugeValue.text = String.format(Locale.US, "%.2f", initialValue)
            
            // Update external display color based on percentage
            updateExternalDisplayColor(initialValue)
        }
    }
    
    private fun setupSeekBar() {
        val seekBar = binding.demoSeekBar
        val seekBarLabel = binding.seekBarLabel
        val speedView = binding.speedView
        val gaugeValue = binding.gaugeValue
        
        // Set initial progress based on default value
        val initialValue = 10.5f
        val progress = calculateProgress(initialValue)
        seekBar.progress = progress
        updateSeekBarLabel(seekBarLabel, initialValue)
        
        // Set up seek bar listener
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Calculate actual value from progress (0-100)
                val value = calculateValue(progress)
                
                // Update the gauge and text
                speedView.speedTo(value)
                gaugeValue.text = String.format(Locale.US, "%.2f", value)
                updateSeekBarLabel(seekBarLabel, value)
                updateExternalDisplayColor(value)
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun calculateValue(progress: Int): Float {
        // Convert progress (0-100) to a value between min and max
        return minValue + (progress / 100f) * (maxValue - minValue)
    }
    
    private fun calculateProgress(value: Float): Int {
        // Convert value to progress (0-100)
        return ((value - minValue) / (maxValue - minValue) * 100).toInt()
    }
    
    private fun updateSeekBarLabel(textView: TextView, value: Float) {
        textView.text = "Test Value: ${String.format(Locale.US, "%.2f", value)} KW"
    }
    
    private fun updateExternalDisplayColor(value: Float) {
        val gaugeValue = binding.gaugeValue
        
        // Calculate value percentage
        val valuePercentage = (value - minValue) / (maxValue - minValue)
        
        // Apply color to the value text
        val colorRes = when {
            valuePercentage < 0.65f -> R.color.chart_green
            valuePercentage < 0.85f -> R.color.chart_yellow
            else -> R.color.chart_red
        }
        
        gaugeValue.setTextColor(ContextCompat.getColor(this, colorRes))
    }
} 