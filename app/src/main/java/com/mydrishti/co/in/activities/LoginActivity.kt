package com.mydrishti.co.`in`.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.textfield.TextInputLayout
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
    private var isPasswordAutoFilled = false

    companion object {
        // Keys for storing credentials
        const val PREF_FILE_NAME = "my_drishti_secure_prefs"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_TOKEN = "auth_token"
        const val KEY_IS_LOGGED_IN = "is_logged_in"
        const val KEY_PASSWORD_AUTO_FILLED = "password_auto_filled"
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
        
        // Setup password visibility toggle
        setupPasswordToggle()
        
        // Setup global focus change listener
        setupFocusChangeListener()
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
        // Get saved credentials regardless of login status
        val savedEmail = encryptedPrefs.getString(KEY_EMAIL, "")
        val savedPassword = encryptedPrefs.getString(KEY_PASSWORD, "")
        
        // Check if we have saved credentials
        val hasSavedCredentials = !savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()
        
        if (hasSavedCredentials) {
            // Auto-fill the credentials if they exist
            binding.etEmail.setText(savedEmail)
            
            // Force the password field to use password input type and transformation method
            binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPassword.setText(savedPassword)
            binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            
            // Mark that password is auto-filled
            isPasswordAutoFilled = true
            encryptedPrefs.edit().putBoolean(KEY_PASSWORD_AUTO_FILLED, true).apply()
            
            // Disable password toggle for autofilled password
            binding.tilPassword.endIconMode = TextInputLayout.END_ICON_NONE
            
            // Show login message based on login status
            if (encryptedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                Toast.makeText(this, "Welcome back! Tap login to continue", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Credentials restored. Please login", Toast.LENGTH_SHORT).show()
            }
            
            // Post a delayed check to ensure password remains masked
            binding.etPassword.post {
                if (binding.etPassword.transformationMethod !is PasswordTransformationMethod) {
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
            }
        } else {
            isPasswordAutoFilled = false
            encryptedPrefs.edit().putBoolean(KEY_PASSWORD_AUTO_FILLED, false).apply()
            
            // Enable password toggle for first login
            binding.tilPassword.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        }
    }
    
    private fun setupPasswordToggle() {
        // Get the TextInputLayout that contains the password field
        val passwordLayout = binding.tilPassword
        
        // For first login, ensure password toggle is available
        if (!isPasswordAutoFilled) {
            passwordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
        } else {
            // For autofilled passwords, hide the toggle
            passwordLayout.endIconMode = TextInputLayout.END_ICON_NONE
        }
        
        // Always ensure password is initially shown as dots
        binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
        
        // Add listener for text changes
        binding.etPassword.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // If user edits the password field manually
                if (isPasswordAutoFilled && before != count) {
                    // User is manually editing an autofilled password
                    // Enable password toggle for better user experience
                    passwordLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                    isPasswordAutoFilled = false
                }
            }
            
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        // Set up a custom end icon click listener ONLY for non-autofilled passwords
        passwordLayout.setEndIconOnClickListener {
            if (!isPasswordAutoFilled) {
                // Toggle password visibility based on current state
                if (binding.etPassword.transformationMethod is PasswordTransformationMethod) {
                    // Show password as plain text
                    binding.etPassword.transformationMethod = null
                } else {
                    // Hide password with dots
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                }
                
                // Move cursor to end to prevent visual issues
                binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
            }
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
        
        // Add listener for manual changes to password field
        binding.etPassword.setOnKeyListener { _, _, _ ->
            // If user manually edits the password, it's no longer auto-filled
            isPasswordAutoFilled = false
            encryptedPrefs.edit().putBoolean(KEY_PASSWORD_AUTO_FILLED, false).apply()
            false
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
        Log.d(TAG, "Saving credentials for email: $email")

        val editor = encryptedPrefs.edit()
        editor.clear()
        editor.commit()
        editor.apply()
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PASSWORD, password)
        editor.putString(KEY_TOKEN, token)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        
        // Make sure changes are committed immediately to avoid data loss
        val success = editor.commit()
        editor.apply()
        
        if (success) {
            Log.d(TAG, "Credentials saved successfully")
        } else {
            Log.e(TAG, "Failed to save credentials")
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

    private fun setupFocusChangeListener() {
        // Create a global focus change listener to ensure password stays masked
        val globalFocusListener = View.OnFocusChangeListener { view, hasFocus ->
            if (view.id == binding.etPassword.id && hasFocus && isPasswordAutoFilled) {
                // When password field gets focus and it's autofilled, enforce masking
                binding.etPassword.post {
                    // Force password masking
                    binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    // Disable toggle icon when autofilled
                    binding.tilPassword.endIconMode = TextInputLayout.END_ICON_NONE
                }
            }
        }
        
        // Apply the focus listener to the password field
        binding.etPassword.onFocusChangeListener = globalFocusListener
    }

    override fun onResume() {
        super.onResume()
        
        // Ensure password stays masked when auto-filled
        if (isPasswordAutoFilled) {
            // Force password masking
            binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.tilPassword.endIconMode = TextInputLayout.END_ICON_NONE
            
            // Force a redraw of the text field
            binding.etPassword.text = binding.etPassword.text
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }
    }
}