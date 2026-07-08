package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Airline implements Serializable {
    @SerializedName("airlineId")
    private String id;
    private String code;
    @SerializedName("airlineName")
    private String name;
    private String logoUrl;

    public String getId() { return id; }
    public String getCode() { return id; }
    public String getName() { return name; }
    public String getLogo() { return logoUrl; }
}
