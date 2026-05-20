package com.gp.compass.dto;

import com.gp.compass.entity.StopPriority;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OptimizeStopInput(

        @NotNull
        UUID id,

        @NotNull
        Double lat,

        @NotNull
        Double lng,

        @NotNull
        String clientName,

        @NotNull
        StopPriority priority

) {}
