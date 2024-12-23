package com.example.nom_food_kotlin.service.stripe

import com.example.nom_food_kotlin.models.CustomerModel
import com.example.nom_food_kotlin.models.EphemeralKeyModel
import com.example.nom_food_kotlin.models.PaymentIntentModel

import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiInterface {
    @Headers("Authorization: Bearer ${Utils.SECRET_KEY}")
    @POST("v1/customers")
    fun getCustomer(): Call<CustomerModel>

    @Headers("Authorization: Bearer ${Utils.SECRET_KEY}", "stripe-version: 2024-06-20")
    @POST("v1/ephemeral_keys")
    fun getEphemeralKey(@Query("customer") customer: String, ): Call<EphemeralKeyModel>

    @Headers("Authorization: Bearer ${Utils.SECRET_KEY}")
    @POST("v1/payment_intents")
    fun getPaymentIntents(
        @Query("customer") customer: String,
        @Query("amount") amount: String,
        @Query("currency") currency: String = "usd",
        @Query("automatic_payment_methods[enabled]") automatePay: Boolean = true,
    ): Call<PaymentIntentModel>

}