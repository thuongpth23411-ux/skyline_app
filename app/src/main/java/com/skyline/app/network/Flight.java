package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Flight {
    private String flightId;
    private String flightNumber;
    private Airline airline;
    private Airport departureAirport;
    private Airport arrivalAirport;
    private String departureAt;
    private String arrivalAt;
    private int durationMinutes;
    private double basePrice;
    private String aircraftModel;

    public String getId() { return flightId; }
    public String getFlightNumber() { return flightNumber; }
    public Airline getAirline() { return airline; }
    public Airport getDepartureAirport() { return departureAirport; }
    public Airport getArrivalAirport() { return arrivalAirport; }
    public String getDepartureAt() { return departureAt; }
    public String getArrivalAt() { return arrivalAt; }
    public int getDuration() { return durationMinutes; }
    public double getBasePrice() { return basePrice; }
    public String getAircraftModel() { return aircraftModel; }
}
