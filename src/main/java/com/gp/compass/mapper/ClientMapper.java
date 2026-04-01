package com.gp.compass.mapper;

import com.gp.compass.dto.ClientRequest;
import com.gp.compass.dto.ClientResponse;
import com.gp.compass.dto.AddressResponse;
import com.gp.compass.entity.Client;
import com.gp.compass.entity.Address;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClientMapper {

    public static Client toEntity(ClientRequest dto) {
        if (dto == null) return null;

        Client client = Client.builder()
                .name(dto.name())
                .build();

        if (dto.addresses() != null && !dto.addresses().isEmpty()) {
            List<Address> addresses = dto.addresses().stream()
                    .map(addressDTO -> {
                        Address address = AddressMapper.toEntity(addressDTO, client);
                        address.setClient(client);
                        return address;
                    })
                    .collect(Collectors.toList());

            client.setAddresses(addresses);
        }

        return client;
    }


    public static ClientResponse toResponse(Client entity) {
        if (entity == null) return null;

        List<AddressResponse> addresses = entity.getAddresses() == null
                ? List.of()
                : entity.getAddresses()
                  .stream()
                  .map(AddressMapper::toResponse)
                  .toList();
        return new ClientResponse(
                entity.getId(),
                entity.getName(),
                addresses
        );
    }

}