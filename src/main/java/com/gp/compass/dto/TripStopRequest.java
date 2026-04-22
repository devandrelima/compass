package com.gp.compass.dto;

import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.StopType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TripStopRequest(

        @NotNull
        StopType stopType,

        StopPriority priority,

        @Valid
        @NotNull
        AddressSnapshotRequest address

) {}