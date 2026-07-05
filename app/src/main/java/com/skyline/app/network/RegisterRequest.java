package com.skyline.app.network;

public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String phone;
    private String cccd;
    private String passport;
    private String dob;
    private String country;
    private String title;
    private String address;

    // Full constructor for detailed info
    public RegisterRequest(String email, String password, String name, String phone, 
                           String cccd, String passport, String dob, String country, 
                           String title, String address) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.cccd = cccd;
        this.passport = passport;
        this.dob = dob;
        this.country = country;
        this.title = title;
        this.address = address;
    }

    // Constructor for Skip functionality
    public RegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters (Required for GSON serialization if fields are private, though GSON uses reflection)
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCccd() { return cccd; }
    public String getPassport() { return passport; }
    public String getDob() { return dob; }
    public String getCountry() { return country; }
    public String getTitle() { return title; }
    public String getAddress() { return address; }
}
