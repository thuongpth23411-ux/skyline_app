package com.skyline.model;

public class Ticket {
    private String day;
    private String monthYear;
    private String flightClass;
    private String flightNo;
    private String originCode;
    private String originCity;
    private String destCode;
    private String destCity;
    private String time;
    private String seat;
    private double totalAmount;
    private String passengerName;

    public Ticket(String day, String monthYear, String flightClass, String flightNo, String originCode, String originCity, String destCode, String destCity, String time, String seat, double totalAmount, String passengerName) {
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
}
