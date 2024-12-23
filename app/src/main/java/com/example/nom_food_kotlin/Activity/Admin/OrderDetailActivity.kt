package com.example.nom_food_kotlin.Activity.Admin

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Adapter.OrderAdapter
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityOrderDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class OrderDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var orderAdapter: OrderAdapter
    private var listOrder: MutableList<Cart> = mutableListOf()
    private lateinit var firebaseHelper: FirebaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper()

        // Retrieve the Intent extras
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

        // Bind the data to the views
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

        setRecycleView()

        // Load data asynchronously
        lifecycleScope.launch {
            loadData()
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivityAdmin::class.java)
            intent.putExtra("navigateToo", "FirstFragment")
            finish()
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnAccept.setOnClickListener {
            firebaseHelper.updateStatus(billId.toString(), "Success")
            val intent = Intent(this, MainActivityAdmin::class.java)
            intent.putExtra("navigateToo", "FirstFragment")
            finish()
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            MotionToast.createColorToast(
                this,
                null,
                "Order accepted",
                MotionToastStyle.SUCCESS,
                Gravity.TOP,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
            )
        }
        binding.btnCancel.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_cancel_order, null)
            val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rgReasons)
            val etDifferentReason = dialogView.findViewById<EditText>(R.id.etDifferentReason)
            val btnSubmitReason = dialogView.findViewById<Button>(R.id.btnSubmitReason)
            val btnCancelReason = dialogView.findViewById<Button>(R.id.btnCancelReason)

            etDifferentReason.visibility = View.GONE

            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId == R.id.rbOther) {
                    etDifferentReason.visibility = View.VISIBLE
                } else {
                    etDifferentReason.visibility = View.GONE
                }
            }

            val alertDialog = AlertDialog.Builder(this)
                .setTitle("Reason for order cancellation")
                .setView(dialogView)
                .create()

            btnSubmitReason.setOnClickListener {
                val selectedReasonId = radioGroup.checkedRadioButtonId

                if (selectedReasonId == -1) {
                    MotionToast.createColorToast(
                        this,
                        null,
                        "Please select a reason",
                        MotionToastStyle.WARNING,
                        Gravity.TOP,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                    )
                    return@setOnClickListener
                }

                val selectedReason = if (selectedReasonId == R.id.rbOther) {
                    val otherReason = etDifferentReason.text.toString()
                    if (otherReason.isBlank()) {
                        MotionToast.createColorToast(
                            this,
                            null,
                            "Please provide a reason for 'Other'",
                            MotionToastStyle.WARNING,
                            Gravity.TOP,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                        )
                        return@setOnClickListener
                    } else {
                        otherReason
                    }
                } else {
                    dialogView.findViewById<RadioButton>(selectedReasonId).text.toString()
                }

                firebaseHelper.updateStatus(billId.toString(), "Cancel")
                if (billId != null) {
                    firebaseHelper.updateReason(billId, "Sorry, your order has been cancelled because " + selectedReason)
                }
                val intent = Intent(this, MainActivityAdmin::class.java)
                intent.putExtra("navigateToo", "FirstFragment")
                finish()
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                MotionToast.createColorToast(
                    this,
                    null,
                    "Order Cancellation",
                    MotionToastStyle.ERROR,
                    Gravity.TOP,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )

                alertDialog.dismiss()
            }

            btnCancelReason.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialog.show()
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