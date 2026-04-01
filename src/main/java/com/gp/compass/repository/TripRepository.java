package com.gp.compass.repository;

import com.gp.compass.entity.Trip;
import com.gp.compass.entity.TripStatus;
import com.gp.compass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    Optional<Trip> findByIdAndUser(UUID id, User user);
    List<Trip> findAllByUser(User user);

    List<Trip> findAllByStatus(TripStatus status);
}