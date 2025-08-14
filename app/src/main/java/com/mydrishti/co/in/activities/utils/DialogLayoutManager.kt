package com.mydrishti.co.`in`.activities.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Utility class for managing dialog layouts to ensure buttons are always visible
 * Fixes OK/Cancel button visibility issues
 */
object DialogLayoutManager {

    private const val TAG = "DialogLayoutManager"

    /**
     * Configure a dialog to ensure buttons are always visible
     * @param dialog The dialog to configure
     * @param ensureButtonVisibility Whether to ensure button visibility
     */
    fun configureDialog(dialog: Dialog, ensureButtonVisibility: Boolean = true) {
        CrashReportingManager.safeExecute(
            operation = {
                val window = dialog.window
                if (window != null) {
                    // Set dialog to resize when keyboard appears
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    
                    // Set dialog width with margins (not full screen)
                    val layoutParams = window.attributes
                    val displayMetrics = dialog.context.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val marginInPx = (32 * displayMetrics.density).toInt() // 32dp margins on each side
                    
                    layoutParams.width = screenWidth - (marginInPx * 2) // Subtract margins from both sides
                    layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                    window.attributes = layoutParams
                    
                    if (ensureButtonVisibility) {
                        setupButtonVisibilityHandling(dialog)
                    }
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error configuring dialog", exception)
            }
        )
    }

    /**
     * Setup button visibility handling for a dialog
     */
    private fun setupButtonVisibilityHandling(dialog: Dialog) {
        CrashReportingManager.safeExecute(
            operation = {
                val decorView = dialog.window?.decorView
                if (decorView != null) {
                    ViewCompat.setOnApplyWindowInsetsListener(decorView) { view, insets ->
                        val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
                        val isKeyboardVisible = imeInsets.bottom > 0
                        
                        if (isKeyboardVisible) {
                            ensureDialogButtonsVisible(dialog, imeInsets.bottom)
                        }
                        
                        insets
                    }
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error setting up button visibility handling", exception)
            }
        )
    }

    /**
     * Ensure dialog buttons remain visible above keyboard
     */
    private fun ensureDialogButtonsVisible(dialog: Dialog, keyboardHeight: Int) {
        CrashReportingManager.safeExecute(
            operation = {
                val window = dialog.window
                if (window != null) {
                    val decorView = window.decorView
                    val contentView = decorView.findViewById<ViewGroup>(android.R.id.content)
                    
                    // Add bottom padding to ensure buttons are visible
                    val bottomPadding = (keyboardHeight * 0.1).toInt().coerceAtLeast(50)
                    contentView?.setPadding(
                        contentView.paddingLeft,
                        contentView.paddingTop,
                        contentView.paddingRight,
                        bottomPadding
                    )
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error ensuring dialog buttons visible", exception)
            }
        )
    }

    /**
     * Create an AlertDialog with proper button visibility configuration
     * @param context The context
     * @param title Dialog title
     * @param message Dialog message
     * @param positiveButtonText Positive button text
     * @param negativeButtonText Negative button text (optional)
     * @param onPositiveClick Positive button click listener
     * @param onNegativeClick Negative button click listener (optional)
     * @return Configured AlertDialog
     */
    fun createAlertDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String? = null,
        onPositiveClick: (() -> Unit)? = null,
        onNegativeClick: (() -> Unit)? = null
    ): AlertDialog {
        return CrashReportingManager.safeExecute(
            operation = {
                val builder = AlertDialog.Builder(context)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveButtonText) { dialog, _ ->
                        onPositiveClick?.invoke()
                        dialog.dismiss()
                    }
                
                if (negativeButtonText != null) {
                    builder.setNegativeButton(negativeButtonText) { dialog, _ ->
                        onNegativeClick?.invoke()
                        dialog.dismiss()
                    }
                }
                
                val dialog = builder.create()
                configureDialog(dialog, ensureButtonVisibility = true)
                
                // Apply rounded corners to all dialogs
                try {
                    val context = dialog.context
                    val resourceId = context.resources.getIdentifier("dialog_rounded_background", "drawable", context.packageName)
                    if (resourceId != 0) {
                        dialog.window?.setBackgroundDrawableResource(resourceId)
                    }
                } catch (e: Exception) {
                    // Fallback to default if custom drawable not found
                    CrashReportingManager.logWarning("DialogLayoutManager", "Could not apply rounded background",
                        mapOf("error" to e.message) as Map<String, Any>?
                    )
                }
                
                dialog
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error creating alert dialog", exception)
            }
        ) ?: AlertDialog.Builder(context).create()
    }

    /**
     * Create a confirmation dialog with proper button visibility
     * @param context The context
     * @param title Dialog title
     * @param message Dialog message
     * @param onConfirm Confirmation callback
     * @param onCancel Cancel callback (optional)
     * @return Configured AlertDialog
     */
    fun createConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ): AlertDialog {
        val dialog = createAlertDialog(
            context = context,
            title = title,
            message = message,
            positiveButtonText = "OK",
            negativeButtonText = "Cancel",
            onPositiveClick = onConfirm,
            onNegativeClick = onCancel
        )
        
        // Apply button colors for confirmation dialogs
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            
            // Check if this is a destructive action (like Delete, Logout)
            val positiveText = positiveButton?.text?.toString()?.lowercase()
            val isDestructiveAction = positiveText?.contains("delete") == true || 
                                    positiveText?.contains("logout") == true ||
                                    positiveText?.contains("remove") == true
            
            if (isDestructiveAction) {
                // For destructive actions: positive button is red, negative (cancel) is green
                positiveButton?.setTextColor(android.graphics.Color.parseColor("#F44336"))
                negativeButton?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            } else {
                // For normal confirmations: positive button is green, negative (cancel) is red
                positiveButton?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
                negativeButton?.setTextColor(android.graphics.Color.parseColor("#F44336"))
            }
        }
        
        return dialog
    }

    /**
     * Fix button visibility for existing dialogs
     * @param dialog The dialog to fix
     */
    fun fixExistingDialogButtons(dialog: Dialog) {
        CrashReportingManager.safeExecute(
            operation = {
                // Find and adjust button container
                val window = dialog.window
                if (window != null) {
                    val decorView = window.decorView
                    val buttonPanel = findButtonPanel(decorView)
                    
                    if (buttonPanel != null) {
                        // Ensure button panel has proper margins
                        val layoutParams = buttonPanel.layoutParams as? ViewGroup.MarginLayoutParams
                        if (layoutParams != null) {
                            layoutParams.bottomMargin = layoutParams.bottomMargin.coerceAtLeast(32)
                            buttonPanel.layoutParams = layoutParams
                        }
                        
                        // Ensure buttons are properly sized
                        adjustButtonSizes(buttonPanel)
                    }
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error fixing existing dialog buttons", exception)
            }
        )
    }

    /**
     * Find the button panel in a dialog view hierarchy
     */
    private fun findButtonPanel(view: View): ViewGroup? {
        return CrashReportingManager.safeExecute(
            operation = {
                if (view is ViewGroup) {
                    // Check if this view contains buttons
                    for (i in 0 until view.childCount) {
                        val child = view.getChildAt(i)
                        if (child is Button) {
                            return@safeExecute view
                        }
                        
                        // Recursively search child views
                        val result = findButtonPanel(child)
                        if (result != null) {
                            return@safeExecute result
                        }
                    }
                }
                null
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error finding button panel", exception)
            }
        )
    }

    /**
     * Adjust button sizes to ensure they're properly visible
     */
    private fun adjustButtonSizes(buttonPanel: ViewGroup) {
        CrashReportingManager.safeExecute(
            operation = {
                for (i in 0 until buttonPanel.childCount) {
                    val child = buttonPanel.getChildAt(i)
                    if (child is Button) {
                        // Ensure minimum button height
                        val minHeight = child.context.resources.getDimensionPixelSize(
                            android.R.dimen.app_icon_size
                        ).coerceAtLeast(48) // 48dp minimum touch target
                        
                        if (child.minimumHeight < minHeight) {
                            child.minimumHeight = minHeight
                        }
                        
                        // Ensure proper padding
                        val padding = child.context.resources.getDimensionPixelSize(
                            android.R.dimen.app_icon_size
                        ) / 4
                        child.setPadding(padding, padding, padding, padding)
                    }
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error adjusting button sizes", exception)
            }
        )
    }

    /**
     * Show a dialog with proper configuration
     * @param dialog The dialog to show
     */
    fun showDialog(dialog: Dialog) {
        CrashReportingManager.safeExecute(
            operation = {
                configureDialog(dialog)
                dialog.show()
                
                // Apply additional fixes after showing
                fixExistingDialogButtons(dialog)
                
                // Ensure proper width with margins after showing
                dialog.window?.let { window ->
                    val layoutParams = window.attributes
                    val displayMetrics = dialog.context.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val marginInPx = (32 * displayMetrics.density).toInt() // 32dp margins on each side
                    
                    layoutParams.width = screenWidth - (marginInPx * 2)
                    window.attributes = layoutParams
                }
            },
            onError = { exception ->
                CrashReportingManager.logError(TAG, "Error showing dialog", exception)
                // Fallback to simple show
                try {
                    dialog.show()
                } catch (e: Exception) {
                    CrashReportingManager.logError(TAG, "Error in fallback dialog show", e)
                }
            }
        )
    }
}