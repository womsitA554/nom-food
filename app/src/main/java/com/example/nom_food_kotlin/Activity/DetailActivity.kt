package com.example.nom_food_kotlin.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityDetailBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.UUID

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private var quantity = 1
    private var price = 0.0F
    private var totalPrice = 0.0F
    private lateinit var cartManager: CartManager
    private lateinit var sharedPreferences: SharedPreferences
    private var userId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Nom_food_kotlin)
        setContentView(binding.root)

        cartManager = CartManager(this)

        sharedPreferences = this.getSharedPreferences("loginSave", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "").toString()

        val title = intent.getStringExtra("title")
        val picUrl = intent.getStringExtra("picUrl")
        price = intent.getFloatExtra("price", 0.0F)
        val description = intent.getStringExtra("description")
        val rating = intent.getFloatExtra("rating", 0.0F)


        binding.tvRating.text = "(" + rating + ")"
        binding.ratingBar.rating = rating
        binding.tvTitle.text = title
        binding.tvPrice.text = "$ " + price
        binding.tvDescription.text = description
        binding.tvQuantity.text = quantity.toString()
        binding.tvTotalPrice.text = "$ " + price.toString()

        val options = RequestOptions().transform(CenterCrop())
        Glide.with(this).load(picUrl).apply(options).into(binding.picUrl)

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        calQuantity()

        binding.btnAddCart.setOnClickListener {
            val itemId = UUID.randomUUID().toString()
            val cartItem = Cart(
                itemId = itemId,
                title = title,
                picUrl = picUrl,
                price = totalPrice.toDouble(),
                quantity = quantity
            )
            if (userId != null) {
                    cartManager.addItemToCart(userId, cartItem, quantity)

                // Send a broadcast to update the cart quantity
                val intent = Intent("UPDATE_CART_QUANTITY")
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                MotionToast.createColorToast(
                    this,
                    null,
                    "Added to cart",
                    MotionToastStyle.SUCCESS,
                    Gravity.TOP,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
                )
            }
        }

    }

    private fun calQuantity() {
        binding.btnMinus.setOnClickListener {
            if (quantity > 1) {
                quantity -= 1
                totalPrice -= price
                binding.tvTotalPrice.text = "$ " + totalPrice.toString()
                binding.tvQuantity.text = quantity.toString()
            }
        }
        totalPrice = price
        binding.btnPlus.setOnClickListener {
            quantity += 1
            totalPrice += price
            binding.tvTotalPrice.text = "$ " + totalPrice.toString()
            binding.tvQuantity.text = quantity.toString()
        }
    }
}