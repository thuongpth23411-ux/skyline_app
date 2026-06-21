package com.skyline.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skyline.app.databinding.ItemPromotionBinding
import com.skyline.model.Promotion

class PromotionAdapter(
    private val items: List<Promotion>,
    private val onClick: (Promotion) -> Unit = {}
) : RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder>() {

    inner class PromotionViewHolder(
        private val binding: ItemPromotionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Promotion) = with(binding) {
            imgPromo.setImageResource(item.imageRes)
            tvPromoTitle.text = item.title
            tvPromoDate.text = item.date
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromotionViewHolder {
        val binding = ItemPromotionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PromotionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PromotionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}