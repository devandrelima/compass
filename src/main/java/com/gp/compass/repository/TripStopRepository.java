package com.gp.compass.repository;

import com.gp.compass.entity.Trip;
import com.gp.compass.entity.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripStopRepository extends JpaRepository<TripStop, UUID> {

    List<TripStop> findAllByTripOrderBySequenceOrderAsc(Trip trip);

    Optional<TripStop> findByIdAndTrip(UUID id, Trip trip);

    int countByTrip(Trip trip);
}
