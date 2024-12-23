package com.example.nom_food_kotlin.Activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nom_food_kotlin.Adapter.MenuAdapter
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.Model.Menu
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityCategoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.UUID

class CategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private var listMenu: MutableList<Menu> = mutableListOf()
    private var listSearch: MutableList<Menu> = mutableListOf()
    private lateinit var adapterMenu: MenuAdapter
    private lateinit var title: String
    private var searchJob: Job? = null
    private lateinit var cartManager: CartManager
    private lateinit var sharedPreferences: SharedPreferences
    private var userId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Nom_food_kotlin)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper()
        cartManager = CartManager(this)

        sharedPreferences = this.getSharedPreferences("loginSave", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "").toString()
        title = intent.getStringExtra("title").toString()
        binding.tvTitle.text = title

        lifecycleScope.launch {
            loadData()
        }

        setRecycleView()

        binding.btnBack.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        adapterMenu.onClickItem = { menu, i ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("userId", userId)
                putExtra("picUrl", menu.picUrl)
                putExtra("title", menu.title)
                putExtra("price", menu.price)
                putExtra("description", menu.description)
                putExtra("rating", menu.rating)
            }
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        adapterMenu.onAddCartClick = {menu, i ->
            val itemId = UUID.randomUUID().toString()

            val cartItem = Cart(
                itemId = itemId,
                title = menu.title,
                picUrl = menu.picUrl,
                price = menu.price?.toDouble(),
                quantity = 1
            )

            userId.let { user ->
                cartItem.quantity?.let { cartManager.addItemToCart(userId, cartItem, it) }

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

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300)  // debounce time
                    if (!s.isNullOrEmpty()) {
                        loadDataOfSearchItems(s.toString())
                    } else {
                        loadData()
                    }
                }
            }
        })


    }

    private suspend fun loadDataOfSearchItems(searchString: String) {
        try {
            listSearch.clear()
            withContext(Dispatchers.IO){
                listSearch.addAll(listMenu.filter {
                    it.title?.contains(searchString, ignoreCase = true) !!
                })
            }

            withContext(Dispatchers.Main){
                adapterMenu.updateData(listSearch)
            }
        } catch (e: Exception) {
            Log.d("search", e.printStackTrace().toString())
        }
    }

    private suspend fun loadData() {
        try {
            val data = withContext(Dispatchers.IO) {
                firebaseHelper.getAllCategoryOfItems(title)
            }
            listMenu.clear()
            listMenu.addAll(data)
            Log.d("data123", listMenu.toString())

            withContext(Dispatchers.Main) {
                adapterMenu.updateData(listMenu)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setRecycleView() {
        adapterMenu = MenuAdapter(listMenu)
        binding.rcvCategory.layoutManager = GridLayoutManager(this, 2)
        binding.rcvCategory.adapter = adapterMenu
    }
}