package com.gp.compass.service;

import com.gp.compass.dto.*;
import com.gp.compass.entity.Address;
import com.gp.compass.entity.Client;
import com.gp.compass.entity.User;
import com.gp.compass.mapper.AddressMapper;
import com.gp.compass.mapper.ClientMapper;
import com.gp.compass.repository.AddressRepository;
import com.gp.compass.repository.ClientRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;


    private User authenticatedUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private Client findClientOwnedByUser(UUID clientId) {
        return clientRepository.findByIdAndUser(clientId, authenticatedUser())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
    }


    public List<ClientResponse> getAll() {
        return clientRepository.findAllByUser(authenticatedUser())
                .stream()
                .map(ClientMapper::toResponse)
                .toList();
    }

    @Transactional
    public ClientResponse create(ClientRequest dto) {
        Client client = ClientMapper.toEntity(dto);
        client.setUser(authenticatedUser());
        return ClientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public ClientResponse update(UUID id, ClientRequest dto) {
        Client client = findClientOwnedByUser(id);

        if (dto.name() != null) client.setName(dto.name());

        return ClientMapper.toResponse(clientRepository.save(client));
    }

    @Transactional
    public AddressResponse addAddress(UUID clientId, AddressRequest dto) {
        Client client = findClientOwnedByUser(clientId);

        Address address = AddressMapper.toEntity(dto, client);

        return AddressMapper.toResponse(addressRepository.save(address));
    }

    public ClientResponse getById(UUID id) {
        return ClientMapper.toResponse(findClientOwnedByUser(id));
    }

    @Transactional
    public void delete(UUID id) {
        clientRepository.delete(findClientOwnedByUser(id));
    }
}