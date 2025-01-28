package com.example.skillexchange.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val email: String,
    val phone: String,
    val skillsOffered: List<String>,
    val skillsWanted: List<String>,
    val password: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) 