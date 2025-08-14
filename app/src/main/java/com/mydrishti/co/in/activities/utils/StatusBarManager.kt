package com.mydrishti.co.`in`.activities.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Utility class for managing status bar appearance and handling system window insets
 * to prevent header collision issues
 */
object StatusBarManager {

    /**
     * Configure the status bar for proper display without collision
     * @param activity The activity to configure
     * @param isLightStatusBar Whether to use light status bar (dark icons)
     * @param useTransparentStatusBar Whether to make status bar transparent
     * @param customColor Custom color for status bar (optional)
     */
    fun configureStatusBar(activity: Activity, isLightStatusBar: Boolean = true, useTransparentStatusBar: Boolean = false, customColor: String? = null) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ (API 30+)
                val windowInsetsController = ViewCompat.getWindowInsetsController(activity.window.decorView)
                windowInsetsController?.isAppearanceLightStatusBars = isLightStatusBar
                
                if (useTransparentStatusBar) {
                    // Make status bar transparent
                    activity.window.statusBarColor = Color.TRANSPARENT
                    activity.window.setFlags(
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    )
                } else {
                    // Use custom color or semi-transparent dark status bar
                    activity.window.statusBarColor = if (customColor != null) {
                        Color.parseColor(customColor)
                    } else {
                        Color.parseColor("#80000000")
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Android 6.0+ (API 23+)
                if (useTransparentStatusBar) {
                    activity.window.decorView.systemUiVisibility = if (isLightStatusBar) {
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    }
                    activity.window.statusBarColor = Color.TRANSPARENT
                } else {
                    // Use light status bar without full screen layout
                    activity.window.decorView.systemUiVisibility = if (isLightStatusBar) {
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        0
                    }
                    activity.window.statusBarColor = if (customColor != null) {
                        Color.parseColor(customColor)
                    } else {
                        Color.parseColor("#80000000")
                    }
                }
            } else {
                // Fallback for older versions
                activity.window.statusBarColor = if (customColor != null) {
                    Color.parseColor(customColor)
                } else {
                    Color.parseColor("#80000000")
                }
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("StatusBarManager", "Error configuring status bar: ${e.message}")
        }
    }

    /**
     * Apply window insets to a view to handle system bars properly
     * @param view The view to apply insets to (usually the root view)
     * @param applyTopInset Whether to apply top inset (for status bar)
     * @param applyBottomInset Whether to apply bottom inset (for navigation bar)
     */
    fun applyWindowInsets(
        view: View, 
        applyTopInset: Boolean = true, 
        applyBottomInset: Boolean = false
    ) {
        try {
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                
                val topPadding = if (applyTopInset) systemBars.top else 0
                val bottomPadding = if (applyBottomInset) systemBars.bottom else 0
                
                v.setPadding(
                    systemBars.left,
                    topPadding,
                    systemBars.right,
                    bottomPadding
                )
                
                insets
            }
        } catch (e: Exception) {
            // Log error but don't crash the app
            android.util.Log.e("StatusBarManager", "Error applying window insets: ${e.message}")
        }
    }

    /**
     * Get the status bar height in pixels
     * @param activity The activity context
     * @return Status bar height in pixels
     */
    fun getStatusBarHeight(activity: Activity): Int {
        return try {
            val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                activity.resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }
        } catch (e: Exception) {
            android.util.Log.e("StatusBarManager", "Error getting status bar height: ${e.message}")
            0
        }
    }

    /**
     * Configure toolbar margins to account for status bar
     * @param toolbar The toolbar view
     * @param activity The activity context
     */
    fun configureToolbarMargins(toolbar: View, activity: Activity) {
        try {
            val statusBarHeight = getStatusBarHeight(activity)
            val layoutParams = toolbar.layoutParams as? android.view.ViewGroup.MarginLayoutParams
            layoutParams?.let {
                it.topMargin = statusBarHeight
                toolbar.layoutParams = it
            }
        } catch (e: Exception) {
            android.util.Log.e("StatusBarManager", "Error configuring toolbar margins: ${e.message}")
        }
    }
}