package com.example.nom_food_kotlin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.nom_food_kotlin.Model.Menu
import com.example.nom_food_kotlin.databinding.MenuItemBinding

class MenuAdapter(private var list: List<Menu>) : RecyclerView.Adapter<MenuAdapter.viewHolder>() {
    var onClickItem: (Menu, Int) -> Unit = {_,_ ->}
    var onAddCartClick : (Menu, Int) -> Unit = {_,_->}
    fun updateData(newList :List<Menu>){
        list = newList
        notifyDataSetChanged()
    }

    inner class viewHolder(private val binding: MenuItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onClickItem.invoke(list[position], position)
            }
            binding.btnAddCart.setOnClickListener {
                onAddCartClick.invoke(list[position], position)
            }
        }
        fun onBind(menu: Menu){
            val options = RequestOptions().transform(CenterCrop())
            menu.picUrl?.let { url ->
                Glide.with(binding.pic.context)
                    .load(url)
                    .apply(options)
                    .into(binding.pic)
            }
            binding.tvTitle.text = menu.title
            binding.tvPrice.text = "$ " + menu.price.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}