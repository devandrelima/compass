package com.gp.compass.dto;

import com.gp.compass.entity.TripStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TripResponse(
        UUID id,
        AddressSnapshotResponse originAddress,
        AddressSnapshotResponse destinationAddress,
        TripStatus status,
        LocalDateTime createdAt
) {}