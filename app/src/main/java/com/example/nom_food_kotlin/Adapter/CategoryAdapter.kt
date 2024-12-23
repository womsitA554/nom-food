package com.example.nom_food_kotlin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.nom_food_kotlin.Model.Category
import com.example.nom_food_kotlin.databinding.CategoryItemBinding

class CategoryAdapter(private val list: List<Category>) :
    RecyclerView.Adapter<CategoryAdapter.viewHolder>() {
    var onClickItem: (Category, Int) -> Unit = { _, _ -> }

    inner class viewHolder(private val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onClickItem.invoke(list[position], position)
            }
        }

        fun onBind(category: Category) {
            val options = RequestOptions().transform(CenterCrop())
            category.picUrl?.let { url ->
                Glide.with(binding.pic.context)
                    .load(url)
                    .apply(options)
                    .into(binding.pic)
            }
            binding.tvTitle.text = category.title
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}