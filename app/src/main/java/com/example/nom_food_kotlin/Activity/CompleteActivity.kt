package com.example.nom_food_kotlin.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityCompleteBinding

class CompleteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompleteBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cartManager: CartManager
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCompleteBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("loginSave", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "").toString()
        cartManager = CartManager(this)

        binding.lottieAnimationView.playAnimation()

        binding.btnNext.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("navigateTo", "MenuFragment")
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}