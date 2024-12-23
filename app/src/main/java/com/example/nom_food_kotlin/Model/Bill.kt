package com.example.nom_food_kotlin.Model

data class Bill(
    val billId: String? = null,
    val userId: String? = null,
    val phoneNumber: String? = null,
    val address: String? = null,
    val items: List<Cart>? = null,
    val delivery: Double? = null,
    val discount: Double? = null,
    val payment: String? = null,
    val totalCart: Double? = null,
    val total: Double? = null,
    val date: String? = null,
    val note: String? = null,
    var status: String? = null,
    var reason: String? = null
)
