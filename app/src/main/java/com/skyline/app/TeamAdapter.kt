package com.skyline.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skyline.app.databinding.ItemTeamBinding
import com.skyline.model.TeamMember

class TeamAdapter(
    private val items: List<TeamMember>
) : RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val binding = ItemTeamBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class TeamViewHolder(
        private val binding: ItemTeamBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TeamMember) = with(binding) {
            imgTeam.setImageResource(item.imageRes)
            tvTeamName.text = item.name
        }
    }
}
