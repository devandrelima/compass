package com.gp.compass.dto;

import com.gp.compass.entity.Address;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ClientRequest(
        @NotBlank
        String name,

        List<AddressRequest> addresses
) {
}
