package com.example.nom_food_kotlin.Helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _orderQuantity = MutableLiveData<Int>()
    val orderQuantity: LiveData<Int> get() = _orderQuantity

    fun setOrderQuantity(quantity: Int) {
        _orderQuantity.value = quantity
    }
}