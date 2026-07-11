package com.skyline.model;

import java.io.Serializable;

public class Notification implements Serializable {
    private String id;
    private String title;
    private String content;
    private String time;
    private boolean isRead;
    private String type;
    private String targetData;

    public Notification(String id, String title, String content, String time, String type, String targetData) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.time = time;
        this.type = type;
        this.targetData = targetData;
        this.isRead = false;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getTime() { return time; }
    public String getType() { return type; }
    public String getTargetData() { return targetData; }
}
