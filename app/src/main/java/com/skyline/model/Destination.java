package com.skyline.model;

import com.google.gson.annotations.SerializedName;

public class Destination {
    @SerializedName("country")
    private String country;
    
    @SerializedName("title")
    private String title;
    
    private int imageRes;
    
    @SerializedName("imageUrl")
    private String imageUrl;
    
    @SerializedName("image")
    private String image;

    @SerializedName("thumbnailUrl")
    private String thumbnailUrl;
    
    @SerializedName("slug")
    private String blogSlug;

    @SerializedName("destination")
    private String destination;

    public Destination(String country, String title, int imageRes) {
        this.country = country;
        this.title = title;
        this.imageRes = imageRes;
    }

    public Destination(String country, String title, String imageUrl) {
        this.country = country;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public Destination(String country, String title, String imageUrl, String blogSlug) {
        this.country = country;
        this.title = title;
        this.imageUrl = imageUrl;
        this.blogSlug = blogSlug;
    }

    public String getCountry() { 
        if (country != null && !country.isEmpty()) return country;
        return destination;
    }
    public String getTitle() { return title; }
    public int getImageRes() { return imageRes; }
    public String getImageUrl() { 
        if (imageUrl != null && !imageUrl.isEmpty()) return imageUrl;
        if (image != null && !image.isEmpty()) return image;
        return thumbnailUrl;
    }
    public String getBlogSlug() { return blogSlug; }
    public void setBlogSlug(String blogSlug) { this.blogSlug = blogSlug; }

    public String getFullImageUrl() {
        return com.skyline.app.network.RetrofitClient.formatUrl(getImageUrl());
    }
}
