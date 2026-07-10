package com.skyline.model;

public class Ticket {
    private final String day;
    private final String monthYear;
    private final String flightClass;
    private final String flightNo;
    private final String originCode;
    private final String originCity;
    private final String destCode;
    private final String destCity;
    private final String time;
    private final String seat;
    private final String ticketType;
    private final double totalAmount;
    private final String passengerName;

    public Ticket(String day, String monthYear, String flightClass, String flightNo, String originCode, String originCity, String destCode, String destCity, String time, String seat, double totalAmount, String passengerName, String ticketType) {
        this.day = day;
        this.monthYear = monthYear;
        this.flightClass = flightClass;
        this.flightNo = flightNo;
        this.originCode = originCode;
        this.originCity = originCity;
        this.destCode = destCode;
        this.destCity = destCity;
        this.time = time;
        this.seat = seat;
        this.totalAmount = totalAmount;
        this.passengerName = passengerName;
        this.ticketType = ticketType;
    }

    public String getDay() { return day; }
    public String getMonthYear() { return monthYear; }
    public String getFlightClass() { return flightClass; }
    public String getFlightNo() { return flightNo; }
    public String getOriginCode() { return originCode; }
    public String getOriginCity() { return originCity; }
    public String getDestCode() { return destCode; }
    public String getDestCity() { return destCity; }
    public String getTime() { return time; }
    public String getSeat() { return seat; }
    public double getTotalAmount() { return totalAmount; }
    public String getPassengerName() { return passengerName; }
    public String getTicketType() { return ticketType; }
}
