package com.gp.compass.service.routing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Builds the NxN cost matrix used by the route optimization solvers.
 * Starts from a Haversine (straight-line) estimate and, when possible,
 * overlays it with real road distances from {@link GoogleRoutesClient}.
 */
@Service
@RequiredArgsConstructor
public class DistanceMatrixService {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private final GoogleRoutesClient googleRoutesClient;

    public double[][] buildMatrix(List<Waypoint> nodes) {
        int n = nodes.size();
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    matrix[i][j] = haversineMeters(nodes.get(i), nodes.get(j));
                }
            }
        }

        List<double[]> coords = nodes.stream()
                .map(w -> new double[]{w.lat(), w.lng()})
                .toList();
        googleRoutesClient.overlayDistances(matrix, coords);

        return matrix;
    }

    private double haversineMeters(Waypoint a, Waypoint b) {
        if (a.lat() == null || a.lng() == null || b.lat() == null || b.lng() == null) return 0;
        double dLat = Math.toRadians(b.lat() - a.lat());
        double dLon = Math.toRadians(b.lng() - a.lng());
        double sin = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.lat())) * Math.cos(Math.toRadians(b.lat()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_METERS * 2 * Math.atan2(Math.sqrt(sin), Math.sqrt(1 - sin));
    }
}
