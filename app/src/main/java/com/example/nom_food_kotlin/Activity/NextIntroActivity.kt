package com.example.nom_food_kotlin.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nom_food_kotlin.Activity.Admin.MainActivityAdmin
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityNextIntroBinding

class NextIntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNextIntroBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityNextIntroBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_Nom_food_kotlin)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("loginSave", MODE_PRIVATE)
        val isLogin = sharedPreferences.getBoolean("isLogin", false)
        if (isLogin) {
            val userId = sharedPreferences.getString("userId", null)
            val role = sharedPreferences.getString("role", null)
            val intent = if (role == "customer") {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, MainActivityAdmin::class.java)
            }.apply {
                putExtra("userId", userId)
            }
            startActivity(intent)
            finish()
        } else {
            binding.btnNext.setOnClickListener {
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                binding.btnNext.isEnabled = false
            }
        }
    }


}
