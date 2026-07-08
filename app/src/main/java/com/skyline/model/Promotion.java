package com.skyline.model;

import com.google.gson.annotations.SerializedName;

public class Promotion {
    @SerializedName("title")
    private String title;
    
    @SerializedName("endDate")
    private String date;
    
    @SerializedName("imageUrl")
    private String imageUrl;

    private int imageRes; // Dùng cho local fallback

    public Promotion(String title, String date, int imageRes) {
        this.title = title;
        this.date = date;
        this.imageRes = imageRes;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getImageUrl() { return imageUrl; }
    public int getImageRes() { return imageRes; }
}
