package com.mydrishti.co.`in`.activities.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.databinding.DialogLoadingBinding

/**
 * A simple loading dialog that shows a progress indicator with optional text
 */
class LoadingDialog(context: Context) : Dialog(context) {
    
    private lateinit var binding: DialogLoadingBinding
    private var loadingText: String = context.getString(R.string.loading)
    
    init {
        // Make dialog cancelable on back press but not on outside touch
        setCancelable(true)
        setCanceledOnTouchOutside(false)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set up the dialog
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        
        // Inflate binding and set content view
        binding = DialogLoadingBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)
        
        // Set loading text
        binding.tvLoadingText.text = loadingText
        
        // Set dialog width and height
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParams
    }
    
    /**
     * Set custom loading text
     */
    fun setLoadingText(text: String) {
        loadingText = text
        if (::binding.isInitialized) {
            binding.tvLoadingText.text = text
        }
    }
    
    /**
     * Show the loading dialog with custom text
     */
    fun showWithText(text: String) {
        setLoadingText(text)
        show()
    }
}