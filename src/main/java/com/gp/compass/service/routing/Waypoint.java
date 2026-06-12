package com.gp.compass.service.routing;

import com.gp.compass.entity.StopPriority;

import java.util.UUID;

/**
 * One point to visit in the optimized route: either the embarque or the
 * desembarque address of a trip stop.
 */
public record Waypoint(UUID stopId, boolean isDesembarque, Double lat, Double lng, StopPriority priority) {
}
