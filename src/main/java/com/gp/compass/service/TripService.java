package com.gp.compass.service;

import com.gp.compass.dto.TripRequest;
import com.gp.compass.dto.TripResponse;
import com.gp.compass.entity.Client;
import com.gp.compass.entity.Trip;
import com.gp.compass.entity.TripStatus;
import com.gp.compass.entity.TripStop;
import com.gp.compass.entity.User;
import com.gp.compass.mapper.TripMapper;
import com.gp.compass.repository.ClientRepository;
import com.gp.compass.repository.TripRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripService {

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

    public TripResponse create(TripRequest dto) {
        User user = authenticatedUser();
        Trip trip = TripMapper.toEntity(dto);
        trip.setUser(user);

        for (int i = 0; i < dto.stops().size(); i++) {
            UUID clientId = dto.stops().get(i).clientId();
            if (clientId != null) {
                Client client = clientRepository.findByIdAndUser(clientId, user)
                        .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
                trip.getStops().get(i).setClient(client);
            }
        }

        return TripMapper.toResponse(tripRepository.save(trip));
    }

    public TripResponse getById(UUID id) {
        return TripMapper.toResponse(findTripOwnedByUser(id));
    }

    public List<TripResponse> getAll() {
        return tripRepository.findAllByUser(authenticatedUser())
                .stream()
                .map(TripMapper::toResponse)
                .toList();
    }

    public TripResponse updateStatus(UUID id, TripStatus status) {
        Trip trip = findTripOwnedByUser(id);
        trip.setStatus(status);
        return TripMapper.toResponse(tripRepository.save(trip));
    }

    public void delete(UUID id) {
        tripRepository.delete(findTripOwnedByUser(id));
    }
}
