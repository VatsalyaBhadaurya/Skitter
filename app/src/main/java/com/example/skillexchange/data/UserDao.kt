package com.example.skillexchange.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): List<UserEntity>

    @Query("UPDATE users SET latitude = :latitude, longitude = :longitude WHERE email = :email")
    suspend fun updateUserLocation(email: String, latitude: Double, longitude: Double)

    // Modified query to find nearby users within a bounding box
    @Query("""
        SELECT * FROM users 
        WHERE email != :currentUserEmail 
        AND latitude BETWEEN (:latitude - :radius) AND (:latitude + :radius)
        AND longitude BETWEEN (:longitude - :radius) AND (:longitude + :radius)
    """)
    suspend fun getNearbyUsers(
        latitude: Double,
        longitude: Double,
        radius: Double,
        currentUserEmail: String
    ): List<UserEntity>
} 