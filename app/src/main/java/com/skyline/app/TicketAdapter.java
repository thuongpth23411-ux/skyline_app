package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemTicketBinding;
import com.skyline.model.Ticket;
import com.bumptech.glide.Glide;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private List<Ticket> tickets;
    private OnTicketActionListener listener;

    public interface OnTicketActionListener {
        void onDetailClick(Ticket ticket);
        void onCancelClick(Ticket ticket);
        void onChangeClick(Ticket ticket);
    }

    public TicketAdapter(List<Ticket> tickets, OnTicketActionListener listener) {
        this.tickets = tickets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTicketBinding binding = ItemTicketBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new TicketViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        holder.binding.tvDay.setText(ticket.getDay());
        holder.binding.tvMonthYear.setText(ticket.getMonthYear());
        holder.binding.tvClass.setText(ticket.getFlightClass());
        // Since tvFlightNo was replaced by tvStatusTag in layout, we might want to show flight number elsewhere or keep it.
        // Let's check if we still need to set it. In layout it was replaced.
        holder.binding.tvOriginCode.setText(ticket.getOriginCode());
        holder.binding.tvOriginCity.setText(ticket.getOriginCity());
        holder.binding.tvDestCode.setText(ticket.getDestCode());
        holder.binding.tvDestCity.setText(ticket.getDestCity());
        holder.binding.tvTime.setText(ticket.getTime());
        holder.binding.tvSeat.setText(ticket.getSeat());
        holder.binding.tvTicketType.setText(ticket.getTicketType());

        if (ticket.getAirlineLogoUrl() != null) {
            Glide.with(holder.itemView.getContext())
                .load(ticket.getAirlineLogoUrl())
                .placeholder(R.drawable.ic_plane)
                .error(R.drawable.ic_plane)
                .into(holder.binding.ivQR);
        } else {
            holder.binding.ivQR.setImageResource(R.drawable.ic_plane);
        }

        if (ticket.getStatus() != null) {
            String statusText = ticket.getStatus();
            int color = 0xFF757575; // Gray default
            int bgColor = 0xFFF3F4F6; // Light gray default
            
            switch (statusText) {
                case "Booked":
                    statusText = "Đã đặt";
                    color = 0xFF2E7D32; // Green
                    bgColor = 0xFFE8F5E9; // Light Green
                    break;
                case "Completed":
                    statusText = "Hoàn thành";
                    color = 0xFF1976D2; // Blue
                    bgColor = 0xFFE3F2FD; // Light Blue
                    break;
                case "Cancelled":
                    statusText = "Đã hủy";
                    color = 0xFFD32F2F; // Red
                    bgColor = 0xFFFFEBEE; // Light Red
                    break;
                case "Disabled":
                    statusText = "Vô hiệu hóa";
                    color = 0xFF757575; // Gray
                    bgColor = 0xFFEEEEEE; // Gray
                    break;
            }
            holder.binding.tvStatusTag.setText(statusText);
            holder.binding.tvStatusTag.setTextColor(color);
            holder.binding.tvStatusTag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgColor));
        } else {
            holder.binding.tvStatusTag.setText("Booked");
            holder.binding.tvStatusTag.setTextColor(0xFF757575);
            holder.binding.tvStatusTag.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF3F4F6));
        }

        holder.binding.btnDetail.setOnClickListener(v -> listener.onDetailClick(ticket));
        holder.binding.btnCancel.setOnClickListener(v -> listener.onCancelClick(ticket));
        holder.binding.btnChange.setOnClickListener(v -> listener.onChangeClick(ticket));
        holder.binding.ivQR.setOnClickListener(v -> listener.onDetailClick(ticket));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        ItemTicketBinding binding;

        public TicketViewHolder(ItemTicketBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
