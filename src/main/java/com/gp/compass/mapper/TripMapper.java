package com.gp.compass.mapper;

import com.gp.compass.dto.AddressSnapshotRequest;
import com.gp.compass.dto.AddressSnapshotResponse;
import com.gp.compass.dto.TripRequest;
import com.gp.compass.dto.TripResponse;
import com.gp.compass.entity.AddressSnapshot;
import com.gp.compass.entity.Trip;

public class TripMapper {

    public static Trip toEntity(TripRequest dto) {
        if (dto == null) return null;

        return Trip.builder()
                .originAddress(toSnapshot(dto.originAddress()))
                .destinationAddress(toSnapshot(dto.destinationAddress()))
                .build();
    }

    public static TripResponse toResponse(Trip entity) {
        if (entity == null) return null;

        return new TripResponse(
                entity.getId(),
                toSnapshotResponse(entity.getOriginAddress()),
                toSnapshotResponse(entity.getDestinationAddress()),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }

    private static AddressSnapshot toSnapshot(AddressSnapshotRequest dto) {
        if (dto == null) return null;

        return AddressSnapshot.builder()
                .cep(dto.cep())
                .street(dto.street())
                .neighborhood(dto.neighborhood())
                .number(dto.number())
                .city(dto.city())
                .state(dto.state())
                .complement(dto.complement())
                .build();
    }

    private static AddressSnapshotResponse toSnapshotResponse(AddressSnapshot snapshot) {
        if (snapshot == null) return null;

        return new AddressSnapshotResponse(
                snapshot.getCep(),
                snapshot.getStreet(),
                snapshot.getNeighborhood(),
                snapshot.getNumber(),
                snapshot.getCity(),
                snapshot.getState(),
                snapshot.getComplement()
        );
    }
}