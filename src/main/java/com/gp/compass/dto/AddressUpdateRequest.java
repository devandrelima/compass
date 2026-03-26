package com.gp.compass.dto;

import com.gp.compass.entity.Client;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record AddressUpdateRequest(

        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
        String cep,

        String street,

        String neighborhood,

        String number,

        String city,

        @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
        String state,

        String complement


) {

}
