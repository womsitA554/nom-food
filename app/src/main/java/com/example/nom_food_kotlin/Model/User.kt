package com.example.nom_food_kotlin.Model

data class User(
    var userId: String? = null,
    var name: String? = null,
    var phoneNumber: String? = null,
    var email: String? = null,
    var password: String? = null,
    var role: String? = null,
    val fcmToken: String? = null
)
