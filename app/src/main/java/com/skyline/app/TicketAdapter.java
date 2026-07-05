package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemTicketBinding;
import com.skyline.model.Ticket;
import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    private final List<Ticket> tickets;
    private final OnDetailClickListener onDetailClickListener;

    public interface OnDetailClickListener {
        void onDetailClick(Ticket ticket);
    }

    public TicketAdapter(List<Ticket> tickets, OnDetailClickListener onDetailClickListener) {
        this.tickets = tickets;
        this.onDetailClickListener = onDetailClickListener;
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTicketBinding binding = ItemTicketBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new TicketViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);
        ItemTicketBinding binding = holder.binding;

        binding.tvDay.setText(ticket.getDay());
        binding.tvMonthYear.setText(ticket.getMonthYear());
        binding.tvClass.setText(ticket.getFlightClass());
        binding.tvFlightNo.setText(ticket.getFlightNo());
        binding.tvOriginCode.setText(ticket.getOriginCode());
        binding.tvOriginCity.setText(ticket.getOriginCity());
        binding.tvDestCode.setText(ticket.getDestCode());
        binding.tvDestCity.setText(ticket.getDestCity());
        binding.tvTime.setText(ticket.getTime());
        binding.tvSeat.setText(ticket.getSeat());

        binding.btnDetail.setOnClickListener(v -> onDetailClickListener.onDetailClick(ticket));
        binding.btnChange.setOnClickListener(v -> { /* Handle change */ });
        binding.btnCancel.setOnClickListener(v -> { /* Handle cancel */ });
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    public static class TicketViewHolder extends RecyclerView.ViewHolder {
        final ItemTicketBinding binding;

        public TicketViewHolder(ItemTicketBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
