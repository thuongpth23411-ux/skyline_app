package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;

public class RankBenefit {
    @SerializedName("_id")
    private Object idObj;
    
    private String rank;
    private String title;
    private String description;
    private String iconType;

    public String getId() {
        if (idObj instanceof String) return (String) idObj;
        if (idObj instanceof java.util.Map) {
            return (String) ((java.util.Map<?, ?>) idObj).get("$oid");
        }
        return null;
    }

    public String getRank() { return rank; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIconType() { return iconType; }
}
