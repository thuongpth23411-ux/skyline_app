package com.skyline.app.network;

import java.util.ArrayList;
import java.util.List;

public class GroqRequest {
    private String model;
    private List<Message> messages;

    public GroqRequest(String model, String systemInstruction, String userText) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("system", systemInstruction));
        this.messages.add(new Message("user", userText));
    }

    public static class Message {
        private String role;
        private String content;
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}
