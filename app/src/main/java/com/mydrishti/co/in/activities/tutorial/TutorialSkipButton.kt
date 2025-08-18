package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mydrishti.co.`in`.R

/**
 * Custom skip button component for tutorial overlays
 */
class TutorialSkipButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private val skipTextView: TextView
    private var onSkipClickListener: (() -> Unit)? = null
    private var skipCount = 0
    
    init {
        // Create skip button
        skipTextView = TextView(context).apply {
            text = "Skip"
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            gravity = Gravity.CENTER
            setPadding(24, 12, 24, 12)
            
            // Create rounded background
            background = createSkipButtonBackground()
            
            // Set click listener
            setOnClickListener {
                handleSkipClick()
            }
            
            // Add ripple effect for better UX
            isClickable = true
            isFocusable = true
        }
        
        // Add to layout
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            topMargin = 48
            rightMargin = 32
        }
        
        addView(skipTextView, layoutParams)
        
        // Set initial visibility
        visibility = View.VISIBLE
        alpha = 0.8f
    }
    
    /**
     * Creates rounded background for skip button
     */
    private fun createSkipButtonBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 24f
            setColor(ContextCompat.getColor(context, android.R.color.black))
            alpha = 180 // Semi-transparent
            setStroke(2, ContextCompat.getColor(context, android.R.color.darker_gray))
        }
    }
    
    /**
     * Handles skip button click with visual feedback
     */
    private fun handleSkipClick() {
        skipCount++
        
        // Provide visual feedback
        animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100)
                    .withEndAction {
                        onSkipClickListener?.invoke()
                    }
            }
        
        // Update button text based on skip count
        updateButtonText()
    }
    
    /**
     * Updates button text based on skip attempts
     */
    private fun updateButtonText() {
        skipTextView.text = when (skipCount) {
            1 -> "Skip?"
            2 -> "Really Skip?"
            else -> "Skip"
        }
    }
    
    /**
     * Sets the skip click listener
     */
    fun setOnSkipClickListener(listener: () -> Unit) {
        onSkipClickListener = listener
    }
    
    /**
     * Shows the skip button with animation
     */
    fun showWithAnimation() {
        visibility = View.VISIBLE
        alpha = 0f
        animate()
            .alpha(0.8f)
            .setDuration(300)
            .start()
    }
    
    /**
     * Hides the skip button with animation
     */
    fun hideWithAnimation() {
        animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                visibility = View.GONE
            }
            .start()
    }
    
    /**
     * Updates button appearance based on step configuration
     */
    fun updateForStep(step: TutorialStep) {
        visibility = if (step.isSkippable) View.VISIBLE else View.GONE
        
        // Update text based on step
        skipTextView.text = when {
            !step.isSkippable -> "Required"
            skipCount > 0 -> "Skip?"
            else -> "Skip"
        }
        
        // Update appearance for non-skippable steps
        if (!step.isSkippable) {
            skipTextView.setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
            isClickable = false
            alpha = 0.5f
        } else {
            skipTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            isClickable = true
            alpha = 0.8f
        }
    }
    
    /**
     * Resets skip count and appearance
     */
    fun reset() {
        skipCount = 0
        skipTextView.text = "Skip"
        skipTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
        isClickable = true
        alpha = 0.8f
    }
    
    /**
     * Gets current skip count
     */
    fun getSkipCount(): Int = skipCount
    
    /**
     * Sets skip count (for state restoration)
     */
    fun setSkipCount(count: Int) {
        skipCount = count
        updateButtonText()
    }
    
    /**
     * Creates a pulsing animation to draw attention
     */
    fun startPulseAnimation() {
        animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .alpha(1.0f)
            .setDuration(500)
            .withEndAction {
                animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(0.8f)
                    .setDuration(500)
                    .withEndAction {
                        // Repeat pulse if still visible
                        if (visibility == View.VISIBLE) {
                            postDelayed({ startPulseAnimation() }, 2000)
                        }
                    }
            }
    }
    
    /**
     * Stops any running animations
     */
    fun stopAnimations() {
        clearAnimation()
        animate().cancel()
    }
}