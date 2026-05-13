package com.gp.compass.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gp.compass.dto.TripStopRequest;
import com.gp.compass.dto.TripStopResponse;
import com.gp.compass.dto.UpdateTripStopRequest;
import com.gp.compass.entity.Client;
import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.Trip;
import com.gp.compass.entity.TripStatus;
import com.gp.compass.entity.TripStop;
import com.gp.compass.entity.User;
import com.gp.compass.mapper.TripStopMapper;
import com.gp.compass.repository.ClientRepository;
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
    private final ClientRepository clientRepository;

    private User authenticatedUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    private Trip findTripOwnedByUser(UUID tripId) {
        return tripRepository.findByIdAndUser(tripId, authenticatedUser())
                .orElseThrow(() -> new EntityNotFoundException("Viagem não encontrada"));
    }

    private void assertNotFinished(Trip trip) {
        if (trip.getStatus() == TripStatus.FINISHED) {
            throw new IllegalStateException("Não é possível modificar uma viagem finalizada");
        }
    }

    private Client resolveClient(UUID clientId) {
        if (clientId == null) return null;
        return clientRepository.findByIdAndUser(clientId, authenticatedUser())
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
    }

    @Transactional
    public List<TripStopResponse> addStops(UUID tripId, List<TripStopRequest> dtos) {
        Trip trip = findTripOwnedByUser(tripId);
        assertNotFinished(trip);

        List<TripStop> currentStops =
                tripStopRepository.findAllByTripOrderBySequenceOrderAsc(trip);

        int nextOrder = currentStops.isEmpty()
                ? 0
                : currentStops.get(currentStops.size() - 1).getSequenceOrder() + 1;

        List<TripStop> newStops = new ArrayList<>();

        for (TripStopRequest dto : dtos) {
            newStops.add(TripStop.builder()
                    .trip(trip)
                    .sequenceOrder(nextOrder++)
                    .stopType(dto.stopType())
                    .priority(dto.priority() != null
                            ? dto.priority()
                            : StopPriority.NORMAL)
                    .client(resolveClient(dto.clientId()))
                    .embarque(TripStopMapper.toSnapshot(dto.embarque()))
                    .desembarque(TripStopMapper.toSnapshot(dto.desembarque()))
                    .build());
        }

        return tripStopRepository.saveAll(newStops).stream()
                .map(TripStopMapper::toResponse)
                .toList();
    }

    public List<TripStopResponse> getStops(UUID tripId, boolean somentePendentes) {
        Trip trip = findTripOwnedByUser(tripId);
        return tripStopRepository.findAllByTripOrderBySequenceOrderAsc(trip)
                .stream()
                .filter(s -> !somentePendentes || !isConcluida(s))
                .map(TripStopMapper::toResponse)
                .toList();
    }

    private boolean isConcluida(TripStop stop) {
        return stop.isEmbarqueChecked() &&
                (stop.getDesembarque() == null || stop.isDesembarqueChecked());
    }

    @Transactional
    public TripStopResponse updateStop(UUID tripId, UUID stopId, UpdateTripStopRequest dto) {
        Trip trip = findTripOwnedByUser(tripId);
        assertNotFinished(trip);

        TripStop stop = tripStopRepository.findById(stopId)
                .orElseThrow(() -> new EntityNotFoundException("Parada não encontrada"));

        if (!stop.getTrip().getId().equals(trip.getId())) {
            throw new EntityNotFoundException("Parada não pertence a esta viagem");
        }

        if (dto.stopType() != null) {
            stop.setStopType(dto.stopType());
        }

        if (dto.priority() != null) {
            stop.setPriority(dto.priority());
        }

        if (dto.clientId() != null) {
            stop.setClient(resolveClient(dto.clientId()));
        }

        if (dto.embarque() != null) {
            stop.setEmbarque(TripStopMapper.toSnapshot(dto.embarque()));
        }

        if (dto.desembarque() != null) {
            stop.setDesembarque(TripStopMapper.toSnapshot(dto.desembarque()));
        }

        return TripStopMapper.toResponse(tripStopRepository.save(stop));
    }

    @Transactional
    public TripStopResponse toggleEmbarqueCheck(UUID tripId, UUID stopId) {
        TripStop stop = findStopOwnedByTrip(tripId, stopId);
        assertNotFinished(stop.getTrip());
        stop.setEmbarqueChecked(!stop.isEmbarqueChecked());
        return TripStopMapper.toResponse(tripStopRepository.save(stop));
    }

    @Transactional
    public TripStopResponse toggleDesembarqueCheck(UUID tripId, UUID stopId) {
        TripStop stop = findStopOwnedByTrip(tripId, stopId);
        assertNotFinished(stop.getTrip());
        if (stop.getDesembarque() == null) {
            throw new IllegalStateException("Esta parada não possui desembarque definido");
        }
        stop.setDesembarqueChecked(!stop.isDesembarqueChecked());
        return TripStopMapper.toResponse(tripStopRepository.save(stop));
    }

    private TripStop findStopOwnedByTrip(UUID tripId, UUID stopId) {
        Trip trip = findTripOwnedByUser(tripId);
        TripStop stop = tripStopRepository.findById(stopId)
                .orElseThrow(() -> new EntityNotFoundException("Parada não encontrada"));
        if (!stop.getTrip().getId().equals(trip.getId())) {
            throw new EntityNotFoundException("Parada não pertence a esta viagem");
        }
        return stop;
    }

    @Transactional
    public TripStopResponse clearDesembarque(UUID tripId, UUID stopId) {
        TripStop stop = findStopOwnedByTrip(tripId, stopId);
        assertNotFinished(stop.getTrip());
        stop.setDesembarque(null);
        stop.setDesembarqueChecked(false);
        return TripStopMapper.toResponse(tripStopRepository.save(stop));
    }

    @Transactional
    public void deleteStops(UUID tripId, List<UUID> stopIds) {
        Trip trip = findTripOwnedByUser(tripId);
        assertNotFinished(trip);

        List<TripStop> stops =
                tripStopRepository.findAllByTripOrderBySequenceOrderAsc(trip);

        List<TripStop> toDelete = stops.stream()
                .filter(s -> stopIds.contains(s.getId()))
                .toList();

        if (toDelete.size() != stopIds.size()) {
            throw new EntityNotFoundException(
                    "Uma ou mais paradas não foram encontradas nesta viagem"
            );
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
