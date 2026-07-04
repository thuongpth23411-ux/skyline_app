package com.skyline.model;

public class Promotion {
    private String title;
    private String date;
    private int imageRes;

    public Promotion(String title, String date, int imageRes) {
        this.title = title;
        this.date = date;
        this.imageRes = imageRes;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getImageRes() { return imageRes; }
}
