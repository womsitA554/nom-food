package com.example.nom_food_kotlin.Adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nom_food_kotlin.Model.Bill
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.BillAdminItemBinding
import com.example.nom_food_kotlin.databinding.BillItemBinding

class BillAdminAdapter (private val list: MutableList<Bill>) : RecyclerView.Adapter<BillAdminAdapter.ViewHolder>() {
    var onClickItem : (Bill, Int) -> Unit = { _, _ -> }
    inner class ViewHolder(private val binding: BillAdminItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onClickItem.invoke(list[adapterPosition], adapterPosition)
            }
        }
        fun bind(bill: Bill) {
            binding.tvBillId.text = bill.billId
            binding.tvDate.text = bill.date
            binding.tvPrice.text = "$ " + bill.total.toString()
            binding.tvStatus.text = bill.status

            when (bill.status) {
                "Pending" -> {
                    binding.tvStatus.setTextColor(Color.parseColor("#BC7001"))
                    binding.tvStatus.setBackgroundResource(R.drawable.status_pending_bg)
                }
                "Success" -> {
                    binding.tvStatus.setTextColor(Color.parseColor("#07650B"))
                    binding.tvStatus.setBackgroundResource(R.drawable.status_success_bg)
                }
                "Cancel" -> {
                    binding.tvStatus.setTextColor(Color.parseColor("#8F0101"))
                    binding.tvStatus.setBackgroundResource(R.drawable.status_cancel_bg)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BillAdminItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}