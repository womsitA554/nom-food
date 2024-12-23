package com.example.nom_food_kotlin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.databinding.OrderItemBinding

class OrderAdapter(private val list: List<Cart>) : RecyclerView.Adapter<OrderAdapter.viewHolder>() {
    class viewHolder(private val binding: OrderItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun onBind(order: Cart){
            val options = RequestOptions().transform(CenterCrop())
            order.picUrl?.let { url ->
                Glide.with(binding.img.context)
                    .load(url)
                    .apply(options)
                    .into(binding.img)
            }
            binding.tvTitle.text = order.title
            binding.tvQuantity.text = "x" + order.quantity
            binding.tvTotalPrice.text ="$ " + order.price
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = OrderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}