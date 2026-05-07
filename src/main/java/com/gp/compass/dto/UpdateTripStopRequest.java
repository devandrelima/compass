package com.gp.compass.dto;

import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.StopType;

import jakarta.validation.Valid;

public record UpdateTripStopRequest(

        StopType stopType,
        StopPriority priority,

        @Valid
        AddressSnapshotRequest embarque,

        @Valid
        AddressSnapshotRequest desembarque

) {}