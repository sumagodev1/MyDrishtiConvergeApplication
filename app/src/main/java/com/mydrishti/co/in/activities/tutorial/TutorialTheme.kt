package com.mydrishti.co.`in`.activities.tutorial

/**
 * Data class representing tutorial theme configuration
 */
data class TutorialTheme(
    val primaryColor: Int = 0xFF2196F3.toInt(), // Material Blue
    val accentColor: Int = 0xFF03DAC5.toInt(),  // Material Teal
    val backgroundColor: Int = 0x80000000.toInt(), // Semi-transparent black
    val textColor: Int = 0xFFFFFFFF.toInt(),    // White
    val highlightColor: Int = 0xFFFFEB3B.toInt(), // Material Yellow
    val animationDuration: Long = 300L,
    val textSize: Float = 16f,
    val titleTextSize: Float = 20f
)