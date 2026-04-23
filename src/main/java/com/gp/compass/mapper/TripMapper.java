package com.gp.compass.mapper;

import com.gp.compass.dto.TripRequest;
import com.gp.compass.dto.TripResponse;
import com.gp.compass.dto.TripStopResponse;
import com.gp.compass.entity.Trip;
import com.gp.compass.entity.TripStop;

import java.util.ArrayList;
import java.util.List;

public class TripMapper {

    public static Trip toEntity(TripRequest dto) {
        if (dto == null) return null;

        Trip trip = Trip.builder()
                .title(dto.title())
                .build();

        List<TripStop> stops = trip.getStops();
        for (int i = 0; i < dto.stops().size(); i++) {
            TripStop stop = TripStop.builder()
                    .trip(trip)
                    .sequenceOrder(i)
                    .address(TripStopMapper.toSnapshot(dto.stops().get(i).address()))
                    .stopType(dto.stops().get(i).stopType())
                    .priority(dto.stops().get(i).priority())
                    .build();
            stops.add(stop);
        }
        trip.setStops(stops);

        return trip;
    }

    public static TripResponse toResponse(Trip entity) {
        if (entity == null) return null;

        List<TripStopResponse> stopResponses = entity.getStops().stream()
                .map(TripStopMapper::toResponse)
                .toList();

        return new TripResponse(
                entity.getId(),
                entity.getTitle(),
                entity.getStatus(),
                stopResponses,
                entity.getCreatedAt()
        );
    }
}