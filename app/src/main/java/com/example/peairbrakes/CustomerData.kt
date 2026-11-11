package com.example.peairbrakes

data class CustomerData (
    var name: String? = "",
    var email: String? = "",
    var phone: String? = "",
    var companyName: String? = "",
    var companyAddress: String? = "",
    var password: String? = "",
    var role: String? = "Customer"
)