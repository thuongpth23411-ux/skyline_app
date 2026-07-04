package com.skyline.model;

public class Experience {
    private String tag;
    private String title;
    private String description;
    private int imageRes;

    public Experience(String tag, String title, String description, int imageRes) {
        this.tag = tag;
        this.title = title;
        this.description = description;
        this.imageRes = imageRes;
    }

    public String getTag() { return tag; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getImageRes() { return imageRes; }
}
