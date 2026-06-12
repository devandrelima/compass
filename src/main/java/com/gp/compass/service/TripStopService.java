package com.gp.compass.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.gp.compass.dto.OptimizePreviewResponse;
import com.gp.compass.dto.TripStopRequest;
import com.gp.compass.dto.TripStopResponse;
import com.gp.compass.dto.UpdateTripStopRequest;
import com.gp.compass.entity.Client;
import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.Trip;
import com.gp.compass.entity.TripStatus;
import com.gp.compass.entity.TripStop;
import com.gp.compass.entity.TripStopSequenceSnapshot;
import com.gp.compass.entity.User;
import com.gp.compass.mapper.TripStopMapper;
import com.gp.compass.repository.ClientRepository;
import com.gp.compass.repository.TripRepository;
import com.gp.compass.repository.TripStopRepository;
import com.gp.compass.repository.TripStopSequenceSnapshotRepository;
import com.gp.compass.service.routing.Waypoint;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripStopService {

    private final TripStopRepository tripStopRepository;
    private final TripRepository tripRepository;
    private final ClientRepository clientRepository;
    private final RouteOptimizationService routeOptimizationService;
    private final TripStopSequenceSnapshotRepository sequenceSnapshotRepository;

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
                tripStopRepository.findAllByTripOrderByEmbarqueSequenceOrderAsc(trip);

        int nextOrder = currentStops.isEmpty()
                ? 0
                : currentStops.stream()
                        .mapToInt(s -> {
                            int max = s.getEmbarqueSequenceOrder();
                            if (s.getDesembarqueSequenceOrder() != null) {
                                max = Math.max(max, s.getDesembarqueSequenceOrder());
                            }
                            return max;
                        })
                        .max()
                        .orElse(-1) + 1;

        List<TripStop> newStops = new ArrayList<>();

        for (TripStopRequest dto : dtos) {
            newStops.add(TripStop.builder()
                    .trip(trip)
                    .embarqueSequenceOrder(nextOrder++)
                    .desembarqueSequenceOrder(null)
                    .stopType(dto.stopType())
                    .priority(dto.priority() != null ? dto.priority() : StopPriority.NORMAL)
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
        return tripStopRepository.findAllByTripOrderByEmbarqueSequenceOrderAsc(trip)
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

        if (dto.stopType() != null) stop.setStopType(dto.stopType());
        if (dto.priority() != null) stop.setPriority(dto.priority());
        if (dto.clientId() != null) stop.setClient(resolveClient(dto.clientId()));
        if (dto.embarque() != null) stop.setEmbarque(TripStopMapper.toSnapshot(dto.embarque()));
        if (dto.desembarque() != null) stop.setDesembarque(TripStopMapper.toSnapshot(dto.desembarque()));

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

    @Transactional
    public TripStopResponse clearDesembarque(UUID tripId, UUID stopId) {
        TripStop stop = findStopOwnedByTrip(tripId, stopId);
        assertNotFinished(stop.getTrip());
        stop.setDesembarque(null);
        stop.setDesembarqueChecked(false);
        stop.setDesembarqueSequenceOrder(null);
        return TripStopMapper.toResponse(tripStopRepository.save(stop));
    }

    @Transactional
    public void deleteStops(UUID tripId, List<UUID> stopIds) {
        Trip trip = findTripOwnedByUser(tripId);
        assertNotFinished(trip);

        List<TripStop> stops =
                tripStopRepository.findAllByTripOrderByEmbarqueSequenceOrderAsc(trip);

        List<TripStop> toDelete = stops.stream()
                .filter(s -> stopIds.contains(s.getId()))
                .toList();

        if (toDelete.size() != stopIds.size()) {
            throw new EntityNotFoundException("Uma ou mais paradas não foram encontradas nesta viagem");
        }

        tripStopRepository.deleteAll(toDelete);
    }

    private void assertReadyForOptimization(List<TripStop> stops) {
        if (stops.isEmpty()) {
            throw new IllegalStateException("A viagem não possui paradas para otimizar");
        }

        for (TripStop stop : stops) {
            if (stop.getEmbarque() == null
                    || stop.getEmbarque().getLat() == null
                    || stop.getEmbarque().getLng() == null) {
                throw new IllegalStateException(
                        "Todas as paradas precisam ter coordenadas de embarque para otimizar a rota.");
            }
            if (stop.getDesembarque() != null
                    && (stop.getDesembarque().getLat() == null || stop.getDesembarque().getLng() == null)) {
                throw new IllegalStateException(
                        "Todas as paradas com desembarque precisam ter coordenadas de desembarque para otimizar a rota.");
            }
        }
    }

    @Transactional
    public List<TripStopResponse> optimizeTrip(UUID tripId) {
        Trip trip = findTripOwnedByUser(tripId);
        assertNotFinished(trip);

        List<TripStop> stops = tripStopRepository.findAllByTripOrderByEmbarqueSequenceOrderAsc(trip);
        assertReadyForOptimization(stops);

        saveSequenceSnapshot(trip, stops);

        List<Waypoint> orderedWaypoints = routeOptimizationService.optimize(stops);

        Map<UUID, TripStop> stopById = stops.stream()
                .collect(Collectors.toMap(TripStop::getId, s -> s));

        for (int i = 0; i < orderedWaypoints.size(); i++) {
            Waypoint wp = orderedWaypoints.get(i);
            TripStop stop = stopById.get(wp.stopId());
            if (stop == null) continue;
            if (wp.isDesembarque()) {
                stop.setDesembarqueSequenceOrder(i);
            } else {
                stop.setEmbarqueSequenceOrder(i);
            }
        }

        return tripStopRepository.saveAll(stops).stream()
                .sorted(Comparator.comparingInt(TripStop::getEmbarqueSequenceOrder))
                .map(TripStopMapper::toResponse)
                .toList();
    }

    public OptimizePreviewResponse previewOptimize(UUID tripId) {
        Trip trip = findTripOwnedByUser(tripId);

        List<TripStop> stops = tripStopRepository.findAllByTripOrderByEmbarqueSequenceOrderAsc(trip);
        assertReadyForOptimization(stops);

        List<Waypoint> currentOrder = routeOptimizationService.buildCurrentOrder(stops);
        List<Waypoint> optimizedOrder = routeOptimizationService.optimize(stops);

        double beforeMeters = routeOptimizationService.totalDistanceMeters(currentOrder);
        double afterMeters = routeOptimizationService.totalDistanceMeters(optimizedOrder);

        return new OptimizePreviewResponse(beforeMeters / 1000.0, afterMeters / 1000.0);
    }

    private void saveSequenceSnapshot(Trip trip, List<TripStop> stops) {
        sequenceSnapshotRepository.deleteAllByTrip(trip);

        List<TripStopSequenceSnapshot> snapshots = stops.stream()
                .map(stop -> TripStopSequenceSnapshot.builder()
                        .trip(trip)
                        .stopId(stop.getId())
                        .embarqueSequenceOrder(stop.getEmbarqueSequenceOrder())
                        .desembarqueSequenceOrder(stop.getDesembarqueSequenceOrder())
                        .build())
                .toList();

        sequenceSnapshotRepository.saveAll(snapshots);
    }

    @Transactional
    public List<TripStopResponse> revertOptimize(UUID tripId) {
        Trip trip = findTripOwnedByUser(tripId);
        assertNotFinished(trip);

        List<TripStopSequenceSnapshot> snapshots = sequenceSnapshotRepository.findAllByTrip(trip);
        if (snapshots.isEmpty()) {
            throw new IllegalStateException("Não há otimização para desfazer nesta viagem");
        }

        List<TripStop> stops = tripStopRepository.findAllByTripOrderByEmbarqueSequenceOrderAsc(trip);

        Set<UUID> stopIds = stops.stream().map(TripStop::getId).collect(Collectors.toSet());
        Set<UUID> snapshotStopIds = snapshots.stream().map(TripStopSequenceSnapshot::getStopId).collect(Collectors.toSet());
        if (!stopIds.equals(snapshotStopIds)) {
            sequenceSnapshotRepository.deleteAllByTrip(trip);
            throw new IllegalStateException("As paradas desta viagem mudaram desde a última otimização");
        }

        Map<UUID, TripStop> stopById = stops.stream()
                .collect(Collectors.toMap(TripStop::getId, s -> s));

        for (TripStopSequenceSnapshot snapshot : snapshots) {
            TripStop stop = stopById.get(snapshot.getStopId());
            stop.setEmbarqueSequenceOrder(snapshot.getEmbarqueSequenceOrder());
            stop.setDesembarqueSequenceOrder(snapshot.getDesembarqueSequenceOrder());
        }

        sequenceSnapshotRepository.deleteAllByTrip(trip);

        return tripStopRepository.saveAll(stops).stream()
                .sorted(Comparator.comparingInt(TripStop::getEmbarqueSequenceOrder))
                .map(TripStopMapper::toResponse)
                .toList();
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
}
