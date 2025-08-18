package com.mydrishti.co.`in`.activities.tutorial

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import com.mydrishti.co.`in`.R

/**
 * Dialog for confirming tutorial skip action
 */
class TutorialSkipDialog(
    private val context: Context,
    private val onConfirmSkip: () -> Unit,
    private val onCancelSkip: () -> Unit
) {
    
    private var dialog: AlertDialog? = null
    
    /**
     * Shows the skip confirmation dialog
     */
    fun show() {
        val builder = AlertDialog.Builder(context)
        
        builder.setTitle("Skip Tutorial?")
        builder.setMessage("Are you sure you want to skip the tutorial? You can restart it later from the settings menu.")
        
        builder.setPositiveButton("Skip") { _, _ ->
            onConfirmSkip()
            dismiss()
        }
        
        builder.setNegativeButton("Continue Tutorial") { _, _ ->
            onCancelSkip()
            dismiss()
        }
        
        builder.setCancelable(true)
        builder.setOnCancelListener {
            onCancelSkip()
        }
        
        dialog = builder.create()
        dialog?.show()
    }
    
    /**
     * Dismisses the dialog
     */
    fun dismiss() {
        dialog?.dismiss()
        dialog = null
    }
    
    /**
     * Checks if dialog is currently showing
     */
    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
}