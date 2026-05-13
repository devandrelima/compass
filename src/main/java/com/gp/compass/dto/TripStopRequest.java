package com.gp.compass.dto;

import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.StopType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TripStopRequest(

        @NotNull
        StopType stopType,

        StopPriority priority,

        UUID clientId,

        @Valid
        @NotNull
        AddressSnapshotRequest embarque,

        @Valid
        AddressSnapshotRequest desembarque

) {}