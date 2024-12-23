package com.example.nom_food_kotlin.Fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Activity.CategoryActivity
import com.example.nom_food_kotlin.Activity.DetailActivity
import com.example.nom_food_kotlin.Activity.SearchActivity
import com.example.nom_food_kotlin.Model.Category
import com.example.nom_food_kotlin.Adapter.CategoryAdapter
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Menu
import com.example.nom_food_kotlin.Adapter.MenuAdapter
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.nom_food_kotlin.databinding.FragmentMenuBinding
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.UUID

class MenuFragment : Fragment() {
    private lateinit var binding: FragmentMenuBinding
    private var listMenu: MutableList<Menu> = mutableListOf()
    private var listCategory: MutableList<Category> = mutableListOf()
    private lateinit var adapterMenu: MenuAdapter
    private lateinit var adapterCategory: CategoryAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var cartManager: CartManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMenuBinding.inflate(inflater, container, false)

        firebaseHelper = FirebaseHelper()
        cartManager = CartManager(requireContext())

        sharedPreferences = requireContext().getSharedPreferences("loginSave", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "")

        setRecycleView()

        lifecycleScope.launch {
            loadData()
        }

        binding.lnSearch.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)
        }
        binding.etSearch.setOnClickListener {
            val intent = Intent(context, SearchActivity::class.java).apply {
                putExtra("userId", userId)
            }
            startActivity(intent)
        }

        adapterCategory.onClickItem = {category, i ->
            val intent = Intent(context, CategoryActivity::class.java).apply {
                putExtra("userId", userId)
                putExtra("title", category.title)
            }
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        adapterMenu.onClickItem = { menu, i ->
            val intent = Intent(context, DetailActivity::class.java).apply {
                putExtra("userId", userId)
                putExtra("picUrl", menu.picUrl)
                putExtra("title", menu.title)
                putExtra("price", menu.price)
                putExtra("description", menu.description)
                putExtra("rating", menu.rating)
            }
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
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

            userId?.let { user ->
                cartManager.addItemToCart(userId, cartItem, 1)

                val intent = Intent("UPDATE_CART_QUANTITY")
                LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)

                MotionToast.createColorToast(
                    requireActivity(),
                    null,
                    "Added to cart",
                    MotionToastStyle.SUCCESS,
                    Gravity.TOP,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(), www.sanju.motiontoast.R.font.helvetica_regular)
                )
            }
        }

        return binding.root
    }
    private suspend fun loadData() {

        try {
            val data1 = withContext(Dispatchers.IO) {
                firebaseHelper.getAllCategories()
            }
            listCategory.clear()
            listCategory.addAll(data1)

            withContext(Dispatchers.Main) {
                adapterCategory.notifyDataSetChanged()
            }
            Log.d("MenuFragment1", "Data loaded: $data1")

            val data2 = withContext(Dispatchers.IO) {
                firebaseHelper.getAllPopularItems()
            }
            Log.d("MenuFragment", "Data loaded: $data2")
            listMenu.clear()
            listMenu.addAll(data2)

            withContext(Dispatchers.Main) {
                adapterMenu.notifyDataSetChanged()
            }
        } catch (e: Exception) {
            Log.d("loadData1", "Error loading data: ${e.message}")
            Log.d("loadData", "Error loading data: ${e.message}")
        }
    }

    private fun setRecycleView() {
        adapterCategory = CategoryAdapter(listCategory)
        binding.rcvCategory.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rcvCategory.adapter = adapterCategory

        adapterMenu = MenuAdapter(listMenu)
        binding.rcvMenu.layoutManager = GridLayoutManager(context, 2)
        binding.rcvMenu.adapter = adapterMenu
    }
}