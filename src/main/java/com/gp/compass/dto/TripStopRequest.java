package com.gp.compass.dto;

import com.gp.compass.entity.StopType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TripStopRequest(

        @NotNull
        StopType stopType,

        @Valid
        @NotNull
        AddressSnapshotRequest address

) {}