package com.skyline.model;

public class Notification {
    private String id;
    private String title;
    private String content;
    private String time;
    private boolean isRead;

    public Notification(String id, String title, String content, String time) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.time = time;
        this.isRead = false;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTime() { return time; }
}
