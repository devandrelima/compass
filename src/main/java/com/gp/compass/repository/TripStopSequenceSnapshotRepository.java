package com.gp.compass.repository;

import com.gp.compass.entity.Trip;
import com.gp.compass.entity.TripStopSequenceSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripStopSequenceSnapshotRepository extends JpaRepository<TripStopSequenceSnapshot, java.util.UUID> {

    List<TripStopSequenceSnapshot> findAllByTrip(Trip trip);

    void deleteAllByTrip(Trip trip);
}
