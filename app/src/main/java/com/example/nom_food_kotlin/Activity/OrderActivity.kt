package com.example.nom_food_kotlin.Activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Toast
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
import com.example.nom_food_kotlin.databinding.ActivityOrderBinding
import com.example.nom_food_kotlin.service.stripe.ApiUtilities
import com.example.nom_food_kotlin.service.stripe.Utils
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.properties.Delegates

class OrderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderBinding
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

    private lateinit var paymentSheet: PaymentSheet
    lateinit var customerId: String
    lateinit var ephemeralKey: String
    lateinit var clientSecretKey: String
    private var apiInterface = ApiUtilities.getApiInterface()

    private lateinit var savedTitle: String
    private lateinit var type: String
    private lateinit var payment: String
    private var valueOfOrder : Double = 0.0
    private lateinit var paymentFormDiscount: String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOrderBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Nom_food_kotlin)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper()
        cartManager = CartManager(this)

        PaymentConfiguration.init(this, Utils.PUBLISHABLE_KEY)

        sharedPreferences = this.getSharedPreferences("loginSave", MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "").toString()

        sharedPreferences2 = this.getSharedPreferences("saveDiscount", MODE_PRIVATE)

        savedTitle = sharedPreferences2.getString("discountTitle", "").toString()
        binding.tvGetDiscount.text = savedTitle

        sharedPreferences3 = this.getSharedPreferences("savePayment", MODE_PRIVATE)

        valueOfOrder = sharedPreferences2.getString("valueOfOrder", "0.0")!!.toDouble()
        type = sharedPreferences2.getString("type", "").toString()
        paymentFormDiscount = sharedPreferences2.getString("payment", "").toString()
        payment = sharedPreferences3.getString("payment", "").toString()

        binding.btnChangeAddress.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            finish()
        }

        binding.tvDelivery.text = "$ 0.0"

        val savedPaymentImageUrl = sharedPreferences3.getString("picUrl", null)

        if (savedPaymentImageUrl != null) {
            Glide.with(this)
                .load(savedPaymentImageUrl)
                .into(binding.imgPayment)
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("navigateTo", "CartFragment")
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        setRecycleView()

        lifecycleScope.launch {
            loadData()
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

        getCustomerId()

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

        binding.btnNext.setOnClickListener {
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
            } else if (type == "visa_discount" && paymentFormDiscount != payment) {
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
                if (payment == "Payment via cash") {
                    createBill()
                } else {
                    if (::clientSecretKey.isInitialized) {
                        paymentFlow()
                    } else {
                        Log.e("PaymentFlow", "Client secret key is not initialized")
                    }
                }
            }
        }
    }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Update the UI or recalculate prices
            val savedTitle = sharedPreferences2.getString("discountTitle", "")
            binding.tvGetDiscount.text = savedTitle

            val savedPaymentImageUrl = sharedPreferences3.getString("picUrl", null)

            if (savedPaymentImageUrl != null) {
                Glide.with(this@OrderActivity)
                    .load(savedPaymentImageUrl)
                    .into(binding.imgPayment)
            }

            priceCalculation()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Unregister BroadcastReceiver
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

    private fun priceCalculation() {
        totalPriceOfCart = cartManager.totalPriceOfCart(userId)
        binding.tvTotalCart.text = "$ " + totalPriceOfCart.toString()

        val type = sharedPreferences2.getString("type", "")
        discount = when (type) {
            "percentage", "visa_discount" -> {
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
        Log.d("PriceCalculation", "Total price: $totalPriceToPay")
    }

    private fun createBill(){
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

        val billId = generateBillId()
        val address = binding.tvAddress.text.toString()
        val currentDate = getCurrentFormattedDate()
        val note = binding.etNote.editableText.toString()
        val payment = sharedPreferences3.getString("payment", "").toString()
        val bill = Bill(
            billId,
            userId,
            phoneNumber,
            address,
            cartManager.getCart(userId),
            delivery,
            discount,
            payment,
            totalPriceOfCart,
            totalPriceToPay,
            currentDate,
            note,
            "Pending",
            ""
        )

        firebaseHelper.addBill(bill!!)
        firebaseHelper.sendNewOrderNotification(billId)

        val intent1 = Intent("UPDATE_CART_QUANTITY")
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent1)

        // Hiển thị thông báo thành công
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

    suspend fun loadData() {
        withContext(Dispatchers.IO) {
            val list = cartManager.getCart(userId)
            listOrder.clear()
            listOrder.addAll(list)
        }

        withContext(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun setRecycleView() {
        adapter = OrderAdapter(listOrder)
        binding.rcvOrder.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rcvOrder.adapter = adapter
    }

    private fun paymentFlow() {
        if (::clientSecretKey.isInitialized) {
            paymentSheet.presentWithPaymentIntent(
                clientSecretKey,
                PaymentSheet.Configuration(
                    "Example, Inc.",
                    PaymentSheet.CustomerConfiguration(
                        customerId,
                        ephemeralKey
                    )
                )
            )
        } else {
            Log.e("PaymentFlow", "Client secret key is not initialized") }
    }

    private fun getCustomerId() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = apiInterface.getCustomer().execute()
                withContext(Dispatchers.Main) {
                    customerId = res.body()?.id ?: run { return@withContext
                    }
                    getEphemeralKey(customerId)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {}
            }
        }
    }

    private fun getEphemeralKey(customerId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = apiInterface.getEphemeralKey(customerId).execute()
                withContext(Dispatchers.Main) {
                    ephemeralKey = res.body()?.id ?: run { return@withContext
                    }
                    getPaymentIntent(customerId, ephemeralKey)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {}
            }
        }
    }

    private fun getPaymentIntent(customerId: String, ephemeralKey: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = apiInterface.getPaymentIntents(customerId, (totalPriceToPay*100).toInt().toString()).execute()
                withContext(Dispatchers.Main) {
                    clientSecretKey = res.body()?.client_secret ?: run { return@withContext
                    }
                    Log.d("PaymentIntent", "Client Secret: $clientSecretKey") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("PaymentIntent", "Error: ${e.message}")
                }
            }
        }
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                createBill()
                Log.d("PaymentSheetResult", "Payment completed")
            }
            is PaymentSheetResult.Failed -> {
                Log.e("PaymentSheetResult", "Payment failed: ${paymentSheetResult.error.message}") }
            is PaymentSheetResult.Canceled -> {
                Log.d("PaymentSheetResult", "Payment canceled") }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(updateReceiver, IntentFilter("UPDATE_ORDER_ACTIVITY"))
        type = sharedPreferences2.getString("type", "").toString()
        payment = sharedPreferences3.getString("payment", "").toString()
        savedTitle = sharedPreferences2.getString("discountTitle", "").toString()
        valueOfOrder = sharedPreferences2.getString("valueOfOrder", "0.0")!!.toDouble()
        paymentFormDiscount = sharedPreferences2.getString("payment", "").toString()
    }


}