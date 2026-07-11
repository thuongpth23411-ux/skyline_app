package com.skyline.model;

public class Destination {
    private String country;
    private String title;
    private int imageRes;
    private String imageUrl;

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

    public String getCountry() { return country; }
    public String getTitle() { return title; }
    public int getImageRes() { return imageRes; }
    public String getImageUrl() { return imageUrl; }
}
