package com.gp.compass.dto;

import java.util.UUID;

public record TripStopResponse(
        UUID id,
        int sequenceOrder,
        AddressSnapshotResponse address
) {}
