package com.example.nom_food_kotlin.Fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Activity.BillActivity
import com.example.nom_food_kotlin.Activity.OrderActivity
import com.example.nom_food_kotlin.Activity.OrderAgainActivity
import com.example.nom_food_kotlin.Adapter.BillAdapter
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Bill
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.FragmentBillBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillFragment : Fragment() {
    private lateinit var binding: FragmentBillBinding
    private lateinit var billAdapter: BillAdapter
    private var listBill : MutableList<Bill> = mutableListOf()
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private var userId: String? = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBillBinding.inflate(inflater, container, false)

        firebaseHelper = FirebaseHelper()
        sharedPreferences = requireActivity().getSharedPreferences("loginSave", MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "")

        setRecyclerView()

        lifecycleScope.launchWhenCreated {
            loadData()
        }

        billAdapter.onClickItem = { bill, _ ->
            val intent = Intent(context, BillActivity::class.java).apply {
                putExtra("billId", bill.billId)
                putExtra("date", bill.date)
                putExtra("address", bill.address)
                putExtra("items", ArrayList(bill.items))
                putExtra("payment", bill.payment)
                putExtra("delivery", bill.delivery)
                putExtra("discount", bill.discount)
                putExtra("totalCart", bill.totalCart)
                putExtra("total", bill.total)
                putExtra("status", bill.status)
                putExtra("note", bill.note)
                putExtra("reason", bill.reason)
            }
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        billAdapter.onClickOrderAgain = { bill, _ ->
            val intent = Intent(context, OrderAgainActivity::class.java).apply {
                putExtra("address", bill.address)
                putExtra("items", ArrayList(bill.items))
                putExtra("totalCart", bill.totalCart)
                putExtra("total", bill.total)
                putExtra("status", bill.status)
                putExtra("note", bill.note)
                putExtra("reason", bill.reason)
            }
            startActivity(intent)
            activity?.finish()
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        return binding.root
    }

    suspend fun loadData(){
        val data = withContext(Dispatchers.IO){
            userId?.let { firebaseHelper.getBillsForCurrentUser(it) }
        }

        listBill.clear()
        if (data != null) {
            listBill.addAll(data)
        }

        withContext(Dispatchers.Main){
            billAdapter.notifyDataSetChanged()
        }
    }

    private fun setRecyclerView() {
        billAdapter = BillAdapter(listBill)
        binding.rcvBill.adapter = billAdapter
        binding.rcvBill.layoutManager = LinearLayoutManager(context)
    }
}