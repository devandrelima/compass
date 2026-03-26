package com.gp.compass.repository;

import com.gp.compass.entity.Client;
import com.gp.compass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByIdAndUser(UUID id, User user);

    List<Client> findAllByUser(User user);
}