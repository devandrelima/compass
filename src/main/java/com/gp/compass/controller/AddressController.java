package com.gp.compass.controller;

import com.gp.compass.dto.AddressResponse;
import com.gp.compass.dto.AddressUpdateRequest;
import com.gp.compass.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService service;

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> getById(@PathVariable UUID id) {
        AddressResponse response = service.getById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AddressResponse> patch(
            @PathVariable UUID id,
            @RequestBody @Valid AddressUpdateRequest dto
    ) {

        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}