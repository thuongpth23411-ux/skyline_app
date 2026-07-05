package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemTeamBinding;
import com.skyline.model.TeamMember;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {
    private List<TeamMember> items;

    public TeamAdapter(List<TeamMember> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamBinding binding = ItemTeamBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TeamViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        holder.bind(items.get(position % items.size()));
    }

    @Override
    public int getItemCount() {
        return items == null || items.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        private ItemTeamBinding binding;

        public TeamViewHolder(ItemTeamBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(TeamMember item) {
            binding.imgTeam.setImageResource(item.getImageRes());
            binding.tvTeamName.setText(item.getName());
        }
    }
}
