package com.mydrishti.co.`in`.activities.tutorial.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.mydrishti.co.`in`.R

/**
 * Enhanced skip dialog with better UX and optional reason collection
 */
class EnhancedSkipDialog(
    private val context: Context,
    private val title: String,
    private val message: String,
    private val positiveButtonText: String,
    private val negativeButtonText: String?,
    private val neutralButtonText: String? = null,
    private val onPositiveClick: (skipReason: String?) -> Unit,
    private val onNegativeClick: (() -> Unit)?,
    private val onNeutralClick: ((skipReason: String?) -> Unit)? = null,
    private val showSkipReasonOption: Boolean = false
) {
    
    private var dialog: AlertDialog? = null
    private var reasonSpinner: Spinner? = null
    private var customReasonEditText: EditText? = null
    private var reasonContainer: LinearLayout? = null
    
    private val skipReasons = listOf(
        "Tutorial is too long",
        "I already know how to use the app",
        "I prefer to explore on my own",
        "The tutorial is confusing",
        "I don't have time right now",
        "Other (please specify)"
    )
    
    /**
     * Shows the enhanced skip dialog
     */
    fun show() {
        val builder = AlertDialog.Builder(context)
        
        // Create custom layout if skip reason option is enabled
        val dialogView = if (showSkipReasonOption) {
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
            val skipReason = getSelectedSkipReason()
            onPositiveClick(skipReason)
            dismiss()
        }
        
        // Negative button
        negativeButtonText?.let { buttonText ->
            builder.setNegativeButton(buttonText) { _, _ ->
                onNegativeClick?.invoke()
                dismiss()
            }
        }
        
        // Neutral button
        neutralButtonText?.let { buttonText ->
            builder.setNeutralButton(buttonText) { _, _ ->
                val skipReason = getSelectedSkipReason()
                onNeutralClick?.invoke(skipReason)
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
     * Creates custom layout with skip reason options
     */
    private fun createCustomLayout(): View {
        val inflater = LayoutInflater.from(context)
        val dialogView = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        
        // Message text
        val messageTextView = TextView(context).apply {
            text = message
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }
        dialogView.addView(messageTextView)
        
        // Reason container
        reasonContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.VISIBLE
        }
        
        // Reason label
        val reasonLabel = TextView(context).apply {
            text = "Why are you skipping? (Optional)"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 8)
        }
        reasonContainer?.addView(reasonLabel)
        
        // Reason spinner
        reasonSpinner = Spinner(context).apply {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, skipReasons).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val isOtherSelected = position == skipReasons.size - 1
                    customReasonEditText?.visibility = if (isOtherSelected) View.VISIBLE else View.GONE
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
        reasonContainer?.addView(reasonSpinner)
        
        // Custom reason input
        customReasonEditText = EditText(context).apply {
            hint = "Please specify your reason..."
            visibility = View.GONE
            setPadding(0, 16, 0, 0)
        }
        reasonContainer?.addView(customReasonEditText)
        
        dialogView.addView(reasonContainer)
        
        return dialogView
    }
    
    /**
     * Gets the selected skip reason
     */
    private fun getSelectedSkipReason(): String? {
        if (!showSkipReasonOption) return null
        
        val selectedPosition = reasonSpinner?.selectedItemPosition ?: 0
        val selectedReason = skipReasons.getOrNull(selectedPosition) ?: return null
        
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
        reasonContainer = null
    }
    
    /**
     * Checks if dialog is currently showing
     */
    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
}