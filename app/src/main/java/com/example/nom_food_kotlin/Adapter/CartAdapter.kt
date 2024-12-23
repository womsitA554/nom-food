package com.example.nom_food_kotlin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.nom_food_kotlin.Model.Cart
import com.example.nom_food_kotlin.databinding.CartItemBinding

class CartAdapter(private val list: MutableList<Cart>) : RecyclerView.Adapter<CartAdapter.viewHolder>() {
    var onClickDeleteItem: (Cart, Int) -> Unit = {_,_->}
    var onClickMinusQuantity: (Cart, Int) -> Unit = {_,_->}
    var onClickPlusQuantity: (Cart, Int) -> Unit = {_,_->}
    inner class viewHolder(private val binding: CartItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnDelete.setOnClickListener {
                binding.btnDelete.isEnabled = false // Disable button
                onClickDeleteItem.invoke(list[position], position)
            }
            binding.btnMinus.setOnClickListener {
                onClickMinusQuantity.invoke(list[position], position)
            }
            binding.btnPlus.setOnClickListener {
                onClickPlusQuantity.invoke(list[position], position)
            }
        }
        fun onBind(cart: Cart) {
            val options = RequestOptions().transform(CenterCrop())
            cart.picUrl?.let { url ->
                Glide.with(binding.pic.context)
                    .load(url)
                    .apply(options)
                    .into(binding.pic)
            }
            binding.tvTitle.text = cart.title
            binding.tvQuantity.text = cart.quantity.toString()
            binding.tvPrice.text = "$ ${cart.price}"

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartAdapter.viewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartAdapter.viewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

}