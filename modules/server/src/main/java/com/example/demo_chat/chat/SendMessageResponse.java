package com.example.demo_chat.chat;

/** Response body for a sent message: the assistant's reply and the resulting dialogue status. */
public record SendMessageResponse(String reply, String status) {}
