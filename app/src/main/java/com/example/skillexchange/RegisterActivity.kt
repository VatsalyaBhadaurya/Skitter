package com.example.skillexchange

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.vatty.skitter.LoginActivity
import com.vatty.skitter.R
import com.example.skillexchange.data.UserEntity
import com.example.skillexchange.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var skillsOfferedEditText: EditText
    private lateinit var skillsWantedEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var loginLinkText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (isLoggedIn()) {
            // If logged in, go directly to map activity
            startActivity(Intent(this, MapActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_register)

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        skillsOfferedEditText = findViewById(R.id.skillsOfferedEditText)
        skillsWantedEditText = findViewById(R.id.skillsWantedEditText)
        registerButton = findViewById(R.id.registerButton)
        loginLinkText = findViewById(R.id.loginLinkText)

        registerButton.setOnClickListener {
            registerUser()
        }

        loginLinkText.setOnClickListener {
            // Navigate to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser() {
        val name = nameEditText.text.toString()
        val email = emailEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val password = passwordEditText.text.toString()
        val skillsOffered = skillsOfferedEditText.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val skillsWanted = skillsWantedEditText.text.toString()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        if (validateInput(name, email, phone, password, skillsOffered, skillsWanted)) {
            val db = AppDatabase.getDatabase(this)
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Create user object
                    val user = UserEntity(
                        name = name,
                        email = email,
                        phone = phone,
                        password = password,
                        skillsOffered = skillsOffered,
                        skillsWanted = skillsWanted
                    )

                    // Insert user into database
                    db.userDao().insert(user)

                    // Save login state
                    withContext(Dispatchers.Main) {
                        saveLoginState(email)
                        // Navigate to MapActivity after successful registration
                        startActivity(Intent(this@RegisterActivity, MapActivity::class.java))
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration failed: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun validateInput(
        name: String,
        email: String,
        phone: String,
        password: String,
        skillsOffered: List<String>,
        skillsWanted: List<String>
    ): Boolean {
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        if (skillsOffered.isEmpty() && skillsWanted.isEmpty()) {
            Toast.makeText(this, "Please enter at least one skill to offer or learn", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveLoginState(email: String) {
        getSharedPreferences("login", MODE_PRIVATE)
            .edit()
            .putBoolean("isLoggedIn", true)
            .putString("userEmail", email)
            .apply()
    }

    private fun isLoggedIn(): Boolean {
        return getSharedPreferences("login", MODE_PRIVATE)
            .getBoolean("isLoggedIn", false)
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            // If this is the last activity, show exit confirmation
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes") { _, _ ->
                    finishAffinity()
                }
                .setNegativeButton("No", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
} 