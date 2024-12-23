package com.example.nom_food_kotlin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.nom_food_kotlin.Model.Payment
import com.example.nom_food_kotlin.databinding.PaymentItemBinding

class PaymentAdapter (private var list: List<Payment>) : RecyclerView.Adapter<PaymentAdapter.viewHolder>() {
    var onClickItem : (Payment, Int) -> Unit = {_,_->}

    inner class viewHolder (private var binding: PaymentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnUse.setOnClickListener {
                onClickItem.invoke(list[position], adapterPosition)
            }
        }
        fun onBind(payment: Payment){
            binding.tvTitle.text = payment.title
            val options = RequestOptions().transform(CenterCrop())
            payment.picUrl?.let { url ->
                Glide.with(binding.picUrl.context)
                    .load(url)
                    .apply(options)
                    .into(binding.picUrl)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentAdapter.viewHolder {
        val binding = PaymentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentAdapter.viewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}