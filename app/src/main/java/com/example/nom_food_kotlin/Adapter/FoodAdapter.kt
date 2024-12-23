package com.example.nom_food_kotlin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.nom_food_kotlin.Model.Menu
import com.example.nom_food_kotlin.databinding.FoodItemBinding

class FoodAdapter(private val list: List<Menu>) : RecyclerView.Adapter<FoodAdapter.viewHolder>() {
    var onEditClickItem : (Menu, Int) -> Unit = { _, _ ->}
    var onDeleteClickItem : (Menu, Int) -> Unit = {_,_ ->}
    inner class viewHolder(private val binding: FoodItemBinding) : RecyclerView.ViewHolder(binding.root){
        init {
            binding.btnEdit.setOnClickListener {
                onEditClickItem(list[adapterPosition], adapterPosition)
            }
            binding.btnDelete.setOnClickListener {
                onDeleteClickItem(list[adapterPosition], adapterPosition)
            }
        }
        fun onBind(order: Menu){
            val options = RequestOptions().transform(CenterCrop())
            order.picUrl?.let { url ->
                Glide.with(binding.img.context)
                    .load(url)
                    .apply(options)
                    .into(binding.img)
            }
            binding.tvTitle.text = order.title
            binding.tvTotalPrice.text ="$ " + order.price
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = FoodItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}