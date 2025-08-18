package com.mydrishti.co.`in`.activities.tutorial.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.mydrishti.co.`in`.R

/**
 * Dialog for collecting detailed skip reasons and feedback
 */
class SkipReasonDialog(
    private val context: Context,
    private val onReasonSelected: (reason: String) -> Unit,
    private val onSkipWithoutReason: () -> Unit
) {
    
    private var dialog: AlertDialog? = null
    private var reasonRadioGroup: RadioGroup? = null
    private var feedbackEditText: EditText? = null
    
    private val skipReasons = listOf(
        "The tutorial is too slow" to "too_slow",
        "I already understand the app" to "already_understand",
        "The instructions are unclear" to "unclear_instructions",
        "I prefer learning by exploring" to "prefer_exploring",
        "The tutorial is too long" to "too_long",
        "I don't have time right now" to "no_time",
        "The tutorial doesn't match my needs" to "not_relevant",
        "Technical issues with the tutorial" to "technical_issues"
    )
    
    /**
     * Shows the skip reason dialog
     */
    fun show() {
        val builder = AlertDialog.Builder(context)
        val dialogView = createDialogLayout()
        
        builder.setTitle("Help Us Improve")
        builder.setView(dialogView)
        
        builder.setPositiveButton("Submit Feedback") { _, _ ->
            val selectedReason = getSelectedReason()
            val feedback = feedbackEditText?.text?.toString()?.trim()
            
            val combinedReason = if (feedback.isNullOrEmpty()) {
                selectedReason
            } else {
                "$selectedReason|feedback:$feedback"
            }
            
            onReasonSelected(combinedReason)
            dismiss()
        }
        
        builder.setNegativeButton("Skip Without Feedback") { _, _ ->
            onSkipWithoutReason()
            dismiss()
        }
        
        builder.setNeutralButton("Continue Tutorial") { _, _ ->
            dismiss()
        }
        
        builder.setCancelable(true)
        builder.setOnCancelListener {
            dismiss()
        }
        
        dialog = builder.create()
        dialog?.show()
    }
    
    /**
     * Creates the dialog layout
     */
    private fun createDialogLayout(): View {
        val scrollView = ScrollView(context)
        val mainLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 24)
        }
        
        // Introduction text
        val introText = TextView(context).apply {
            text = "We'd love to know why you're skipping the tutorial so we can make it better for future users."
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }
        mainLayout.addView(introText)
        
        // Reason selection
        val reasonLabel = TextView(context).apply {
            text = "What's the main reason you're skipping?"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        }
        mainLayout.addView(reasonLabel)
        
        reasonRadioGroup = RadioGroup(context)
        
        skipReasons.forEachIndexed { index, (reasonText, reasonCode) ->
            val radioButton = RadioButton(context).apply {
                text = reasonText
                id = index
                setPadding(0, 8, 0, 8)
            }
            reasonRadioGroup?.addView(radioButton)
        }
        
        // Select first option by default
        reasonRadioGroup?.check(0)
        
        mainLayout.addView(reasonRadioGroup)
        
        // Additional feedback
        val feedbackLabel = TextView(context).apply {
            text = "Any additional feedback? (Optional)"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 24, 0, 8)
        }
        mainLayout.addView(feedbackLabel)
        
        feedbackEditText = EditText(context).apply {
            hint = "Tell us more about your experience..."
            minLines = 3
            maxLines = 5
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setBackgroundResource(android.R.drawable.edit_text)
            setPadding(16, 16, 16, 16)
        }
        mainLayout.addView(feedbackEditText)
        
        // Thank you note
        val thankYouText = TextView(context).apply {
            text = "Thank you for helping us improve the tutorial experience!"
            textSize = 12f
            setTextColor(context.getColor(android.R.color.darker_gray))
            setPadding(0, 24, 0, 0)
        }
        mainLayout.addView(thankYouText)
        
        scrollView.addView(mainLayout)
        return scrollView
    }
    
    /**
     * Gets the selected reason code
     */
    private fun getSelectedReason(): String {
        val selectedId = reasonRadioGroup?.checkedRadioButtonId ?: 0
        return skipReasons.getOrNull(selectedId)?.second ?: "unknown"
    }
    
    /**
     * Dismisses the dialog
     */
    fun dismiss() {
        dialog?.dismiss()
        dialog = null
        reasonRadioGroup = null
        feedbackEditText = null
    }
    
    /**
     * Checks if dialog is currently showing
     */
    fun isShowing(): Boolean {
        return dialog?.isShowing == true
    }
}