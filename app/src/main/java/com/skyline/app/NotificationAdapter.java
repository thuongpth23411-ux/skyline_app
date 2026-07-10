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

        holder.itemView.setOnClickListener(v -> {
            if (v.getContext() instanceof Activity) {
                Activity activity = (Activity) v.getContext();
                if (item.getType() != null && !item.getType().isEmpty()) {
                    try {
                        NotificationHelper.NotifType type = NotificationHelper.NotifType.valueOf(item.getType());
                        handleRedirection(activity, type, item.getTargetData());
                    } catch (IllegalArgumentException e) {
                        // Fallback if type is invalid
                        activity.startActivity(new Intent(activity, NotificationActivity.class));
                    }
                } else {
                    // Default if no type
                    activity.startActivity(new Intent(activity, NotificationActivity.class));
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
        NotifViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
