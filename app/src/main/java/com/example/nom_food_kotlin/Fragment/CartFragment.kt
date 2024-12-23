package com.example.nom_food_kotlin.Fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Activity.MainActivity
import com.example.nom_food_kotlin.Activity.OrderActivity
import com.example.nom_food_kotlin.Adapter.CartAdapter
import com.example.nom_food_kotlin.Helper.CartManager
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.FragmentCartBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private var listCart: MutableList<Cart> = mutableListOf()
    private lateinit var adapterCart: CartAdapter
    private lateinit var cartManager: CartManager
    private var userId : String = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)


        val sharedPreferences = requireContext().getSharedPreferences("loginSave", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "").toString()

        cartManager = CartManager(requireContext())

        binding.btnGoShopNow.setOnClickListener{
            updateIcons()
            openFragment(MenuFragment())
        }

        binding.btnNext.setOnClickListener{
            val intent = Intent(context, OrderActivity::class.java)
            startActivity(intent)
            activity?.finish()
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        lifecycleScope.launch {
            loadData()
        }

        setRecycleView()

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            cartUpdateReceiver,
            IntentFilter("UPDATE_CART_QUANTITY")
        )

        adapterCart.onClickMinusQuantity = { cart, i ->
            cart.itemId?.let {
                if (cart.quantity!! > 1) {  // Only decrement if quantity is greater than 1
                    val eachPrice = cart.price?.div(cart.quantity!!)
                    cart.quantity = cart.quantity!! - 1
                    if (eachPrice != null) {
                        cart.price = eachPrice * cart.quantity!!
                    }
                    cartManager.updateQuantity(userId, cart.itemId, cart.quantity!!)
                    cartManager.updatePrice(userId, cart.itemId, cart.price!!)
                }
                cartManager.updateQuantity(userId, it, cart.quantity!!)
            }
            adapterCart.notifyItemChanged(i)
        }

        adapterCart.onClickPlusQuantity = { cart, i ->
            cart.itemId?.let {
                val eachPrice = cart.price?.div(cart.quantity!!)
                cart.quantity = cart.quantity!! + 1
                if (eachPrice != null) {
                    cart.price = eachPrice * cart.quantity!!
                }
                cartManager.updateQuantity(userId, cart.itemId, cart.quantity!!)
                cartManager.updatePrice(userId, cart.itemId, cart.price!!)
            }
            adapterCart.notifyItemChanged(i)
        }

        return binding.root
    }

    private suspend fun loadData(){
        withContext(Dispatchers.IO){
            val list = cartManager.getCart(userId)
            listCart.clear()
            listCart.addAll(list)
        }

        updateUIBasedOnCart()

        withContext(Dispatchers.Main){
            adapterCart.notifyDataSetChanged()
        }
    }

    private fun setRecycleView() {
        adapterCart = CartAdapter(listCart)
        binding.rcvCart.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rcvCart.adapter = adapterCart

        adapterCart.onClickDeleteItem = {cart, i ->
            cart.itemId?.let {
                cartManager.removeItemFromCart( userId, it)
                listCart.removeAt(i)
                adapterCart.notifyItemRemoved(i)

                val intent = Intent("UPDATE_CART_QUANTITY")
                LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)

                updateUIBasedOnCart() // Check if the cart is empty after removal
                MotionToast.createColorToast(
                    requireActivity(),
                    null,
                    "Removed from cart",
                    MotionToastStyle.DELETE,
                    Gravity.TOP,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(), www.sanju.motiontoast.R.font.helvetica_regular)
                )
            }
        }
    }

    private fun updateIcons() {
        (activity as? MainActivity)?.updateIconSelected(selected = 1)
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }

    private fun updateUIBasedOnCart() {
        if (listCart.isEmpty()) {
            binding.rcvCart.visibility = View.GONE
            binding.btnNext.visibility = View.GONE
            binding.btnGoShopNow.visibility = View.VISIBLE
            binding.textView.visibility = View.VISIBLE
        } else {
            binding.rcvCart.visibility = View.VISIBLE
            binding.btnNext.visibility = View.VISIBLE
            binding.btnGoShopNow.visibility = View.GONE
            binding.textView.visibility = View.GONE
        }
    }

    private val cartUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            lifecycleScope.launch {
                loadData()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(cartUpdateReceiver)
    }
}