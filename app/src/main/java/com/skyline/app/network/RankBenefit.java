package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;

public class RankBenefit {
    @SerializedName("_id")
    private String id;
    private String rank;
    private String title;
    private String description;
    private String iconType; // e.g., "points", "checkin", "voucher"

    public String getId() { return id; }
    public String getRank() { return rank; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIconType() { return iconType; }
}
