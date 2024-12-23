package com.example.nom_food_kotlin.Activity.Admin

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.nom_food_kotlin.Adapter.MenuAdapter
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivitySearchBillBinding
import com.example.nom_food_kotlin.databinding.ActivitySearchBinding
import kotlinx.coroutines.Job

class SearchBillActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBillBinding
    private lateinit var adapter: MenuAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    private var getListItems : MutableList<com.example.nom_food_kotlin.Model.Menu> = mutableListOf()
    private var searchList : MutableList<com.example.nom_food_kotlin.Model.Menu> = mutableListOf()
    private var emptyList : MutableList<com.example.nom_food_kotlin.Model.Menu> = mutableListOf()
    private var searchJob: Job? = null
    private lateinit var cartManager: CartManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBillBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_Nom_food_kotlin)
        setContentView(binding.root)
    }
}