package com.gp.compass.dto;

public record AddressSnapshotResponse(
        String cep,
        String street,
        String neighborhood,
        String number,
        String city,
        String state,
        String complement
) {}