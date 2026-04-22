package com.gp.compass.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gp.compass.dto.TripStopRequest;
import com.gp.compass.dto.TripStopResponse;
import com.gp.compass.dto.UpdateTripStopRequest;
import com.gp.compass.entity.Trip;
import com.gp.compass.entity.TripStop;
import com.gp.compass.entity.User;
import com.gp.compass.mapper.TripStopMapper;
import com.gp.compass.repository.TripRepository;
import com.gp.compass.repository.TripStopRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripStopService {

    private final TripStopRepository tripStopRepository;
    private final TripRepository tripRepository;

    private User authenticatedUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private Trip findTripOwnedByUser(UUID tripId) {
        return tripRepository.findByIdAndUser(tripId, authenticatedUser())
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
    }

    @Transactional
    public List<TripStopResponse> addStops(UUID tripId, List<TripStopRequest> dtos) {
        Trip trip = findTripOwnedByUser(tripId);
        List<TripStop> currentStops = tripStopRepository.findAllByTripOrderBySequenceOrderAsc(trip);

        // Desloca o destino N posições para abrir espaço para todos os novos
        TripStop destination = currentStops.get(currentStops.size() - 1);
        destination.setSequenceOrder(destination.getSequenceOrder() + dtos.size());
        tripStopRepository.save(destination);

        int nextOrder = destination.getSequenceOrder() - dtos.size();
        List<TripStop> newStops = new ArrayList<>();
        for (TripStopRequest dto : dtos) {
            newStops.add(TripStop.builder()
                    .trip(trip)
                    .sequenceOrder(nextOrder++)
                    .stopType(dto.stopType())
                    .address(TripStopMapper.toSnapshot(dto.address()))
                    .build());
        }

        return tripStopRepository.saveAll(newStops).stream()
                .map(TripStopMapper::toResponse)
                .toList();
    }

    public List<TripStopResponse> getStops(UUID tripId) {
        Trip trip = findTripOwnedByUser(tripId);
        return tripStopRepository.findAllByTripOrderBySequenceOrderAsc(trip)
                .stream()
                .map(TripStopMapper::toResponse)
                .toList();
    }

    @Transactional
    public TripStopResponse updateStop(UUID tripId, UUID stopId, UpdateTripStopRequest dto) {

        Trip trip = findTripOwnedByUser(tripId);

        TripStop stop = tripStopRepository.findById(stopId)
                .orElseThrow(() -> new EntityNotFoundException("Parada não encontrada"));

        if (!stop.getTrip().getId().equals(trip.getId())) {
            throw new EntityNotFoundException("Parada não pertence a esta viagem");
        }

        stop.setStopType(dto.stopType());

        return TripStopMapper.toResponse(tripStopRepository.save(stop));
    }

    @Transactional
    public void deleteStops(UUID tripId, List<UUID> stopIds) {
        Trip trip = findTripOwnedByUser(tripId);
        List<TripStop> stops = tripStopRepository.findAllByTripOrderBySequenceOrderAsc(trip);

        Set<Integer> protectedOrders = Set.of(
                stops.get(0).getSequenceOrder(),
                stops.get(stops.size() - 1).getSequenceOrder()
        );

        List<TripStop> toDelete = stops.stream()
                .filter(s -> stopIds.contains(s.getId()))
                .toList();

        if (toDelete.size() != stopIds.size()) {
            throw new EntityNotFoundException("Uma ou mais paradas não foram encontradas nesta viagem");
        }

        boolean hasProtected = toDelete.stream()
                .anyMatch(s -> protectedOrders.contains(s.getSequenceOrder()));
        if (hasProtected) {
            throw new IllegalStateException("Não é possível remover a parada de início ou fim da viagem");
        }

        tripStopRepository.deleteAll(toDelete);
    }

    private void validateNotFirstOrLast(TripStop stop, List<TripStop> stops) {
        boolean isFirst = stop.getSequenceOrder() == stops.get(0).getSequenceOrder();
        boolean isLast = stop.getSequenceOrder() == stops.get(stops.size() - 1).getSequenceOrder();
        if (isFirst || isLast) {
            throw new IllegalStateException("Não é possível remover a parada de início ou fim da viagem");
        }
    }
}
