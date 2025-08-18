package com.mydrishti.co.`in`.activities.tutorial

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mydrishti.co.`in`.R

/**
 * Custom overlay view that displays tutorial highlights and instructions
 */
class TutorialOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    
    private var step: TutorialStep? = null
    private var targetView: View? = null
    private var animationController: TutorialAnimationController? = null
    private var onStepCompleted: (() -> Unit)? = null
    private var onTutorialSkipped: (() -> Unit)? = null
    private var onTutorialDismissed: (() -> Unit)? = null
    
    // Constructor for programmatic creation with parameters
    constructor(
        context: Context,
        step: TutorialStep,
        targetView: View?,
        animationController: TutorialAnimationController,
        onStepCompleted: () -> Unit,
        onTutorialSkipped: () -> Unit,
        onTutorialDismissed: () -> Unit
    ) : this(context) {
        this.step = step
        this.targetView = targetView
        this.animationController = animationController
        this.onStepCompleted = onStepCompleted
        this.onTutorialSkipped = onTutorialSkipped
        this.onTutorialDismissed = onTutorialDismissed
        
        setupOverlay()
    }
    
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private var targetBounds: RectF? = null
    private var instructionTextView: TextView? = null
    private var skipButton: TextView? = null
    
    private val overlayColor = Color.parseColor("#80000000") // Semi-transparent black
    private val highlightColor = Color.parseColor("#FF4081") // Material pink
    private val textColor = Color.WHITE
    
    init {
        setWillNotDraw(false)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    
    private fun setupOverlay() {
        setupPaints()
        calculateTargetBounds()
        createInstructionViews()
        setupClickHandlers()
    }
    
    /**
     * Sets up paint objects for drawing
     */
    private fun setupPaints() {
        // Overlay paint
        paint.color = overlayColor
        paint.style = Paint.Style.FILL
        
        // Text paint
        textPaint.color = textColor
        textPaint.textSize = 48f
        textPaint.typeface = Typeface.DEFAULT_BOLD
        
        // Highlight paint
        highlightPaint.color = highlightColor
        highlightPaint.style = Paint.Style.STROKE
        highlightPaint.strokeWidth = 8f
    }
    
    /**
     * Calculates the bounds of the target view
     */
    private fun calculateTargetBounds() {
        targetView?.let { view ->
            val location = IntArray(2)
            view.getLocationOnScreen(location)
            
            targetBounds = RectF(
                location[0].toFloat(),
                location[1].toFloat(),
                (location[0] + view.width).toFloat(),
                (location[1] + view.height).toFloat()
            )
        }
    }
    
    /**
     * Creates instruction text and skip button
     */
    private fun createInstructionViews() {
        val currentStep = step ?: return
        
        // Create instruction text
        instructionTextView = TextView(context).apply {
            text = currentStep.description
            textSize = 18f
            setTextColor(textColor)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(32, 16, 32, 16)
            setBackgroundColor(Color.parseColor("#CC000000"))
            
            // Position based on target location
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.BOTTOM
                bottomMargin = 200
            }
        }
        
        addView(instructionTextView)
        
        // Create skip button if allowed
        if (currentStep.isSkippable) {
            skipButton = TextView(context).apply {
                text = "Skip Tutorial"
                textSize = 14f
                setTextColor(Color.LTGRAY)
                setPadding(24, 12, 24, 12)
                setBackgroundColor(Color.parseColor("#66000000"))
                
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.END or android.view.Gravity.TOP
                    topMargin = 48
                    rightMargin = 32
                }
                
                setOnClickListener {
                    onTutorialSkipped?.invoke()
                }
            }
            
            addView(skipButton)
        }
        
        // Add explanatory text if available
        currentStep.explanatoryText?.let { explanatoryText ->
            val explanatoryTextView = TextView(context).apply {
                text = explanatoryText
                textSize = 14f
                setTextColor(Color.LTGRAY)
                setPadding(32, 8, 32, 8)
                
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = android.view.Gravity.CENTER_HORIZONTAL or android.view.Gravity.BOTTOM
                    bottomMargin = 140
                }
            }
            
            addView(explanatoryTextView)
        }
    }
    
    /**
     * Sets up click handlers for interaction
     */
    private fun setupClickHandlers() {
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleTouch(event.x, event.y)
                    true
                }
                else -> false
            }
        }
    }
    
    /**
     * Handles touch events on the overlay
     */
    private fun handleTouch(x: Float, y: Float) {
        val currentStep = step ?: return
        
        targetBounds?.let { bounds ->
            if (bounds.contains(x, y)) {
                // Touch on target area - complete step
                if (currentStep.validationRequired) {
                    // Show validation feedback if required
                    showValidationFeedback("Step completed!")
                }
                onStepCompleted?.invoke()
            } else {
                // Touch outside target - dismiss or ignore based on step configuration
                if (!currentStep.isSkippable) {
                    // Show hint to tap on target
                    showTargetHint()
                } else {
                    onTutorialDismissed?.invoke()
                }
            }
        } ?: run {
            // No target bounds - any touch completes step
            onStepCompleted?.invoke()
        }
    }
    
    /**
     * Shows validation feedback message
     */
    private fun showValidationFeedback(feedback: String) {
        val feedbackView = TextView(context).apply {
            text = feedback
            textSize = 16f
            setTextColor(highlightColor)
            setPadding(24, 12, 24, 12)
            setBackgroundColor(Color.parseColor("#CC000000"))
            
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
            }
        }
        
        addView(feedbackView)
        
        // Animate feedback and remove after delay
        animationController?.let { controller ->
            val fadeIn = controller.createFadeAnimation(feedbackView)
            controller.startAnimation(fadeIn)
            
            postDelayed({
                val fadeOut = controller.createFadeAnimation(feedbackView)
                controller.startAnimation(fadeOut)
                
                postDelayed({
                    removeView(feedbackView)
                }, 300)
            }, 2000)
        }
    }
    
    /**
     * Shows hint to tap on target
     */
    private fun showTargetHint() {
        targetView?.let { view ->
            animationController?.let { controller ->
                val pulseAnimation = controller.createPulseAnimation(view, 1000L)
                controller.startAnimation(pulseAnimation)
            }
        }
    }
    
    /**
     * Custom drawing for overlay and highlight
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw overlay background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Draw highlight around target if exists
        targetBounds?.let { bounds ->
            // Clear the target area (make it transparent)
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            canvas.drawRoundRect(
                bounds.left - 16f,
                bounds.top - 16f,
                bounds.right + 16f,
                bounds.bottom + 16f,
                16f,
                16f,
                paint
            )
            paint.xfermode = null
            
            // Draw highlight border
            canvas.drawRoundRect(
                bounds.left - 16f,
                bounds.top - 16f,
                bounds.right + 16f,
                bounds.bottom + 16f,
                16f,
                16f,
                highlightPaint
            )
        }
    }
    
    /**
     * Starts the overlay animations
     */
    fun startAnimations() {
        val currentStep = step ?: return
        val controller = animationController ?: return
        
        // Animate instruction text
        instructionTextView?.let { textView ->
            val slideAnimation = controller.createSlideInAnimation(textView)
            controller.startAnimation(slideAnimation)
        }
        
        // Animate skip button
        skipButton?.let { button ->
            val fadeAnimation = controller.createFadeAnimation(button)
            controller.startAnimation(fadeAnimation)
        }
        
        // Animate target highlight
        targetView?.let { view ->
            val highlightAnimation = when (currentStep.animationType) {
                AnimationType.PULSE -> controller.createPulseAnimation(view, currentStep.duration)
                AnimationType.RIPPLE -> controller.createRippleAnimation(view, currentStep.duration)
                else -> controller.createAnimation(view, currentStep.animationType, currentStep.duration)
            }
            controller.startAnimation(highlightAnimation)
        }
    }
    
    /**
     * Stops all overlay animations
     */
    fun stopAnimations() {
        animationController?.cancelAllAnimations()
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimations()
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimations()
    }
}