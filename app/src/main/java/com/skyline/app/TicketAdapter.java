package com.skyline.app;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.databinding.ItemTicketBinding;
import com.skyline.model.Ticket;
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
        holder.binding.tvFlightNo.setText(ticket.getFlightNo());
        holder.binding.tvOriginCode.setText(ticket.getOriginCode());
        holder.binding.tvOriginCity.setText(ticket.getOriginCity());
        holder.binding.tvDestCode.setText(ticket.getDestCode());
        holder.binding.tvDestCity.setText(ticket.getDestCity());
        holder.binding.tvTime.setText(ticket.getTime());
        holder.binding.tvSeat.setText(ticket.getSeat());
        holder.binding.tvTicketType.setText(ticket.getTicketType());

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
