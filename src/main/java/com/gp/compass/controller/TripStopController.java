package com.gp.compass.controller;

import com.gp.compass.dto.AddressSnapshotRequest;
import com.gp.compass.dto.TripStopRequest;
import com.gp.compass.dto.TripStopResponse;
import com.gp.compass.dto.UpdateTripStopRequest;
import com.gp.compass.service.TripStopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{tripId}/stops")
@RequiredArgsConstructor
public class TripStopController {

    private final TripStopService service;

    @GetMapping
    public ResponseEntity<List<TripStopResponse>> getStops(
            @PathVariable UUID tripId,
            @RequestParam(defaultValue = "false") boolean somentePendentes
    ) {
        return ResponseEntity.ok(service.getStops(tripId, somentePendentes));
    }

    @PostMapping
    public ResponseEntity<List<TripStopResponse>> addStops(
            @PathVariable UUID tripId,
            @RequestBody @Valid List<TripStopRequest> dtos
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addStops(tripId, dtos));
    }

    @PatchMapping("/{stopId}")
    public ResponseEntity<TripStopResponse> updateStop(
            @PathVariable UUID tripId,
            @PathVariable UUID stopId,
            @RequestBody @Valid UpdateTripStopRequest dto
    ) {
        return ResponseEntity.ok(service.updateStop(tripId, stopId, dto));
    }

    @PatchMapping("/{stopId}/embarque/check")
    public ResponseEntity<TripStopResponse> toggleEmbarqueCheck(
            @PathVariable UUID tripId,
            @PathVariable UUID stopId
    ) {
        return ResponseEntity.ok(service.toggleEmbarqueCheck(tripId, stopId));
    }

    @PatchMapping("/{stopId}/desembarque/check")
    public ResponseEntity<TripStopResponse> toggleDesembarqueCheck(
            @PathVariable UUID tripId,
            @PathVariable UUID stopId
    ) {
        return ResponseEntity.ok(service.toggleDesembarqueCheck(tripId, stopId));
    }

    @DeleteMapping("/{stopId}/desembarque")
    public ResponseEntity<TripStopResponse> clearDesembarque(
            @PathVariable UUID tripId,
            @PathVariable UUID stopId
    ) {
        return ResponseEntity.ok(service.clearDesembarque(tripId, stopId));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteStops(
            @PathVariable UUID tripId,
            @RequestBody List<UUID> stopIds
    ) {
        service.deleteStops(tripId, stopIds);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/optimize")
    public ResponseEntity<List<TripStopResponse>> optimize(@PathVariable UUID tripId) {
        return ResponseEntity.ok(service.optimizeTrip(tripId));
    }
}
