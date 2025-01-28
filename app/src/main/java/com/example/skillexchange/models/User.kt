package com.example.skillexchange.models

data class User(
    val email: String,
    val phone: String,
    val skillsOffered: List<String>,
    val skillsWanted: List<String>,
    val availability: String,
    val location: Location
)

data class Location(
    val latitude: Double,
    val longitude: Double
) 