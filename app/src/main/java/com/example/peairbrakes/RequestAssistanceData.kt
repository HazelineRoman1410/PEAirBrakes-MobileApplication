package com.example.peairbrakes

data class RequestAssistanceData(
    var userEmail: String? = "",
    var registration: String? = "",
    var problem: String? = "",
    var photo: String? = "",
    var location: String? = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var status: String? = "Pending"
)