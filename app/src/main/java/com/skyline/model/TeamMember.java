package com.skyline.model;

public class TeamMember {
    private String name;
    private int imageRes;

    public TeamMember(String name, int imageRes) {
        this.name = name;
        this.imageRes = imageRes;
    }

    public String getName() { return name; }
    public int getImageRes() { return imageRes; }
}
