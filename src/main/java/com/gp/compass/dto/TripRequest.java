package com.gp.compass.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TripRequest(

        @NotBlank(message = "O título da viagem é obrigatório")
        @Size(max = 100, message = "O título não pode ter mais de 100 caracteres")
        String title,

        @NotNull(message = "A lista de paradas é obrigatória")
        @Size(min = 2, message = "A viagem deve ter no mínimo 2 paradas (início e fim)")
        @Valid
        List<AddressSnapshotRequest> stops

) {}