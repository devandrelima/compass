package com.gp.compass.dto;

import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.StopType;

import jakarta.validation.Valid;

import java.util.UUID;

public record UpdateTripStopRequest(

        StopType stopType,
        StopPriority priority,
        UUID clientId,

        @Valid
        AddressSnapshotRequest embarque,

        @Valid
        AddressSnapshotRequest desembarque

) {}