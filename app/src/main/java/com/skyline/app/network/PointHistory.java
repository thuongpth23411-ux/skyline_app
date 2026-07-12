package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PointHistory implements Serializable {
    @SerializedName("_id")
    private String id;
    private String userId;
    private int points;
    private String type; // EARN, REDEEM, REVOKE
    private double amount;
    private String description;
    private String bookingCode;
    private String date;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public int getPoints() { return points; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getBookingCode() { return bookingCode; }
    public String getDate() { return date; }
}
