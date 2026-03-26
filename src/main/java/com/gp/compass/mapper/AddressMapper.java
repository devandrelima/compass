package com.gp.compass.mapper;

import com.gp.compass.dto.AddressRequest;
import com.gp.compass.dto.AddressResponse;
import com.gp.compass.dto.AddressUpdateRequest;
import com.gp.compass.entity.Address;
import com.gp.compass.entity.Client;

public class AddressMapper {


    public static Address toEntity(AddressRequest dto, Client client) {
        if (dto == null) return null;

        return Address.builder()
                .cep(dto.cep())
                .street(dto.street())
                .neighborhood(dto.neighborhood())
                .number(dto.number())
                .city(dto.city())
                .state(dto.state())
                .complement(dto.complement())
                .client(client)
                .build();
    }

    public static AddressResponse toResponse(Address entity) {
        if (entity == null) return null;

        return new AddressResponse(
                entity.getId(),
                entity.getCep(),
                entity.getStreet(),
                entity.getNeighborhood(),
                entity.getNumber(),
                entity.getCity(),
                entity.getState(),
                entity.getComplement(),
                entity.getClient() != null ? entity.getClient().getId() : null
        );
    }

    public static void updateEntity(Address entity, AddressUpdateRequest dto) {
        if (dto == null || entity == null) return;

        if (dto.cep() != null) entity.setCep(dto.cep());
        if (dto.street() != null) entity.setStreet(dto.street());
        if (dto.neighborhood() != null) entity.setNeighborhood(dto.neighborhood());
        if (dto.number() != null) entity.setNumber(dto.number());
        if (dto.city() != null) entity.setCity(dto.city());
        if (dto.state() != null) entity.setState(dto.state());
        if (dto.complement() != null) entity.setComplement(dto.complement());

    }

}