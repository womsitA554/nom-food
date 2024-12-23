package com.example.nom_food_kotlin.Activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding

    companion object {
        private const val INTRO_DISPLAY_TIME = 1000
    }

    private var hasCheckedNetwork = false

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Handler().postDelayed({
            if (!hasCheckedNetwork) {
                checkNetworkAndProceed()
            }
        }, INTRO_DISPLAY_TIME.toLong())
    }

    private fun checkNetworkAndProceed() {
        hasCheckedNetwork = true
        if (!isNetworkAvailable()) {
            startErrorActivity()
        } else {
            startNextActivity()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun startErrorActivity() {
        val intent = Intent(this, ErrorActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun startNextActivity() {
        val intent = Intent(this, NextIntroActivity::class.java)
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}
