package com.example.skillexchange

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.vatty.skitter.R
import com.example.skillexchange.data.AppDatabase
import com.example.skillexchange.data.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MatchingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)
        
        // Load nearby users with matching skills
        loadMatches()
    }

    private fun loadMatches() {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            // Get current user's preferences
            val userEmail = getSharedPreferences("login", MODE_PRIVATE)
                .getString("userEmail", "") ?: ""
            
            // Get user's location
            val latitude = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("user_latitude", "0.0")?.toDoubleOrNull() ?: 0.0
            val longitude = getSharedPreferences("user_prefs", MODE_PRIVATE)
                .getString("user_longitude", "0.0")?.toDoubleOrNull() ?: 0.0

            // Use a larger bounding box for initial filtering (about 10km)
            val radiusInDegrees = 0.1 // Approximately 11km at the equator
            
            try {
                val nearbyUsers = db.userDao().getNearbyUsers(
                    latitude = latitude,
                    longitude = longitude,
                    radius = radiusInDegrees,
                    currentUserEmail = userEmail
                )

                // Filter users by actual distance and sort them
                val filteredUsers = nearbyUsers.map { user ->
                    val distance = calculateDistance(
                        latitude, longitude,
                        user.latitude, user.longitude
                    )
                    Pair(user, distance)
                }.filter { (_, distance) ->
                    distance <= 10.0 // 10 km radius
                }.sortedBy { (_, distance) ->
                    distance
                }

                withContext(Dispatchers.Main) {
                    // Update UI with filtered users
                    updateUI(filteredUsers)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MatchingActivity,
                        "Error loading matches",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Helper function to calculate distance between two points using Haversine formula
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371.0 // Earth's radius in kilometers

        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val latDiff = Math.toRadians(lat2 - lat1)
        val lonDiff = Math.toRadians(lon2 - lon1)

        val a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2)
        
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        
        return R * c // Distance in kilometers
    }

    private fun updateUI(users: List<Pair<UserEntity, Double>>) {
        if (users.isEmpty()) {
            // Show empty state
            findViewById<RecyclerView>(R.id.matchesRecyclerView).visibility = View.GONE
            findViewById<LinearLayout>(R.id.emptyState).visibility = View.VISIBLE
        } else {
            // Show matches in RecyclerView
            findViewById<RecyclerView>(R.id.matchesRecyclerView).visibility = View.VISIBLE
            findViewById<LinearLayout>(R.id.emptyState).visibility = View.GONE
            // Update your RecyclerView adapter here
        }
    }
} 