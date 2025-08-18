package com.mydrishti.co.`in`.activities.tutorial.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.*
import com.mydrishti.co.`in`.R

/**
 * Dialog for collecting detailed restart reasons and feedback
 */
class RestartReasonDialog(
    private val context: Context,
    private val onReasonSelected: (reason: String) -> Unit,
    private val onRestartWithoutReason: () -> Unit,
    private val onCancel: () -> Unit
) {
    
    private var dialog: AlertDialog? = null
    private var reasonRadioGroup: RadioGroup? = null
    private var feedbackEditText: EditText? = null
    
    private val restartReasons = listOf(
        "I want to practice the steps again" to "practice_steps",
        "I missed some information the first time" to "missed_information",
        "I'm showing someone else how to use the app" to "showing_others",
        "I want to refresh my memory" to "refresh_memory",
        "I had technical issues during the tutorial" to "technical_issues",
        "I want to see if the tutorial has been updated" to "check_updates",
        "The tutorial was helpful and I want to see it again" to "helpful_repeat",
        "I accidentally skipped parts I wanted to see" to "accidental_skip"
    )
    
    /**
     * Shows the restart reason dialog
     */
    fun show() {
        val builder = AlertDialog.Builder(context)
        val dialogView = createDialogLayout()
        
        builder.setTitle("Why Restart the Tutorial?")
        builder.setView(dialogView)
        
        builder.setPositiveButton("Restart Tutorial") { _, _ ->
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
        
        builder.setNegativeButton("Restart Without Reason") { _, _ ->
            onRestartWithoutReason()
            dismiss()
        }
        
        builder.setNeutralButton("Cancel") { _, _ ->
            onCancel()
            dismiss()
        }
        
        builder.setCancelable(true)
        builder.setOnCancelListener {
            onCancel()
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
            text = "Help us understand why you're restarting the tutorial. This helps us improve the experience for everyone."
            textSize = 16f
            setPadding(0, 0, 0, 24)
        }
        mainLayout.addView(introText)
        
        // Reason selection
        val reasonLabel = TextView(context).apply {
            text = "What's your main reason for restarting?"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 0, 0, 12)
        }
        mainLayout.addView(reasonLabel)
        
        reasonRadioGroup = RadioGroup(context)
        
        restartReasons.forEachIndexed { index, (reasonText, reasonCode) ->
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
            text = "Any additional thoughts? (Optional)"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 24, 0, 8)
        }
        mainLayout.addView(feedbackLabel)
        
        feedbackEditText = EditText(context).apply {
            hint = "Tell us more about your experience or suggestions for improvement..."
            minLines = 3
            maxLines = 5
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setBackgroundResource(android.R.drawable.edit_text)
            setPadding(16, 16, 16, 16)
        }
        mainLayout.addView(feedbackEditText)
        
        // Thank you note
        val thankYouText = TextView(context).apply {
            text = "Thank you for helping us improve the tutorial!"
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
        return restartReasons.getOrNull(selectedId)?.second ?: "unknown"
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