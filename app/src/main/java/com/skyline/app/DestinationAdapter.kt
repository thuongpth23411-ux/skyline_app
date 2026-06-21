package com.skyline.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skyline.app.databinding.ItemDestinationBinding
import com.skyline.model.Destination

class DestinationAdapter(
    private val items: List<Destination>,
    private val onClick: (Destination) -> Unit = {}
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {

    inner class DestinationViewHolder(
        private val binding: ItemDestinationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Destination) = with(binding) {
            imgDestination.setImageResource(item.imageRes)
            tvCountry.text = item.country
            tvDestinationTitle.text = item.title
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemDestinationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DestinationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}