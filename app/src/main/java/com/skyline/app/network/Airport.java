package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;

public class Airport {
    @SerializedName("airportId")
    private String id;
    private String code;
    @SerializedName("airportName")
    private String name;
    private String city;
    private String country;

    public String getId() { return id; }
    public String getCode() { return id; } // Using id as code as per your notes
    public String getName() { return name; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
}
