package com.skyline.app.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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
    
    // Dữ liệu động từ Backend
    private List<PriceOption> priceOptions;
    private List<String> occupiedSeats;

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
    
    public List<PriceOption> getPriceOptions() { return priceOptions; }
    public List<String> getOccupiedSeats() { return occupiedSeats; }

    public static class PriceOption {
        private String type; // "ECONOMY", "BUSINESS"
        private double price;
        private double totalPrice; // Backend support cả 2 trường này

        public String getType() { return type; }
        public double getPrice() { return totalPrice > 0 ? totalPrice : price; }
    }
}
