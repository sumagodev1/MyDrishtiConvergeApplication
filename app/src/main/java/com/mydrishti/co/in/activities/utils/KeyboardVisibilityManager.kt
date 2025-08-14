package com.mydrishti.co.`in`.activities.utils

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Utility class for managing keyboard visibility and adjusting UI accordingly
 * Prevents login form elements from being hidden when keyboard appears
 */
object KeyboardVisibilityManager {

    private const val TAG = "KeyboardVisibilityManager"

    /**
     * Setup keyboard visibility handling for an activity
     * @param activity The activity to setup keyboard handling for
     * @param rootView The root view of the activity
     * @param adjustableView The view that should be adjusted when keyboard appears (e.g., login card)
     * @param onKeyboardVisibilityChanged Optional callback for keyboard visibility changes
     */
    fun setupKeyboardHandling(
        activity: Activity,
        rootView: View,
        adjustableView: View? = null,
        onKeyboardVisibilityChanged: ((Boolean, Int) -> Unit)? = null
    ) {
        CrashReportingManager.safeExecute(
            operation = {
                // Method 1: Use WindowInsets (Android 11+)
                ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
                    val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                    val isKeyboardVisible = imeInsets.bottom > 0
                    
                    onKeyboardVisibilityChanged?.invoke(isKeyboardVisible, imeInsets.bottom)
                    
                    if (adjustableView != null) {
                        adjustViewForKeyboard(adjustableView, isKeyboardVisible, imeInsets.bottom)
                    }
                    
                    insets
                }

                // Method 2: Fallback using ViewTreeObserver for older Android versions
                setupFallbackKeyboardDetection(activity, rootView, adjustableView, onKeyboardVisibilityChanged)
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error setting up keyboard handling", exception)
            }
        )
    }

    /**
     * Fallback keyboard detection using ViewTreeObserver
     */
    private fun setupFallbackKeyboardDetection(
        activity: Activity,
        rootView: View,
        adjustableView: View?,
        onKeyboardVisibilityChanged: ((Boolean, Int) -> Unit)?
    ) {
        val globalLayoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            private var wasKeyboardVisible = false

            override fun onGlobalLayout() {
                CrashReportingManager.safeExecute(
                    operation = {
                        val rect = Rect()
                        rootView.getWindowVisibleDisplayFrame(rect)
                        val screenHeight = rootView.rootView.height
                        val keypadHeight = screenHeight - rect.bottom
                        val isKeyboardVisible = keypadHeight > screenHeight * 0.15

                        if (isKeyboardVisible != wasKeyboardVisible) {
                            wasKeyboardVisible = isKeyboardVisible
                            onKeyboardVisibilityChanged?.invoke(isKeyboardVisible, keypadHeight)
                            
                            if (adjustableView != null) {
                                adjustViewForKeyboard(adjustableView, isKeyboardVisible, keypadHeight)
                            }
                        }
                    },
                    onError = { exception ->
                        CrashReportingManager.logError(TAG, "Error in global layout listener", exception)
                    }
                )
            }
        }

        rootView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    /**
     * Adjust a view's position/margins when keyboard appears
     * @param view The view to adjust
     * @param isKeyboardVisible Whether keyboard is visible
     * @param keyboardHeight Height of the keyboard
     */
    private fun adjustViewForKeyboard(view: View, isKeyboardVisible: Boolean, keyboardHeight: Int) {
        CrashReportingManager.safeExecute(
            operation = {
                val layoutParams = view.layoutParams
                
                when (layoutParams) {
                    is FrameLayout.LayoutParams -> {
                        if (isKeyboardVisible) {
                            // Add bottom margin to keep view visible above keyboard
                            layoutParams.bottomMargin = (keyboardHeight * 0.1).toInt().coerceAtLeast(32)
                        } else {
                            // Restore original margin
                            layoutParams.bottomMargin = view.resources.getDimensionPixelSize(
                                android.R.dimen.app_icon_size
                            ).coerceAtMost(32)
                        }
                        view.layoutParams = layoutParams
                    }
                    
                    is ConstraintLayout.LayoutParams -> {
                        if (isKeyboardVisible) {
                            // Add bottom margin to keep view visible above keyboard
                            layoutParams.bottomMargin = (keyboardHeight * 0.1).toInt().coerceAtLeast(32)
                        } else {
                            // Restore original margin
                            layoutParams.bottomMargin = view.resources.getDimensionPixelSize(
                                android.R.dimen.app_icon_size
                            ).coerceAtMost(32)
                        }
                        view.layoutParams = layoutParams
                    }
                    
                    is ViewGroup.MarginLayoutParams -> {
                        if (isKeyboardVisible) {
                            // Add bottom margin to keep view visible above keyboard
                            layoutParams.bottomMargin = (keyboardHeight * 0.1).toInt().coerceAtLeast(32)
                        } else {
                            // Restore original margin
                            layoutParams.bottomMargin = view.resources.getDimensionPixelSize(
                                android.R.dimen.app_icon_size
                            ).coerceAtMost(32)
                        }
                        view.layoutParams = layoutParams
                    }
                }
                
                // Request layout to apply changes
                view.requestLayout()
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error adjusting view for keyboard", exception)
            }
        )
    }

    /**
     * Ensure a specific view remains visible when keyboard appears
     * @param targetView The view that must remain visible
     * @param rootView The root view of the screen
     */
    fun ensureViewVisibleAboveKeyboard(targetView: View, rootView: View) {
        CrashReportingManager.safeExecute(
            operation = {
                rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        val rect = Rect()
                        rootView.getWindowVisibleDisplayFrame(rect)
                        val screenHeight = rootView.rootView.height
                        val keypadHeight = screenHeight - rect.bottom
                        val isKeyboardVisible = keypadHeight > screenHeight * 0.15

                        if (isKeyboardVisible) {
                            // Check if target view is visible
                            val location = IntArray(2)
                            targetView.getLocationOnScreen(location)
                            val viewBottom = location[1] + targetView.height

                            if (viewBottom > rect.bottom) {
                                // View is hidden by keyboard, scroll to make it visible
                                val scrollAmount = viewBottom - rect.bottom + 50 // Add some padding
                                rootView.scrollBy(0, scrollAmount)
                            }
                        }
                    }
                })
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error ensuring view visibility", exception)
            }
        )
    }

    /**
     * Hide keyboard programmatically
     * @param activity The activity context
     */
    fun hideKeyboard(activity: Activity) {
        CrashReportingManager.safeExecute(
            operation = {
                val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                val currentFocus = activity.currentFocus
                if (currentFocus != null) {
                    inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error hiding keyboard", exception)
            }
        )
    }

    /**
     * Show keyboard for a specific view
     * @param view The view to show keyboard for
     */
    fun showKeyboard(view: View) {
        CrashReportingManager.safeExecute(
            operation = {
                view.requestFocus()
                val inputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                inputMethodManager.showSoftInput(view, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error showing keyboard", exception)
            }
        )
    }
}