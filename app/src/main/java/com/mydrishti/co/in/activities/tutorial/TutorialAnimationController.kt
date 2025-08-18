package com.mydrishti.co.`in`.activities.tutorial

import android.animation.*
import android.content.Context
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Controls animations for tutorial elements
 */
class TutorialAnimationController(private val context: Context) {
    
    private val activeAnimators = mutableListOf<Animator>()
    
    /**
     * Creates a fade animation
     */
    fun createFadeAnimation(view: View, duration: Long = TutorialConstants.ANIMATION_DURATION_MEDIUM): Animator {
        val animator = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        activeAnimators.add(animator)
        return animator
    }
    
    /**
     * Creates a scale animation
     */
    fun createScaleAnimation(view: View, duration: Long = TutorialConstants.ANIMATION_DURATION_MEDIUM): Animator {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f, 1.0f)
        
        val animator = AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            this.duration = duration
            interpolator = OvershootInterpolator()
        }
        
        activeAnimators.add(animator)
        return animator
    }
    
    /**
     * Creates a slide-in animation
     */
    fun createSlideInAnimation(view: View, duration: Long = TutorialConstants.ANIMATION_DURATION_MEDIUM): Animator {
        val animator = ObjectAnimator.ofFloat(view, "translationY", -100f, 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        activeAnimators.add(animator)
        return animator
    }
    
    /**
     * Creates a pulse animation
     */
    fun createPulseAnimation(view: View, duration: Long = TutorialConstants.ANIMATION_DURATION_LONG): Animator {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.1f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.1f)
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1.1f, 1.0f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1.1f, 1.0f)
        
        val scaleUp = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            this.duration = duration / 2
        }
        
        val scaleDown = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            this.duration = duration / 2
        }
        
        val animator = AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        activeAnimators.add(animator)
        return animator
    }
    
    /**
     * Creates a ripple animation
     */
    fun createRippleAnimation(view: View, duration: Long = TutorialConstants.ANIMATION_DURATION_MEDIUM): Animator {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.7f, 1.0f)
        
        val animator = AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        activeAnimators.add(animator)
        return animator
    }
    
    /**
     * Creates a celebration animation
     */
    fun createCelebrationAnimation(view: View, duration: Long = TutorialConstants.ANIMATION_DURATION_CELEBRATION): Animator {
        val bounce = ObjectAnimator.ofFloat(view, "translationY", 0f, -50f, 0f).apply {
            interpolator = BounceInterpolator()
        }
        
        val rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val scale = createScaleAnimation(view, duration / 2)
        
        val animator = AnimatorSet().apply {
            playTogether(bounce, rotate, scale)
            this.duration = duration
        }
        
        activeAnimators.add(animator)
        return animator
    }
    
    /**
     * Creates a fade-scale combination animation
     */
    fun createFadeScaleAnimation(view: View, duration: Long = TutorialConstants.ANIMATION_DURATION_MEDIUM): Animator {
        val fade = createFadeAnimation(view, duration)
        val scale = createScaleAnimation(view, duration)
        
        val animator = AnimatorSet().apply {
            playTogether(fade, scale)
        }
        
        activeAnimators.add(animator)
        return animator
    }
    
    /**
     * Creates animation based on animation type
     */
    fun createAnimation(view: View, animationType: AnimationType, duration: Long): Animator {
        return when (animationType) {
            AnimationType.FADE -> createFadeAnimation(view, duration)
            AnimationType.SCALE -> createScaleAnimation(view, duration)
            AnimationType.SLIDE_IN -> createSlideInAnimation(view, duration)
            AnimationType.PULSE -> createPulseAnimation(view, duration)
            AnimationType.RIPPLE -> createRippleAnimation(view, duration)
            AnimationType.CELEBRATION -> createCelebrationAnimation(view, duration)
            AnimationType.FADE_SCALE -> createFadeScaleAnimation(view, duration)
        }
    }
    
    /**
     * Creates a smooth transition between tutorial steps
     */
    fun createStepTransition(
        fromView: View?, 
        toView: View, 
        duration: Long = TutorialConstants.ANIMATION_DURATION_MEDIUM
    ): Animator {
        val animators = mutableListOf<Animator>()
        
        // Fade out previous view if exists
        fromView?.let { view ->
            val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f).apply {
                this.duration = duration / 2
            }
            animators.add(fadeOut)
        }
        
        // Fade in new view
        val fadeIn = ObjectAnimator.ofFloat(toView, "alpha", 0f, 1f).apply {
            this.duration = duration / 2
            startDelay = duration / 2
        }
        animators.add(fadeIn)
        
        val animator = AnimatorSet().apply {
            playTogether(animators)
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        activeAnimators.add(animator)
        return animator
    }
    
    /**
     * Starts an animation with callback
     */
    fun startAnimation(
        animator: Animator, 
        onStart: (() -> Unit)? = null,
        onEnd: (() -> Unit)? = null
    ) {
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                onStart?.invoke()
                
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Animation started")
                }
            }
            
            override fun onAnimationEnd(animation: Animator) {
                activeAnimators.remove(animation)
                onEnd?.invoke()
                
                if (TutorialConstants.DEBUG_MODE_ENABLED) {
                    Log.d(TutorialConstants.DEBUG_LOG_TAG, "Animation ended")
                }
            }
        })
        
        animator.start()
    }
    
    /**
     * Cancels all active animations
     */
    fun cancelAllAnimations() {
        activeAnimators.forEach { animator ->
            if (animator.isRunning) {
                animator.cancel()
            }
        }
        activeAnimators.clear()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "All animations cancelled")
        }
    }
    
    /**
     * Checks if any animations are currently running
     */
    fun hasRunningAnimations(): Boolean {
        return activeAnimators.any { it.isRunning }
    }
    
    /**
     * Gets the number of active animations
     */
    fun getActiveAnimationCount(): Int {
        return activeAnimators.size
    }
    
    /**
     * Cleanup method
     */
    fun cleanup() {
        cancelAllAnimations()
        
        if (TutorialConstants.DEBUG_MODE_ENABLED) {
            Log.d(TutorialConstants.DEBUG_LOG_TAG, "Animation controller cleaned up")
        }
    }
}