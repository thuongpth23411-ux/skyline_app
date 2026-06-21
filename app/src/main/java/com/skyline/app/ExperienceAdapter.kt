package com.skyline.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skyline.app.databinding.ItemExperienceBinding
import com.skyline.model.Experience

class ExperienceAdapter(
    private val items: List<Experience>,
    private val onClick: (Experience) -> Unit = {}
) : RecyclerView.Adapter<ExperienceAdapter.ExperienceViewHolder>() {

    inner class ExperienceViewHolder(
        private val binding: ItemExperienceBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Experience) = with(binding) {
            imgExperience.setImageResource(item.imageRes)
            tvExperienceTag.text = item.tag
            tvExperienceTitle.text = item.title
            tvExperienceDesc.text = item.description
            root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExperienceViewHolder {
        val binding = ItemExperienceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ExperienceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExperienceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}