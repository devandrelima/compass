package com.gp.compass.repository;

import com.gp.compass.entity.Address;
import com.gp.compass.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    Optional<Address> findByIdAndClientUser(UUID id, User user);

}