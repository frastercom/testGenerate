package com.example.demo.dto;

public record RegistrationRequest(
        String username,
        String password,
        String displayName
) {
}
