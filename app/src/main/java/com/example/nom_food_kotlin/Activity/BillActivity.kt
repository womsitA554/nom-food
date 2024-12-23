package com.example.nom_food_kotlin.Activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Adapter.OrderAdapter
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityBillBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBillBinding
    private lateinit var orderAdapter: OrderAdapter
    private var listOrder: MutableList<Cart> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityBillBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val billId = intent.getStringExtra("billId")
        val date = intent.getStringExtra("date")
        val address = intent.getStringExtra("address")
        val payment = intent.getStringExtra("payment")
        val delivery = intent.getDoubleExtra("delivery", 0.0)
        val discount = intent.getDoubleExtra("discount", 0.0)
        val totalCart = intent.getDoubleExtra("totalCart", 0.0)
        val total = intent.getDoubleExtra("total", 0.0)
        val note = intent.getStringExtra("note")
        val status = intent.getStringExtra("status")
        val reason = intent.getStringExtra("reason")

        binding.lnReason.visibility = View.GONE

        binding.tvBillId.text = billId
        binding.tvDate.text = date
        binding.tvAddress.text = address
        binding.tvPaymentMethod.text = payment
        binding.tvDelivery.text = "$ " + delivery.toString()
        binding.tvDiscount.text = "$ " + discount.toString()
        binding.tvTotalCart.text = "$ " + totalCart.toString()
        binding.tvTotal.text = "$ " + total.toString()
        if (note.isNullOrEmpty()) {
            binding.tvNote.text = "No note"
        } else {
            binding.tvNote.text = note
        }

        when (status) {
            "Pending" -> {
                binding.tvStatus.text = status
                binding.tvStatus.setTextColor(Color.parseColor("#BC7001"))
                binding.tvStatus.setBackgroundResource(R.drawable.status_pending_bg)
            }
            "Success" -> {
                binding.tvStatus.text = status
                binding.tvStatus.setTextColor(Color.parseColor("#07650B"))
                binding.tvStatus.setBackgroundResource(R.drawable.status_success_bg)
            }
            "Cancel" -> {
                binding.tvStatus.text = status
                binding.tvStatus.setTextColor(Color.parseColor("#8F0101"))
                binding.tvStatus.setBackgroundResource(R.drawable.status_cancel_bg)
            }
        }

        if (!reason.isNullOrEmpty()) {
            binding.lnReason.visibility = View.VISIBLE
            binding.tvReason.text = reason
        }

        setRecycleView()

        lifecycleScope.launch {
            loadData()
        }

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private suspend fun loadData() {
        val items = withContext(Dispatchers.IO) {
            intent.getSerializableExtra("items") as? ArrayList<Cart>
        }

        items?.let {
            listOrder.clear()
            listOrder.addAll(it)
        }

        withContext(Dispatchers.Main) {
            orderAdapter.notifyDataSetChanged()
        }
    }

    private fun setRecycleView() {
        binding.rcvBill.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrderAdapter(listOrder)
        binding.rcvBill.adapter = orderAdapter
    }
}