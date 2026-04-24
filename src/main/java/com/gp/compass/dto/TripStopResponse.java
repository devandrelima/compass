package com.gp.compass.dto;

import java.util.UUID;

import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.StopType;

public record TripStopResponse(
        UUID id,
        int sequenceOrder,
        StopType stopType,
        StopPriority priority,
        AddressSnapshotResponse embarque,
        AddressSnapshotResponse desembarque,
        boolean embarqueChecked,
        boolean desembarqueChecked,
        boolean concluida
) {}