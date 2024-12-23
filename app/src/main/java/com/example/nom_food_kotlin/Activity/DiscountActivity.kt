package com.example.nom_food_kotlin.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Adapter.DiscountAdapter
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Discount
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityDiscountBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DiscountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDiscountBinding
    private var listDiscount: MutableList<Discount> = mutableListOf()
    private lateinit var adapterDiscount: DiscountAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences3: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDiscountBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("saveDiscount", MODE_PRIVATE)
        sharedPreferences3 = this.getSharedPreferences("savePayment", MODE_PRIVATE)

        firebaseHelper = FirebaseHelper()

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        lifecycleScope.launch {
            loadData()
        }

        binding.btnUse.visibility = View.GONE
        binding.btnNoUse.visibility = View.VISIBLE

        setRecycleView()

        adapterDiscount.onClickItem = { discount, i ->
            val intent = Intent(this, DiscountDetailActivity::class.java).apply {
                putExtra("title", discount.title)
                putExtra("content", discount.content)
                putExtra("start", discount.start)
                putExtra("end", discount.end)
                putExtra("payment", discount.payment)
                putExtra("condition", discount.condition)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        adapterDiscount.onSelectionChanged = {
            val isAnySelected = listDiscount.any { it.isSelected }
            if (isAnySelected) {
                binding.btnUse.visibility = View.VISIBLE
                binding.btnNoUse.visibility = View.GONE
            } else {
                binding.btnUse.visibility = View.GONE
                binding.btnNoUse.visibility = View.VISIBLE
            }
        }


        binding.btnUse.setOnClickListener {
            val selectedDiscounts = listDiscount.filter { it.isSelected }
            if (selectedDiscounts.size > 1) {
                Toast.makeText(this, "You can only select one discount", Toast.LENGTH_SHORT).show()
            } else if (selectedDiscounts.size == 1) {
                val selectedDiscount = selectedDiscounts[0]
                val editor = sharedPreferences.edit()
                editor.putString("discountTitle", selectedDiscount.title)
                selectedDiscount.valueOfOrder?.toString()?.let { editor.putString("valueOfOrder", it) }
                editor.putString("type", selectedDiscount.type)
                selectedDiscount.value?.toString()?.let { editor.putString("value", it) }
                editor.putString("payment", selectedDiscount.payment)
                editor.apply()

                val intent = Intent("UPDATE_ORDER_ACTIVITY")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }

        binding.btnNoUse.setOnClickListener {
            sharedPreferences.edit().clear().apply()

            val intent = Intent("UPDATE_ORDER_ACTIVITY")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    suspend fun loadData() {
        val data = withContext(Dispatchers.IO) {
            firebaseHelper.getAllDiscount()
        }

        Log.d("checkDiscount", data.toString())

        listDiscount.clear()
        listDiscount.addAll(data)

        withContext(Dispatchers.Main) {
            adapterDiscount.notifyDataSetChanged()
        }
    }

    private fun setRecycleView() {
        adapterDiscount = DiscountAdapter(listDiscount)
        binding.rcvDiscount.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcvDiscount.adapter = adapterDiscount
    }
}