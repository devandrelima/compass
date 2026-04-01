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

        @Size(max = 150, message = "Rua deve ter menos de 150 caracteres")
        String street,

        @Size(max = 100, message = "Bairro deve ter menos de 100 caracteres")
        String neighborhood,

        @Size(max = 20, message = "Número deve ter menos de 20 caracteres")
        String number,

        @Size(max = 100, message = "Cidade deve ter menos de 100 caracteres")
        String city,

        @Size(min = 2, max = 2, message = "Estado deve ter 2 caracteres")
        String state,

        @Size(max = 150, message = "Complemento deve ter menos de 150 caracteres")
        String complement


) {

}
