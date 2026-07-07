package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;

public class TicketResponse {
    @SerializedName("_id")
    private String id;
    private String bookingCode;
    private String seatNumber;
    private String ticketClass;
    private String status;
    private String passengerName;
    private double totalPrice;
    
    @SerializedName("flightId")
    private Flight flight;

    public String getId() { return id; }
    public String getBookingCode() { return bookingCode; }
    public String getSeatNumber() { return seatNumber; }
    public String getTicketClass() { return ticketClass; }
    public String getStatus() { return status; }
    public String getPassengerName() { return passengerName; }
    public double getTotalPrice() { return totalPrice; }
    public Flight getFlight() { return flight; }
}
