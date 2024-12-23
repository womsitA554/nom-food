package com.example.nom_food_kotlin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.nom_food_kotlin.Model.Discount
import com.example.nom_food_kotlin.databinding.DiscountItemBinding

class DiscountAdapter (private val list: List<Discount>):RecyclerView.Adapter<DiscountAdapter.viewHolder>(){
    var onClickItem : (Discount, Int) -> Unit = {_,_->}
    var onUseClickItem : (Discount, Int) -> Unit = {_,_->}
    var onSelectionChanged: (() -> Unit)? = null
    inner class viewHolder (private val binding: DiscountItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onClickItem.invoke(list[position], position)
            }
            itemView.setOnClickListener {
                onUseClickItem?.invoke(list[adapterPosition], adapterPosition)
            }
        }
            fun onBind(discount: Discount){
                binding.tvTitle.text = discount.title
                binding.tvContent.text = discount.content

                binding.checkBox.isChecked = discount.isSelected
                binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                    discount.isSelected = isChecked
                    onSelectionChanged?.invoke()
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountAdapter.viewHolder {
        val binding = DiscountItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountAdapter.viewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}