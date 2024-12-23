package com.example.nom_food_kotlin.Activity

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.nom_food_kotlin.Adapter.OrderAdapter
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Bill
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityOrderAgainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderAgainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderAgainBinding
    private var listOrder: MutableList<Cart> = mutableListOf()
    private lateinit var adapter: OrderAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var sharedPreferences2: SharedPreferences
    private lateinit var sharedPreferences3: SharedPreferences
    private lateinit var cartManager: CartManager
    private lateinit var firebaseHelper: FirebaseHelper
    private var userId: String = ""
    private var phoneNumber: String = ""
    private var totalPriceOfCart: Double = 0.0
    private var paymentImage: Int = 0
    private var discount: Double = 0.0
    private var totalPriceToPay: Double = 0.0
    private var delivery = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOrderAgainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Nom_food_kotlin)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper()
        cartManager = CartManager(this)
        sharedPreferences = this.getSharedPreferences("loginSave", MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "").toString()
        sharedPreferences2 = this.getSharedPreferences("saveDiscount", MODE_PRIVATE)
        sharedPreferences3 = this.getSharedPreferences("savePayment", MODE_PRIVATE)

        val valueOfOrder = sharedPreferences2.getString("valueOfOrder", "0.0")!!.toDouble()
        val type = sharedPreferences2.getString("type", "")
        val paymentFormDiscount = sharedPreferences2.getString("payment", "")
        val payment = sharedPreferences3.getString("payment", "")

        setRecycleView()

        lifecycleScope.launch {
            loadData()
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, IntentFilter("UPDATE_ORDER_ACTIVITY"))

        val savedTitle = sharedPreferences2.getString("discountTitle", "")
        binding.tvGetDiscount.text = savedTitle

        val savedPaymentImageUrl = sharedPreferences3.getString("picUrl", null)
        if (savedPaymentImageUrl != null) {
            Glide.with(this)
                .load(savedPaymentImageUrl)
                .into(binding.imgPayment)
        }

        binding.btnChangeAddress.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("navigateTo", "ProfileFragment")
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnDiscount.setOnClickListener {
            val intent = Intent(this, DiscountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnPayment.setOnClickListener {
            val intent = Intent(this, PaymentActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        priceCalculation()

        binding.btnNext.setOnClickListener {
            lifecycleScope.launch {
                if (checkItemsExistence()) {
                    proceedWithOrder()
                }
            }
        }
    }

    private suspend fun checkItemsExistence(): Boolean {
        return withContext(Dispatchers.IO) {
            val allItems = firebaseHelper.getAllItems() // Assume this function fetches all items from the database
            val missingItems = listOrder.filter { menu ->
                allItems.none { it.itemId == menu.itemId }
            }
            Log.d("missingItems", missingItems.toString())

            if (missingItems.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    showMissingItemsDialog(missingItems)
                }
                false
            } else {
                true
            }
        }
    }

    private fun showMissingItemsDialog(missingItems: List<Cart>) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_orderagain, null)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun proceedWithOrder() {
        // Existing code for proceeding with the order...
        val valueOfOrder = sharedPreferences2.getString("valueOfOrder", "0.0")!!.toDouble()
        val type = sharedPreferences2.getString("type", "")
        val paymentFormDiscount = sharedPreferences2.getString("payment", "")
        val payment = sharedPreferences3.getString("payment", "")

        if (type == "percentage" && totalPriceOfCart < valueOfOrder) {
            MotionToast.createColorToast(
                this,
                null,
                "You need to buy more than $valueOfOrder$ to use this discount",
                MotionToastStyle.ERROR,
                Gravity.TOP,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
            )
        } else if (type == "fixed_amount" && totalPriceOfCart < valueOfOrder) {
            MotionToast.createColorToast(
                this,
                null,
                "You need to buy more than $valueOfOrder$ to use this discount",
                MotionToastStyle.ERROR,
                Gravity.TOP,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
            )
        } else if (type == "free_ship" && totalPriceOfCart < valueOfOrder) {
            MotionToast.createColorToast(
                this,
                null,
                "You need to buy more than $valueOfOrder$ to use this discount",
                MotionToastStyle.ERROR,
                Gravity.TOP,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
            )
        } else if (type == "momo_discount" && paymentFormDiscount != payment) {
            MotionToast.createColorToast(
                this,
                null,
                "You need to check your payment method again",
                MotionToastStyle.ERROR,
                Gravity.TOP,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
            )
        } else {
            if (userId != null) {
                firebaseHelper.getPhoneNumberAndEmail(userId).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val dataSnapshot = task.result
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            phoneNumber =
                                dataSnapshot.child("phoneNumber").getValue(String::class.java)
                                    .toString()
                        } else {
                            Log.d("UserInfo", "No data available")
                        }
                    } else {
                        Log.e("UserInfo", "Error getting data", task.exception)
                    }
                }
            }

            val billId = generateBillId()
            val address = binding.tvAddress.text.toString()
            val currentDate = getCurrentFormattedDate()
            val note = binding.etNote.editableText.toString()
            val bill = payment?.let { it1 ->
                Bill(
                    billId,
                    userId,
                    phoneNumber,
                    address,
                    cartManager.getCart(userId),
                    delivery,
                    discount,
                    it1,
                    totalPriceOfCart,
                    totalPriceToPay,
                    currentDate,
                    note,
                    "Pending",
                    ""
                )
            }

            firebaseHelper.addBill(bill!!)
            val intent1 = Intent("UPDATE_CART_QUANTITY")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent1)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            MotionToast.createColorToast(
                this,
                null,
                "Order successfully",
                MotionToastStyle.SUCCESS,
                Gravity.TOP,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
            )
            val intent = Intent(this, CompleteActivity::class.java)
            startActivity(intent)
            finish()

            cartManager.clearCart(userId)
        }
    }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Update the UI or recalculate prices
            val savedTitle = sharedPreferences2.getString("discountTitle", "")
            binding.tvGetDiscount.text = savedTitle

            val savedPaymentImageUrl = sharedPreferences3.getString("picUrl", null)
            if (savedPaymentImageUrl != null) {
                Glide.with(this@OrderAgainActivity)
                    .load(savedPaymentImageUrl)
                    .into(binding.imgPayment)
            }

            priceCalculation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver)
    }

    fun getCurrentFormattedDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    fun generateBillId(): String {
        val uniqueNumber = System.currentTimeMillis().toString().takeLast(9)
        return "HD$uniqueNumber"
    }

    suspend fun loadData() {
        withContext(Dispatchers.IO) {
            val list = intent.getSerializableExtra("items") as? ArrayList<Cart>
            listOrder.clear()
            if (list != null) {
                listOrder.addAll(list)
            }
        }

        Log.d("listOrder", listOrder.toString())

        withContext(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun priceCalculation() {
        if (listOrder.isNotEmpty()) {
            totalPriceOfCart = listOrder.sumOf { it.price?.times(it.quantity!!) ?: 0.0 }
            Log.d("totalPriceOfCart", "not empty")
        } else {
            totalPriceOfCart = 0.0
            Log.d("totalPriceOfCart", "empty")
        }
        Log.d("totalPriceOfCart", totalPriceOfCart.toString())
        binding.tvTotalCart.text = "$ " + totalPriceOfCart.toString()

        val type = sharedPreferences2.getString("type", "")
        discount = when (type) {
            "percentage", "momo_discount" -> {
                val value = sharedPreferences2.getString("value", "")!!.toDouble()
                totalPriceOfCart * value / 100
            }

            "fixed_amount" -> {
                sharedPreferences2.getString("value", "")!!.toDouble()
            }

            "free_ship" -> {
                binding.tvDelivery.text = "$ 0.0"
                delivery = 0.0
                0.0
            }

            else -> 0.0
        }

        binding.tvDiscount.text = "$ " + discount.toString()

        totalPriceToPay = totalPriceOfCart - discount + delivery
        binding.tvTotal.text = "$ " + totalPriceToPay.toString()
    }

    private fun setRecycleView() {
        adapter = OrderAdapter(listOrder)
        binding.rcvOrder.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcvOrder.adapter = adapter
    }
}