package com.example.peairbrakes

data class JobCardData(
    var reqId: Int = -1,
    var kmTraveled: String? = "",
    var registration: String? = "",
    var make: String? = "",
    var odometer: String? = "",
    var problem: String? = "",
    var parts: String? = "",
    var photo: String? = "",
    var refusedPhoto: String? = "",
    var refusedRepairs: String? = "",
    var signature: String? = "",
    var startTime: String? = "",
    var endTime: String? = "",
    var status: String? = ""
)