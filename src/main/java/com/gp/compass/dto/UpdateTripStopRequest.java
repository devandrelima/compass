package com.gp.compass.dto;

import com.gp.compass.entity.StopType;

import jakarta.validation.constraints.NotNull;

public record UpdateTripStopRequest(

        @NotNull
        StopType stopType

) {}