package com.gp.compass.controller;

import com.gp.compass.dto.*;
import com.gp.compass.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;


    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }


    @PostMapping
    public ResponseEntity<ClientResponse> create(@RequestBody @Valid ClientRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable UUID id, @RequestBody @Valid ClientRequest dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<AddressResponse> addAddress(@PathVariable UUID id, @Valid @RequestBody AddressRequest dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAddress(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}