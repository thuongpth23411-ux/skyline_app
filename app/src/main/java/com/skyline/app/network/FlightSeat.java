package com.skyline.app.network;

public class FlightSeat {
    private String seatId;
    private String seatNumber;
    private int rowNumber;
    private String seatColumn;
    private String cabinClass;
    private String seatStatus; // "AVAILABLE", "OCCUPIED"

    public String getSeatNumber() { return seatNumber; }
    public int getRowNumber() { return rowNumber; }
    public String getSeatColumn() { return seatColumn; }
    public String getCabinClass() { return cabinClass; }
    public String getSeatStatus() { return seatStatus; }
}