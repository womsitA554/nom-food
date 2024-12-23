package com.example.nom_food_kotlin.Activity.Admin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.nom_food_kotlin.Fragment.CartFragment
import com.example.nom_food_kotlin.Fragment.MenuFragment
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Helper.SharedViewModel
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityMainAdminBinding
import kotlinx.coroutines.launch

import androidx.activity.viewModels
import androidx.lifecycle.Observer

class MainActivityAdmin : AppCompatActivity() {
    private lateinit var binding: ActivityMainAdminBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseHelper = FirebaseHelper()

        // Register the receiver to listen for cart update broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(updateOrderReceiver, IntentFilter("UPDATE_ORDER_QUANTITY"))

        binding = ActivityMainAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var selected = 0
        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(FirstFragment())
            binding.imageView.setImageResource(R.drawable.menu_black)
            selected = 1
        }

        updateIconSelected(selected)

        binding.menu.setOnClickListener {
            replaceFragment(FirstFragment())
            selected = 1
            updateIconSelected(selected)
        }

        binding.cart.setOnClickListener {
            replaceFragment(SecondFragment())
            selected = 2
            updateIconSelected(selected)
        }
        binding.profile.setOnClickListener {
            replaceFragment(ThirdFragment())
            selected = 3
            updateIconSelected(selected)
        }

        binding.bill.setOnClickListener {
            replaceFragment(FourthFragment())
            selected = 5
            updateIconSelected(selected)
        }

        val navigateTo = intent.getStringExtra("navigateToo")
        if (navigateTo == "FirstFragment") {
            replaceFragment(FirstFragment())
            updateIconSelected(1)
        } else if (navigateTo == "SecondFragment") {
            replaceFragment(SecondFragment())
            updateIconSelected(2)
        }

        lifecycleScope.launch {
            updateOrderQuantity()
        }

        // Observe order quantity changes
        sharedViewModel.orderQuantity.observe(this, Observer { quantity ->
            if (quantity == 0) {
                binding.tvQuantityOfCart.visibility = View.GONE
                binding.imgRedNotice.visibility = View.GONE
            } else {
                binding.tvQuantityOfCart.visibility = View.VISIBLE
                binding.imgRedNotice.visibility = View.VISIBLE
                binding.tvQuantityOfCart.text = quantity.toString()
            }
        })
    }

    fun updateIconSelected(selected: Int) {
        binding.imageView.setImageResource(if (selected == 1) R.drawable.menu_black else R.drawable.menu_grey)
        binding.imageView1.setImageResource(if (selected == 2) R.drawable.cart_black else R.drawable.cart_grey)
        binding.imageView2.setImageResource(if (selected == 3) R.drawable.person_black else R.drawable.person_grey)
        binding.imageView3.setImageResource(if (selected == 4) R.drawable.chat_black else R.drawable.chat_grey)
        binding.imageView4.setImageResource(if (selected == 5) R.drawable.bill_fragment_icon_black else R.drawable.bill_fragment_icon_grey)
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container_admin, fragment)
        fragmentTransaction.commit()
    }

    private fun updateOrderQuantity() {
        lifecycleScope.launch {
            val quantityOfOrder = firebaseHelper.getQuantityOfOrder()
            sharedViewModel.setOrderQuantity(quantityOfOrder)
        }
    }


    private val updateOrderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            lifecycleScope.launch {
                updateOrderQuantity()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        updateOrderQuantity()
    }


    override fun onDestroy() {
        super.onDestroy()
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateOrderReceiver)
    }

}