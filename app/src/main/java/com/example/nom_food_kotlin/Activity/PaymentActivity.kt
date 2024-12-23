package com.example.nom_food_kotlin.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Adapter.PaymentAdapter
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Payment
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityPaymentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PaymentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPaymentBinding
    private var selectedPaymentMethod: Int? = null
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var adapter: PaymentAdapter
    private val list: MutableList<Payment> = mutableListOf()
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper()

        sharedPreferences = this.getSharedPreferences("savePayment", MODE_PRIVATE)

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        lifecycleScope.launch {
            loadData()
        }

        setRecycleView()

        adapter.onClickItem = { payment, i ->
            val editor = sharedPreferences.edit()
            editor.putString("picUrl", payment.picUrl)
            editor.putString("title", payment.title)
            editor.putString("payment", payment.payment)
            editor.apply()
            val intent = Intent("UPDATE_ORDER_ACTIVITY")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    suspend fun loadData() {
        val data = withContext(Dispatchers.IO) {
            firebaseHelper.getAllPayment()
        }
        Log.d("checkPayment", data.toString())
        list.clear()
        list.addAll(data)

        withContext(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun setRecycleView() {
        binding.rcvPayment.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = PaymentAdapter(list)
        binding.rcvPayment.adapter = adapter
    }
}