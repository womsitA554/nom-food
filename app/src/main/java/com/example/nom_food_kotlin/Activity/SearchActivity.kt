package com.example.nom_food_kotlin.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nom_food_kotlin.Adapter.MenuAdapter
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivitySearchBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.UUID

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var adapter: MenuAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private var getListItems : MutableList<com.example.nom_food_kotlin.Model.Menu> = mutableListOf()
    private var searchList : MutableList<com.example.nom_food_kotlin.Model.Menu> = mutableListOf()
    private var emptyList : MutableList<com.example.nom_food_kotlin.Model.Menu> = mutableListOf()
    private var searchJob: Job? = null
    private lateinit var cartManager: CartManager

    override fun onCreate(savedInstanceState: Bundle?) {
        binding =ActivitySearchBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Nom_food_kotlin)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper()
        cartManager = CartManager(this)

        val userId = intent.getStringExtra("userId").toString()

        binding.etSearch.requestFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)

        binding.etSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(0)
                    if (!s.isNullOrEmpty()){
                        searchData(s.toString())
                    } else{
                        adapter.updateData(emptyList)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}

        })

        lifecycleScope.launch {
            loadData()
        }

        setRecycleView()

        adapter.onClickItem = {menu, i ->
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
        adapter.onAddCartClick = {menu, i ->
            val itemId = UUID.randomUUID().toString()

            val cartItem = Cart(
                itemId = itemId,
                title = menu.title,
                picUrl = menu.picUrl,
                price = menu.price?.toDouble(),
                quantity = 1
            )

            userId.let { user ->
                cartManager.addItemToCart(userId, cartItem, 1)

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

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private suspend fun searchData(string:String){
        try {
            searchList.clear()
            withContext(Dispatchers.IO){
                searchList.addAll(getListItems.filter {
                    it.title?.contains(string, ignoreCase = true) !!
                })
            }
            withContext(Dispatchers.Main){
                adapter.updateData(searchList)
            }
            Log.d("searchs", searchList.toString())
        } catch (e :Exception){
            Log.d("searchs", e.toString())
        }
    }

    private suspend fun loadData(){
        try {
            val data = withContext(Dispatchers.IO){
                firebaseHelper.getAllItems()
            }
            getListItems.clear()
            getListItems.addAll(data)
            Log.d("getListItems", getListItems.toString())
        } catch (e :Exception){
            Log.d("getListItems", e.toString())
        }
    }

    private fun setRecycleView() {
        adapter = MenuAdapter(emptyList)
        binding.rcvCategory.layoutManager = GridLayoutManager(this, 2)
        binding.rcvCategory.adapter = adapter
    }
}