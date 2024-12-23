package com.example.nom_food_kotlin.Activity.Admin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Adapter.FoodAdapter
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Menu
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.FragmentSecondBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class SecondFragment : Fragment() {

    private lateinit var binding: FragmentSecondBinding
    private val list: MutableList<Menu> = mutableListOf()
    private lateinit var adapter: FoodAdapter
    private lateinit var firebaseHelper: FirebaseHelper
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecondBinding.inflate(inflater, container, false)

        firebaseHelper = FirebaseHelper()

        lifecycleScope.launch {
            loadData()
        }
        setRecyclerView()
        adapter.onEditClickItem = { menu, i ->
            val intent = Intent(context, AddOrEditFoodActivity::class.java).apply {
                putExtra("itemId", menu.itemId)
                putExtra("picUrl", menu.picUrl)
                putExtra("title", menu.title)
                putExtra("price", menu.price)
                putExtra("description", menu.description)
            }
            startActivity(intent)
            activity?.finish()
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        adapter.onDeleteClickItem = { menu, i ->
            val dialogView = layoutInflater.inflate(R.layout.dialog_delete_item, null)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }

            btnConfirm.setOnClickListener {
                menu.itemId?.let { menu.category?.let { it1 -> firebaseHelper.deleteMenu(it1, it) } }
                Log.d("checkItemID", menu.itemId.toString())
                list.removeAt(i)
                adapter.notifyItemRemoved(i)
                MotionToast.createColorToast(
                    requireActivity(),
                    null,
                    "Removed from cart",
                    MotionToastStyle.DELETE,
                    Gravity.TOP,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(
                        requireContext(),
                        www.sanju.motiontoast.R.font.helvetica_regular
                    )
                )
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        binding.btnAddNewItem.setOnClickListener {
            val intent = Intent(context, AddOrEditFoodActivity::class.java)
            startActivity(intent)
            activity?.finish()
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
        return binding.root
    }

    suspend fun loadData() {
        val data = withContext(Dispatchers.IO) {
            firebaseHelper.getAllItems()
        }
        Log.d("checkData", data.toString())
        list.clear()
        list.addAll(data)
        withContext(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }

    fun setRecyclerView() {
        adapter = FoodAdapter(list)
        binding.rcvFood.adapter = adapter
        binding.rcvFood.setHasFixedSize(true)
        binding.rcvFood.layoutManager = LinearLayoutManager(context)
    }
}