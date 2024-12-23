package com.example.nom_food_kotlin.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityDiscountDetailBinding

class DiscountDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiscountDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDiscountDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        val start = intent.getStringExtra("start")
        val end = intent.getStringExtra("end")
        val payment = intent.getStringExtra("payment")
        val condition = intent.getStringExtra("condition")

        val date = start + " to " + end

        binding.tvTitle.text = title
        binding.tvContent.text = content
        binding.tvDate.text = date
        binding.tvPayment.text = payment
        binding.tvCondition.text = condition

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }
}