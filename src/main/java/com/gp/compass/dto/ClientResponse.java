package com.gp.compass.dto;

import java.util.List;
import java.util.UUID;

public record ClientResponse(
        UUID id,
        String name,

        List<AddressResponse> addresses
) {
}
