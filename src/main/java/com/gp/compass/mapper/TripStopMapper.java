package com.gp.compass.mapper;

import com.gp.compass.dto.AddressSnapshotRequest;
import com.gp.compass.dto.AddressSnapshotResponse;
import com.gp.compass.dto.TripStopResponse;
import com.gp.compass.entity.AddressSnapshot;
import com.gp.compass.entity.TripStop;

public class TripStopMapper {

    public static TripStopResponse toResponse(TripStop entity) {
        if (entity == null) return null;

        return new TripStopResponse(
                entity.getId(),
                entity.getSequenceOrder(),
                entity.getStopType(),
                toSnapshotResponse(entity.getAddress())
        );
    }

    public static AddressSnapshot toSnapshot(AddressSnapshotRequest dto) {
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
