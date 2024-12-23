package com.example.nom_food_kotlin.Activity.Admin

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nom_food_kotlin.Activity.BillActivity
import com.example.nom_food_kotlin.Adapter.BillAdapter
import com.example.nom_food_kotlin.Adapter.BillAdminAdapter
import com.example.nom_food_kotlin.Adapter.OrderAdminAdapter
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Bill
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.FragmentFirstBinding
import com.example.nom_food_kotlin.databinding.FragmentFourthBinding
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class FourthFragment : Fragment() {
    private lateinit var _binding: FragmentFourthBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private var list: MutableList<Bill> = mutableListOf()
    private lateinit var adapter: BillAdminAdapter
    private lateinit var ordersRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentFourthBinding.inflate(inflater, container, false)

        firebaseHelper = FirebaseHelper()

        firebaseHelper = FirebaseHelper()

        lifecycleScope.launch {
            loadData()
        }

//        setupFilters()
        setRecyclerView()

        adapter.onClickItem = { bill, _ ->
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

        return _binding.root
    }

//    private fun setupFilters() {
//        // Xử lý sự kiện khi nhấn vào biểu tượng lịch
//        _binding.imgCalendar.setOnClickListener {
//            val calendar = Calendar.getInstance()
//            val datePickerDialog = DatePickerDialog(
//                requireContext(),
//                { _, year, month, dayOfMonth ->
//                    // Sau khi người dùng chọn ngày
//                    filterBills(dayOfMonth, month + 1, year, _binding.statusSpinner.selectedItem.toString())
//                },
//                calendar.get(Calendar.YEAR),
//                calendar.get(Calendar.MONTH),
//                calendar.get(Calendar.DAY_OF_MONTH)
//            )
//
//            // Hiển thị DatePickerDialog
//            datePickerDialog.show()
//        }
//
//        // Xử lý sự kiện khi thay đổi status trong Spinner
//        _binding.statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                // Lấy giá trị ngày, tháng, năm hiện tại từ DatePickerDialog hoặc một biến đã lưu trước đó
//                val selectedDate = getCurrentSelectedDate()
//                filterBills(selectedDate.day, selectedDate.month, selectedDate.year, _binding.statusSpinner.selectedItem.toString())
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Không làm gì cả
//            }
//        }
//    }

    // Hàm để lấy ngày, tháng, năm hiện tại từ DatePicker hoặc biến đã lưu
    private fun getCurrentSelectedDate(): SelectedDate {
        // Trả về đối tượng SelectedDate chứa các giá trị ngày, tháng, năm
        return SelectedDate(
            day = 0,
            month = 0,
            year = 0
        )
    }

    // Hàm để lọc hóa đơn dựa trên ngày, tháng, năm, và status
    private fun filterBills(day: Int, month: Int, year: Int, status: String) {
        // Thực hiện lọc hóa đơn dựa trên các tiêu chí đã chọn
        // Sử dụng danh sách bill để lọc
        // val filteredBills = allBills.filter { it.date.year == year && it.date.month == month && it.status == status }
        // Cập nhật RecyclerView với danh sách đã lọc
        // adapter.submitList(filteredBills)
    }

    // Lớp để chứa các giá trị ngày, tháng, năm đã chọn
    data class SelectedDate(val day: Int, val month: Int, val year: Int)



    private fun setRecyclerView() {
        adapter = BillAdminAdapter(list)
        _binding.rcvBill.adapter = adapter
        _binding.rcvBill.layoutManager = LinearLayoutManager(context)
    }

    suspend fun loadData() {
        withContext(Dispatchers.IO){
            ordersRef = FirebaseDatabase.getInstance().getReference("Bills")
            ordersRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val order = snapshot.getValue(Bill::class.java)
                    order?.let {
                            list.add(it)
                            adapter.notifyItemInserted(list.size - 1)
                    }
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
        withContext(Dispatchers.Main){
            adapter.notifyDataSetChanged()
        }
    }

}