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
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Adapter.OrderAdminAdapter
import com.example.nom_food_kotlin.Model.Bill
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.FragmentFirstBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

import androidx.fragment.app.activityViewModels
import com.example.nom_food_kotlin.Helper.SharedViewModel

class FirstFragment : Fragment() {
    private lateinit var _binding: FragmentFirstBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private var list: MutableList<Bill> = mutableListOf()
    private lateinit var adapter: OrderAdminAdapter
    private lateinit var ordersRef: DatabaseReference
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        firebaseHelper = FirebaseHelper()

        lifecycleScope.launch {
            loadData()
        }

        setRecyclerView()

        adapter.onClickItem = { bill, i ->
            val intent = Intent(context, OrderDetailActivity::class.java).apply {
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
            activity?.finish()
            activity?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        return binding.root
    }

    private suspend fun loadData() {
        withContext(Dispatchers.IO) {
            ordersRef = FirebaseDatabase.getInstance().getReference("Bills")
            ordersRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val order = snapshot.getValue(Bill::class.java)
                    order?.let {
                        if (it.status == "Pending") {
                            list.add(it)
                            adapter.notifyItemInserted(list.size - 1)
                        }
                    }
                    if (isAdded) {
                        val intent = Intent("UPDATE_ORDER_QUANTITY")
                        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)
                    }
                    updateUIBasedOnCart()
                    Log.d("checkOrder", list.toString())
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle order updates if needed
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    // Handle order removal if needed
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Handle order moves if needed
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        }
        withContext(Dispatchers.Main) {
            adapter.notifyDataSetChanged()
        }
    }

    private fun setRecyclerView() {
        adapter = OrderAdminAdapter(list)
        binding.rcvOrderAdmin.adapter = adapter
        binding.rcvOrderAdmin.setHasFixedSize(true)
        binding.rcvOrderAdmin.layoutManager = LinearLayoutManager(context)

        adapter.onClickAccept = { bill, i ->
            if (i < list.size) {
                bill.billId?.let { it -> firebaseHelper.updateStatus(it, "Success") }
                list[i].status = "Success"
                list.removeAt(i)
                adapter.notifyItemRemoved(i)
                updateUIBasedOnCart()

                val intent = Intent("UPDATE_ORDER_QUANTITY")
                LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)

                MotionToast.createColorToast(
                    requireActivity(),
                    null,
                    "Order accepted",
                    MotionToastStyle.SUCCESS,
                    Gravity.TOP,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(), www.sanju.motiontoast.R.font.helvetica_regular)
                )
            }
        }

        adapter.onClickCancel = { bill, i ->
            if (i < list.size) {
                val dialogView = layoutInflater.inflate(R.layout.dialog_cancel_order, null)
                val radioGroup = dialogView.findViewById<RadioGroup>(R.id.rgReasons)
                val etDifferentReason = dialogView.findViewById<EditText>(R.id.etDifferentReason)
                val btnSubmitReason = dialogView.findViewById<Button>(R.id.btnSubmitReason)
                val btnCancelReason = dialogView.findViewById<Button>(R.id.btnCancelReason)

                etDifferentReason.visibility = View.GONE

                radioGroup.setOnCheckedChangeListener { _, checkedId ->
                    if (checkedId == R.id.rbOther) {
                        etDifferentReason.visibility = View.VISIBLE
                    } else {
                        etDifferentReason.visibility = View.GONE
                    }
                }

                val alertDialog = AlertDialog.Builder(requireContext())
                    .setTitle("Reason for order cancellation")
                    .setView(dialogView)
                    .create()

                btnSubmitReason.setOnClickListener {
                    val selectedReasonId = radioGroup.checkedRadioButtonId

                    if (selectedReasonId == -1) {
                        MotionToast.createColorToast(
                            requireActivity(),
                            null,
                            "Please select a reason",
                            MotionToastStyle.WARNING,
                            Gravity.TOP,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(requireContext(), www.sanju.motiontoast.R.font.helvetica_regular)
                        )
                        return@setOnClickListener
                    }

                    val selectedReason = if (selectedReasonId == R.id.rbOther) {
                        val otherReason = etDifferentReason.text.toString()
                        if (otherReason.isBlank()) {
                            MotionToast.createColorToast(
                                requireActivity(),
                                null,
                                "Please provide a reason for 'Other'",
                                MotionToastStyle.WARNING,
                                Gravity.TOP,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(requireContext(), www.sanju.motiontoast.R.font.helvetica_regular)
                            )
                            return@setOnClickListener
                        } else {
                            otherReason
                        }
                    } else {
                        dialogView.findViewById<RadioButton>(selectedReasonId).text.toString()
                    }

                    bill.billId?.let { billId ->
                        firebaseHelper.updateStatus(billId, "Cancel")
                        firebaseHelper.updateReason(billId, "Sorry, your order has been cancelled because " + selectedReason)
                    }

                    list[i].status = "Cancel"
                    list.removeAt(i)
                    adapter.notifyItemRemoved(i)

                    updateUIBasedOnCart()

                    val intent = Intent("UPDATE_ORDER_QUANTITY")
                    LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent)

                    MotionToast.createColorToast(
                        requireActivity(),
                        null,
                        "Order Cancellation: $selectedReason",
                        MotionToastStyle.ERROR,
                        Gravity.TOP,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), www.sanju.motiontoast.R.font.helvetica_regular)
                    )

                    alertDialog.dismiss()
                }

                btnCancelReason.setOnClickListener {
                    alertDialog.dismiss()
                }

                alertDialog.show()
            }
        }
    }

    private fun updateUIBasedOnCart() {
        if (list.isEmpty()) {
            binding.rcvOrderAdmin.visibility = View.GONE
            binding.emptyOrder.visibility = View.VISIBLE
            binding.tvTitle.visibility = View.VISIBLE
            binding.pic.visibility = View.VISIBLE
        } else {
            binding.rcvOrderAdmin.visibility = View.VISIBLE
            binding.emptyOrder.visibility = View.GONE
            binding.tvTitle.visibility = View.GONE
            binding.pic.visibility = View.GONE
        }
    }
}