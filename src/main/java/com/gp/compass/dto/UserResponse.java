package com.gp.compass.dto;

public record UserResponse(
        String id,
        String name,
        String email,
        String role
) {}
