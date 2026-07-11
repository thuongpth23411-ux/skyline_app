package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;

public class TicketResponse {
    @SerializedName("_id")
    private String id;
    private String bookingCode;
    private String seatId;
    @SerializedName("ticketStatus")
    private String status;
    private String ticketClass;
    private String ticketType;
    private String passengerName;
    private double totalAmount;
    
    @SerializedName("flightData")
    private Flight flight;

    public String getId() { return id; }
    public String getBookingCode() { return bookingCode; }
    public String getSeatId() { return seatId; }
    public String getStatus() { return status; }
    public String getTicketClass() { return ticketClass; }
    public String getPassengerName() { return passengerName; }
    public double getTotalAmount() { return totalAmount; }
    public String getTicketType() { return ticketType; }
    public Flight getFlight() { return flight; }
}
