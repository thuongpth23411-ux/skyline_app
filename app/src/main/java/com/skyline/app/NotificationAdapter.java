package com.skyline.app;

import com.skyline.app.utils.NotificationHelper;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.model.Notification;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {
    private List<Notification> items;

    public NotificationAdapter(List<Notification> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        Notification item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvContent.setText(item.getContent());
        holder.tvTime.setText(item.getTime());

        // Gắn màu sắc theo loại thông báo
        if (item.getType() != null) {
            int color = holder.itemView.getContext().getResources().getColor(R.color.skyline_blue);
            if (item.getType().equals("PROMOTION")) color = holder.itemView.getContext().getResources().getColor(R.color.auth_gold);
            else if (item.getType().equals("TICKET")) color = holder.itemView.getContext().getResources().getColor(R.color.auth_success);
            else if (item.getType().equals("PROFILE")) color = holder.itemView.getContext().getResources().getColor(R.color.auth_blue);
            holder.viewTypeColor.setBackgroundColor(color);
        }

        holder.itemView.setOnClickListener(v -> {
            if (v.getContext() instanceof Activity) {
                Activity activity = (Activity) v.getContext();
                String typeStr = item.getType();
                
                if (typeStr != null && !typeStr.isEmpty()) {
                    try {
                        NotificationHelper.NotifType type = NotificationHelper.NotifType.valueOf(typeStr);
                        handleRedirection(activity, type, item.getTargetData());
                    } catch (IllegalArgumentException e) {
                        activity.startActivity(new Intent(activity, NotificationActivity.class));
                    }
                } else {
                    // Tự động đoán loại dựa trên tiêu đề nếu dữ liệu cũ không có Type
                    if (item.getTitle().contains("hồ sơ") || item.getTitle().contains("hạng")) {
                        handleRedirection(activity, NotificationHelper.NotifType.PROFILE, null);
                    } else if (item.getTitle().contains("vé") || item.getTitle().contains("chuyến bay")) {
                        handleRedirection(activity, NotificationHelper.NotifType.TICKET, null);
                    } else if (item.getTitle().contains("Ưu đãi") || item.getTitle().contains("Khuyến mãi")) {
                        handleRedirection(activity, NotificationHelper.NotifType.PROMOTION, null);
                    }
                }
            }
        });
    }

    private void handleRedirection(Activity activity, NotificationHelper.NotifType type, String targetData) {
        Intent intent;
        switch (type) {
            case PROMOTION:
                intent = new Intent(activity, PromotionsActivity.class);
                if (targetData != null) intent.putExtra("OPEN_PROMO_NAME", targetData);
                break;
            case TICKET:
                intent = new Intent(activity, HomeActivity.class);
                intent.putExtra("TARGET_FRAGMENT", "FLIGHTS");
                break;
            case PROFILE:
                intent = new Intent(activity, ProfileActivity.class);
                break;
            default:
                return;
        }
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent, tvTime;
        View viewTypeColor;
        NotifViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
            viewTypeColor = itemView.findViewById(R.id.viewTypeColor);
        }
    }
}
