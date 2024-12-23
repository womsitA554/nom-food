package com.example.nom_food_kotlin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nom_food_kotlin.Model.Bill
import com.example.nom_food_kotlin.databinding.OrderAdminItemBinding

class OrderAdminAdapter (private val list: List<Bill>) : RecyclerView.Adapter<OrderAdminAdapter.ViewHolder>() {
    var onClickItem : (Bill, Int) -> Unit = { _, _ -> }
    var onClickAccept : (Bill, Int) -> Unit = { _, _ -> }
    var onClickCancel : (Bill, Int) -> Unit = { _, _ -> }
    inner class ViewHolder (private val binding: OrderAdminItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onClickItem.invoke(list[adapterPosition], adapterPosition)
            }


            binding.btnAccept.setOnClickListener {
                binding.btnAccept.isEnabled = false // Disable button
                onClickAccept.invoke(list[adapterPosition], adapterPosition)
            }

            binding.btnCancel.setOnClickListener {
                onClickCancel.invoke(list[position], position)
            }
        }
        fun onbind(bill: Bill) {
            binding.tvBillId.text = bill.billId
            binding.tvDate.text = bill.date
            binding.tvPrice.text = "$ " + bill.total.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = OrderAdminItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onbind(list[position])
    }
}