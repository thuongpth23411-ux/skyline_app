package com.skyline.app.network;

public class RegisterRequest {
    private String email;
    private String password;
    private String name;
    private String phone;

    public RegisterRequest(String email, String password, String name, String phone) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
    }

    public RegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
