package com.example.nom_food_kotlin.Activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.User
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivitySetupnameBinding

class SetUpNameActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupnameBinding
    private lateinit var firebaseHelper: FirebaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySetupnameBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val phoneNumber = intent.getStringExtra("phoneNumber")
        firebaseHelper = FirebaseHelper()

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnNext.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val repeatPassword = binding.etRepeatPassword.text.toString().trim()
            if (name.isNotEmpty() && password.isNotEmpty() && repeatPassword.isNotEmpty() && password == repeatPassword) {
                val userId = firebaseHelper.currentUserId()
                val user = User(userId, name, phoneNumber, email = "", password, role = "customer")
                firebaseHelper.getAllUserId { userIds ->
                    if (userId != null && !userIds.contains(userId)) {
                        firebaseHelper.addUser(user)
                    }else{
                        Toast.makeText(this, "This phone number is already in use", Toast.LENGTH_SHORT).show()
                    }
                    startActivity(Intent(this, SignUpActivity::class.java))
                    finish()
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                }
            } else if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
            } else if (password != repeatPassword || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Check password", Toast.LENGTH_SHORT).show()
            }
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }
}