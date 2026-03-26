package com.gp.compass.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record TripRequest(

        @NotNull(message = "Endereço de origem é obrigatório")
        @Valid
        AddressSnapshotRequest originAddress,

        @NotNull(message = "Endereço de destino é obrigatório")
        @Valid
        AddressSnapshotRequest destinationAddress
) {}