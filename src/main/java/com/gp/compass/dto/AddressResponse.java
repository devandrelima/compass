package com.gp.compass.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AddressResponse(
        UUID id,

        String cep,

        String street,

        String neighborhood,

        String number,

        String city,

        String state,

        String complement,

        UUID client_id
) {
}
