package com.skyline.model;

import com.google.gson.annotations.SerializedName;

public class Destination {
    @SerializedName("destination")
    private String country;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("thumbnailUrl")
    private String imageUrl;

    @SerializedName("slug")
    private String blogSlug;

    private int imageRes;

    public Destination(String country, String title, int imageRes) {
        this.country = country;
        this.title = title;
        this.imageRes = imageRes;
    }

    public Destination(String country, String title, int imageRes, String blogSlug) {
        this.country = country;
        this.title = title;
        this.imageRes = imageRes;
        this.blogSlug = blogSlug;
    }

    public String getCountry() { return country; }
    public String getTitle() { return title; }
    public int getImageRes() { return imageRes; }
    public String getImageUrl() { return imageUrl; }
    public String getBlogSlug() { return blogSlug; }
}
