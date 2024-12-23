package com.example.nom_food_kotlin.Model

data class Discount(
    val title: String? = null,
    val type: String? = null,
    val condition: String? = null,
    val content: String? = null,
    var description: String? = null,
    var value: Double? = null,
    val start: String? = null,
    var end: String? = null,
    var status: Boolean? = null,
    val payment: String? = null,
    val valueOfOrder: Double? = null,
    var isSelected: Boolean = false
)
