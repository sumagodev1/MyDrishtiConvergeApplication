package com.mydrishti.co.`in`.activities.tutorial.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.*
import com.mydrishti.co.`in`.R

/**
 * Dialog for confirming tutorial restart with optional reason collection
 */
class RestartConfirmationDialog(
    private val context: Context,
    private val title: String,
    private val message: String,
    private val positiveButtonText: String,
    private val negativeButtonText: String?,
    private val onPositiveClick: (restartReason: String?) -> Unit,
    private val onNegativeClick: (() -> Unit)?,
    private val showReasonOption: Boolean = false
) {
    
    private var dialog: AlertDialog? = null
    private var reasonSpinner: Spinner? = null
    private var customReasonEditText: EditText? = null
    
    private val restartReasons = listOf(
        "Want to practice the steps again",
        "Missed some information the first time",
        "Showing someone else how to use the app",
        "Want to refresh my memory",
        "Had technical issues during tutorial",
        "Want to see if anything changed",
        "Other (please specify)"
    )
    
    /**
     * Shows the restart confirmation dialog
     */
    fun show() {
        val builder = AlertDialog.Builder(context)
        
        // Create custom layout if reason option is enabled
        val dialogView = if (showReasonOption) {
            createCustomLayout()
        } else {
            null
        }
        
        builder.setTitle(title)
        
        if (dialogView != null) {
            builder.setView(dialogView)
        } else {
            builder.setMessage(message)
        }
        
        // Positive button
        builder.setPositiveButton(positiveButtonText) { _, _ ->
            val restartReason = getSelectedRestartReason()
            onPositiveClick(restartReason)
            dismiss()
        }
        
        // Negative button
        negativeButtonText?.let { buttonText ->
            builder.setNegativeButton(buttonText) { _, _ ->
                onNegativeClick?.invoke()
                dismiss()
            }
        }
        
        builder.setCancelable(true)
        builder.setOnCancelListener {
            onNegativeClick?.invoke()
        }
        
        dialog = builder.create()
        dialog?.show()
    }
    
    /**
     * Creates custom layout with restart reason options
     */
    private fun createCustomLayout(): View {
        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        
        // Message text
        val messageTextView = TextView(context).apply {
            text = message
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }
        mainLayout.addView(messageTextView)
        
        // Reason section
        val reasonLabel = TextView(context).apply {
            text = "Why are you restarting? (Optional)"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        mainLayout.addView(reasonLabel)
        
        // Reason spinner
        reasonSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, restartReasons).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val isOtherSelected = position == restartReasons.size - 1
                    customReasonEditText?.visibility = if (isOtherSelected) View.VISIBLE else View.GONE
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        mainLayout.addView(reasonSpinner)
        
        // Custom reason input
        customReasonEditText = EditText(context).apply {
            hint = "Please specify your reason..."
            visibility = View.GONE
            setPadding(0, 16, 0, 0)
        }
        mainLayout.addView(customReasonEditText)
        
        return mainLayout
    }
    
    /**
     * Gets the selected restart reason
     */
    private fun getSelectedRestartReason(): String? {
        if (!showReasonOption) return null
        
        val selectedPosition = reasonSpinner?.selectedItemPosition ?: 0
        val selectedReason = restartReasons.getOrNull(selectedPosition) ?: return null
        
        return if (selectedReason == "Other (please specify)") {
            val customReason = customReasonEditText?.text?.toString()?.trim()
            if (customReason.isNullOrEmpty()) "Other (no details)" else "Other: $customReason"
        } else {
            selectedReason
        }
    }
    
    /**
     * Dismisses the dialog
     */
    fun dismiss() {
        dialog?.dismiss()
        dialog = null
        reasonSpinner = null
        customReasonEditText = null
    }
    
    /**
     * Checks if dialog is currently showing
     */
    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
}