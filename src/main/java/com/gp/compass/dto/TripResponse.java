package com.gp.compass.dto;

import com.gp.compass.entity.TripStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TripResponse(
        UUID id,
        TripStatus status,
        List<TripStopResponse> stops,
        LocalDateTime createdAt
) {}