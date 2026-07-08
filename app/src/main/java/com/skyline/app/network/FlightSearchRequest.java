package com.skyline.app.network;

public class FlightSearchRequest {
    private String fromCode;
    private String toCode;
    private String date; // YYYY-MM-DD

    public FlightSearchRequest(String fromCode, String toCode, String date) {
        this.fromCode = fromCode;
        this.toCode = toCode;
        this.date = date;
    }
}
