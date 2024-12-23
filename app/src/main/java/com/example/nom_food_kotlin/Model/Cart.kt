package com.example.nom_food_kotlin.Model

import java.io.Serializable

data class Cart(
    val itemId: String? = null,
    val picUrl:String ? = null,
    val title: String? = null,
    var price: Double? = null,
    var quantity: Int? = null
) : Serializable
