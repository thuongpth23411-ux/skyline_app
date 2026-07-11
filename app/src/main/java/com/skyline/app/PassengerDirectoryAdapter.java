package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemPassengerBinding;
import com.skyline.app.network.PassengerDirectory;
import java.util.List;

public class PassengerDirectoryAdapter extends RecyclerView.Adapter<PassengerDirectoryAdapter.ViewHolder> {
    private List<PassengerDirectory> items;
    private final OnPassengerClickListener listener;

    public interface OnPassengerClickListener {
        void onItemClick(PassengerDirectory item);
    }

    public PassengerDirectoryAdapter(List<PassengerDirectory> items, OnPassengerClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void setItems(List<PassengerDirectory> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPassengerBinding binding = ItemPassengerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] words = name.trim().split("\\s+");
        if (words.length == 0) return "?";
        if (words.length == 1) return words[0].substring(0, 1).toUpperCase();
        return words[0].substring(0, 1).toUpperCase() + words[words.length - 1].substring(0, 1).toUpperCase();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemPassengerBinding binding;

        ViewHolder(ItemPassengerBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PassengerDirectory item) {
            binding.tvPassengerName.setText(item.getPassengerName().toUpperCase());
            binding.tvPassengerPhone.setText(item.getPassengerPhone());
            binding.tvPassengerInitials.setText(getInitials(item.getPassengerName()));
            
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
