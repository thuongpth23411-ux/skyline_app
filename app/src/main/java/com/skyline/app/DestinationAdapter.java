package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.skyline.app.databinding.ItemDestinationBinding;
import com.skyline.model.Destination;
import java.util.List;

public class DestinationAdapter extends RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder> {
    private List<Destination> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Destination item);
    }

    public DestinationAdapter(List<Destination> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DestinationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDestinationBinding binding = ItemDestinationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DestinationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DestinationViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class DestinationViewHolder extends RecyclerView.ViewHolder {
        private ItemDestinationBinding binding;

        public DestinationViewHolder(ItemDestinationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Destination item) {
            String url = item.getImageUrl();
            if (url != null && !url.isEmpty()) {
                Glide.with(binding.imgDestination.getContext())
                        .load(url)
                        .placeholder(R.drawable.img_destination_hochiminh)
                        .error(R.drawable.img_destination_hochiminh)
                        .into(binding.imgDestination);
            } else if (item.getImageRes() != 0) {
                binding.imgDestination.setImageResource(item.getImageRes());
            } else {
                binding.imgDestination.setImageResource(R.drawable.img_destination_hochiminh);
            }
            binding.tvCountry.setText(item.getCountry());
            binding.tvDestinationTitle.setText(item.getTitle());
            binding.getRoot().setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}
