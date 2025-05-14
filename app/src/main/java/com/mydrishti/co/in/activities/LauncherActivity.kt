package com.mydrishti.co.`in`.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.utils.SessionManager
import com.mydrishti.co.`in`.databinding.ActivityLauncherBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LauncherActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize SessionManager with application context
        SessionManager.getInstance().init(applicationContext)

        // Apply animations
        applyAnimations()

        // Check login status using the singleton instance of SessionManager
        if (SessionManager.getInstance().isLoggedIn()) {
            // If logged in, navigate to dashboard with a slight delay for animations
            lifecycleScope.launch {
                delay(1000) // Short delay for better UX
                navigateToDashboard()
            }
        } else {
            // If not logged in, setup click listeners
            setupClickListeners()
        }
    }

    private fun applyAnimations() {
        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)

        // Apply animations with slight delays for sequence
        lifecycleScope.launch {
            binding.logoImageView.startAnimation(fadeIn)
            delay(500)
            binding.tvAppTitle.alpha = 0f
            binding.tvAppTitle.animate().alpha(1f).duration = 500
            delay(300)
            binding.btnLogin.visibility = android.view.View.VISIBLE
            binding.btnLogin.startAnimation(slideUp)
        }
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // No finish() here as we might want to come back to this screen
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}