package com.vatty.skitter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.skillexchange.MapActivity
import com.example.skillexchange.RegisterActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is already logged in
        if (isLoggedIn()) {
            // If logged in, go to map activity
            startActivity(Intent(this, MapActivity::class.java))
            // Don't finish MainActivity so user can come back
            return
        }

        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun isLoggedIn(): Boolean {
        return getSharedPreferences("login", MODE_PRIVATE)
            .getBoolean("isLoggedIn", false)
    }

    // Override back button to exit app from MainActivity
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