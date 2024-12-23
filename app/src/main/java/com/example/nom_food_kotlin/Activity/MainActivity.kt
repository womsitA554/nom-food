package com.example.nom_food_kotlin.Activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.nom_food_kotlin.Fragment.CartFragment
import com.example.nom_food_kotlin.Fragment.ChatFragment
import com.example.nom_food_kotlin.Fragment.MenuFragment
import com.example.nom_food_kotlin.Fragment.ProfileFragment
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var cartManager: CartManager
    private lateinit var sharedPreferences: SharedPreferences
    private var userId: String = ""

    private val fragments = mutableMapOf<Int, Fragment>()
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_Nom_food_kotlin)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        cartManager = CartManager(this)

        LocalBroadcastManager.getInstance(this).registerReceiver(updateCartReceiver, IntentFilter("UPDATE_CART_QUANTITY"))

        sharedPreferences = this.getSharedPreferences("loginSave", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "").toString()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        fragments[1] = MenuFragment()
        fragments[2] = CartFragment()
        fragments[3] = ProfileFragment()
        fragments[4] = ChatFragment()

        if (savedInstanceState == null) {
            showFragment(1)
            updateIconSelected(1)
        }

        binding.menu.setOnClickListener {
            showFragment(1)
            updateIconSelected(1)
        }

        binding.cart.setOnClickListener {
            showFragment(2)
            updateIconSelected(2)
        }

        updateCartQuantity()

        binding.profile.setOnClickListener {
            showFragment(3)
            updateIconSelected(3)
        }

        binding.chat.setOnClickListener {
            showFragment(4)
            updateIconSelected(4)
        }

        val navigateTo = intent.getStringExtra("navigateTo")
        when (navigateTo) {
            "MenuFragment" -> {
                showFragment(1)
                updateIconSelected(1)
            }
            "CartFragment" -> {
                showFragment(2)
                updateIconSelected(2)
            }
            "ProfileFragment" -> {
                showFragment(3)
                updateIconSelected(3)
            }
            "ChatFragment" -> {
                showFragment(4)
                updateIconSelected(4)
            }
        }
    }

    fun updateIconSelected(selected: Int) {
        binding.imageView.setImageResource(if (selected == 1) R.drawable.menu_black else R.drawable.menu_grey)
        binding.imageView1.setImageResource(if (selected == 2) R.drawable.cart_black else R.drawable.cart_grey)
        binding.imageView2.setImageResource(if (selected == 3) R.drawable.person_black else R.drawable.person_grey)
        binding.imageView3.setImageResource(if (selected == 4) R.drawable.chat_black else R.drawable.chat_grey)
    }

    private fun showFragment(fragmentId: Int) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        val newFragment = fragments[fragmentId] ?: return
        if (currentFragment != null && currentFragment == newFragment) return

        currentFragment?.let { fragmentTransaction.hide(it) }

        if (newFragment.isAdded) {
            fragmentTransaction.show(newFragment)
        } else {
            fragmentTransaction.add(R.id.fragment_container, newFragment)
        }

        fragmentTransaction.commit()
        currentFragment = newFragment
    }

    fun updateCartQuantity() {
        val quantityOfItem = cartManager.countItem(userId)
        if (quantityOfItem == 0) {
            binding.tvQuantityOfCart.visibility = View.GONE
            binding.imgRedNotice.visibility = View.GONE
        } else {
            binding.tvQuantityOfCart.visibility = View.VISIBLE
            binding.imgRedNotice.visibility = View.VISIBLE
            binding.tvQuantityOfCart.text = quantityOfItem.toString()
        }
    }

    private val updateCartReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateCartQuantity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateCartReceiver)
    }
}

