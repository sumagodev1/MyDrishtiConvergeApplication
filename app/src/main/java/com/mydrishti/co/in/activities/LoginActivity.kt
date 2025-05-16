package com.mydrishti.co.`in`.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.mydrishti.co.`in`.R
import com.mydrishti.co.`in`.activities.api.ApiClient
import com.mydrishti.co.`in`.activities.api.ApiService
import com.mydrishti.co.`in`.activities.models.LoginRequest
import com.mydrishti.co.`in`.activities.models.LoginResponse
import com.mydrishti.co.`in`.activities.models.LoginResponseModel
import com.mydrishti.co.`in`.activities.utils.NetworkUtils
import com.mydrishti.co.`in`.databinding.ActivityLoginBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var encryptedPrefs: SharedPreferences
    private val TAG = "LoginActivity"
    private lateinit var apiService: ApiService

    companion object {
        // Keys for storing credentials
        const val PREF_FILE_NAME = "my_drishti_secure_prefs"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_TOKEN = "auth_token"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize EncryptedSharedPreferences
        initEncryptedPrefs()

        // Initialize API service
        apiService = ApiClient.getApiService()

        // Apply custom theme colors
        applyThemeColors()

        // Apply animations
        applyAnimations()

        // Check if user is already logged in
        checkPreviousLogin()

        // Setup click listeners
        setupClickListeners()
    }

    private fun initEncryptedPrefs() {
        try {
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            encryptedPrefs = EncryptedSharedPreferences.create(
                applicationContext,
                PREF_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences: ${e.message}")
            // Fallback to regular SharedPreferences in case of error
            encryptedPrefs = getSharedPreferences(PREF_FILE_NAME, MODE_PRIVATE)
        }
    }

    private fun applyThemeColors() {
        // Set green and white theme colors
        binding.btnLogin.setBackgroundColor(resources.getColor(R.color.green_primary, theme))
        binding.cardLogin.setCardBackgroundColor(resources.getColor(R.color.white, theme))
    }

    private fun checkPreviousLogin() {
        if (encryptedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            val savedEmail = encryptedPrefs.getString(KEY_EMAIL, "")
            val savedPassword = encryptedPrefs.getString(KEY_PASSWORD, "")

            // Auto-fill the credentials
            binding.etEmail.setText(savedEmail)
            binding.etPassword.setText(savedPassword)

            // Show a message to user
            Toast.makeText(this, "Tap login to continue", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyAnimations() {
        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)

        // Apply animations with slight delays for sequence
        lifecycleScope.launch {
            binding.lottieAnimationView.startAnimation(fadeIn)

            delay(300)
            binding.tvAppTitle.visibility = View.VISIBLE
            binding.tvAppTitle.startAnimation(slideInRight)

            delay(150)
            binding.tvLoginSubtitle.visibility = View.VISIBLE
            binding.tvLoginSubtitle.startAnimation(slideInRight)

            delay(300)
            binding.cardLogin.visibility = View.VISIBLE
           
            
        }
    }

    private fun setupClickListeners() {
        // Login button click listener
        binding.btnLogin.setOnClickListener {
            if (validateInput()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString()

                if (NetworkUtils.isNetworkAvailable(this)) {
                    showLoading(true)
                    performLogin(email, password)
                } else {
                    Toast.makeText(
                        this,
                        "No internet connection. Please check your network.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        // Validate email
        val email = binding.etEmail.text.toString().trim()
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Enter a valid email address"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Validate password
        val password = binding.etPassword.text.toString()
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else if (password.length < 8) {
            binding.tilPassword.error = "Password must be at least 8 characters"
            isValid = false
        } /*else if (!password.matches(".*[A-Z].*".toRegex())) {
            binding.tilPassword.error = "Password must contain at least one uppercase letter"
            isValid = false
        } else if (!password.matches(".*[a-z].*".toRegex())) {
            binding.tilPassword.error = "Password must contain at least one lowercase letter"
            isValid = false
        } else if (!password.matches(".*[0-9].*".toRegex())) {
            binding.tilPassword.error = "Password must contain at least one number"
            isValid = false
        } else if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*".toRegex())) {
            binding.tilPassword.error = "Password must contain at least one special character"
            isValid = false
        } */else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun performLogin(email: String, password: String) {
        val loginRequest = LoginRequest(email, password)

        apiService.login(loginRequest).enqueue(object : Callback<LoginResponseModel> {
            override fun onResponse(call: Call<LoginResponseModel>, response: Response<LoginResponseModel>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // Save credentials securely
                    saveCredentials(email, password, loginResponse.accessToken)

                    showLoading(false)
                    navigateToDashboard()
                } else {
                    showLoading(false)
                    handleLoginError(response.code())
                }
            }

            override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {
                showLoading(false)
                Log.e(TAG, "Login failed: ${t.message}")
                Toast.makeText(
                    this@LoginActivity,
                    "Login failed: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun saveCredentials(email: String, password: String, token: String) {
        encryptedPrefs.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putString(KEY_TOKEN, token)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    private fun handleLoginError(responseCode: Int) {
        when (responseCode) {
            401 -> Toast.makeText(
                this,
                "Invalid email or password. Please try again.",
                Toast.LENGTH_LONG
            ).show()

            500 -> Toast.makeText(
                this,
                "Server error. Please try again later.",
                Toast.LENGTH_LONG
            ).show()

            else -> Toast.makeText(
                this,
                "Login failed with code: $responseCode",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnLogin.text = ""
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
        } else {
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.text = getString(R.string.login)
            binding.btnLogin.isEnabled = true
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)

        // Create animation for shared element transition
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            this,
            binding.tvAppTitle,
            "app_title_transition"
        )

        startActivity(intent, options.toBundle())
        finish()
    }
}