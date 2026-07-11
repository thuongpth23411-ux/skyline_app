package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;

public class PassengerDirectory {
    @SerializedName("_id")
    private String id;
    
    private String userId;
    
    @SerializedName("passengerName")
    private String passengerName;
    
    @SerializedName("passengerPhone")
    private String passengerPhone;
    
    @SerializedName("passengerCccd")
    private String passengerCccd;
    
    @SerializedName("passengerDob")
    private String passengerDob;
    
    @SerializedName("passengerEmail")
    private String passengerEmail;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getPassengerName() { return passengerName; }
    public String getPassengerPhone() { return passengerPhone; }
    public String getPassengerCccd() { return passengerCccd; }
    public String getPassengerDob() { return passengerDob; }
    public String getPassengerEmail() { return passengerEmail; }
}
