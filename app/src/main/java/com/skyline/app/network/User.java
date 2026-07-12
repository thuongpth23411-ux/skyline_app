package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("_id")
    private String id;
    
    private String email;
    
    @SerializedName("fullName")
    private String fullName;
    
    private String phone;
    private String dob;
    private String gender;
    private String passport;
    private String cccd;
    private String country;
    private String title;
    private String rank;
    
    @SerializedName("skyPoints")
    private int skyPoints;
    
    private String memberCode;
    
    @SerializedName("avatarUrl")
    private String avatarUrl;
    
    @SerializedName("createdAt")
    private String createdAt;

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return fullName; }
    public String getPhone() { return phone; }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getPassport() { return passport; }
    public String getCccd() { return cccd; }
    public String getCountry() { return country; }
    public String getTitle() { return title; }
    public String getRank() { return rank; }
    public int getSkyPoints() { return skyPoints; }
    public String getMemberCode() { return memberCode; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getJoinDate() { return createdAt; }
}
