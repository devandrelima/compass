package com.gp.compass.service;

import com.gp.compass.dto.AddressResponse;
import com.gp.compass.dto.AddressUpdateRequest;
import com.gp.compass.entity.Address;
import com.gp.compass.entity.User;
import com.gp.compass.mapper.AddressMapper;
import com.gp.compass.repository.AddressRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;


    private User authenticatedUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private Address findAddressOwnedByUser(UUID addressId) {
        return addressRepository.findByIdAndClientUser(addressId, authenticatedUser())
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado"));
    }

    public AddressResponse getById(UUID id) {
        return AddressMapper.toResponse(findAddressOwnedByUser(id));
    }

    @Transactional
    public AddressResponse update(UUID id, AddressUpdateRequest dto) {
        Address address = findAddressOwnedByUser(id);

        AddressMapper.updateEntity(address, dto);

        return AddressMapper.toResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(UUID id) {
        addressRepository.delete(findAddressOwnedByUser(id));
    }
}