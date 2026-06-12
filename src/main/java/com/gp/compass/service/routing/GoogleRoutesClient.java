package com.gp.compass.service.routing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Client for the Google Routes API (computeRouteMatrix). Provides real
 * road distances (meters) between a set of coordinates.
 */
@Component
public class GoogleRoutesClient {

    private static final String URL = "https://routes.googleapis.com/distanceMatrix/v2:computeRouteMatrix";

    /** Google limits computeRouteMatrix to 625 elements (origins x destinations), i.e. 25x25 per request. */
    private static final int MAX_DIM = 25;

    private final RestClient restClient = RestClient.create();

    @Value("${google.routes.api-key:}")
    private String apiKey;

    /**
     * Overlays {@code matrix} (already filled with a Haversine estimate) with real
     * road distances in meters from the Google Routes API. The coordinate set is
     * split into 25x25 chunks to respect the 625-element-per-request limit, so any
     * matrix size is supported. Cells belonging to a chunk that fails, or that
     * Google could not route, keep their Haversine value.
     */
    public void overlayDistances(double[][] matrix, List<double[]> coords) {
        if (apiKey == null || apiKey.isBlank()) return;

        List<int[]> blocks = chunkIndices(coords.size());
        for (int[] originBlock : blocks) {
            for (int[] destBlock : blocks) {
                overlayChunk(matrix, coords, originBlock, destBlock);
            }
        }
    }

    private List<int[]> chunkIndices(int n) {
        List<int[]> blocks = new ArrayList<>();
        for (int start = 0; start < n; start += MAX_DIM) {
            int end = Math.min(start + MAX_DIM, n);
            int[] block = new int[end - start];
            for (int i = 0; i < block.length; i++) block[i] = start + i;
            blocks.add(block);
        }
        return blocks;
    }

    private void overlayChunk(double[][] matrix, List<double[]> coords, int[] originBlock, int[] destBlock) {
        Map<String, Object> body = Map.of(
                "origins", toWaypoints(coords, originBlock),
                "destinations", toWaypoints(coords, destBlock),
                "travelMode", "DRIVE"
        );

        try {
            List<Map<String, Object>> cells = restClient.post()
                    .uri(URL)
                    .header("Content-Type", "application/json")
                    .header("X-Goog-Api-Key", apiKey)
                    .header("X-Goog-FieldMask", "originIndex,destinationIndex,distanceMeters,condition")
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });

            if (cells == null) return;

            for (Map<String, Object> cell : cells) {
                if (!"ROUTE_EXISTS".equals(cell.get("condition"))) continue;
                int localOrigin = ((Number) cell.getOrDefault("originIndex", 0)).intValue();
                int localDest = ((Number) cell.getOrDefault("destinationIndex", 0)).intValue();
                Number distance = (Number) cell.get("distanceMeters");
                matrix[originBlock[localOrigin]][destBlock[localDest]] = distance == null ? 0.0 : distance.doubleValue();
            }
        } catch (Exception e) {
            // leave Haversine values for this chunk
        }
    }

    private List<Map<String, Object>> toWaypoints(List<double[]> coords, int[] indices) {
        List<Map<String, Object>> waypoints = new ArrayList<>(indices.length);
        for (int idx : indices) {
            double[] c = coords.get(idx);
            waypoints.add(Map.of("waypoint", Map.of("location", Map.of("latLng",
                    Map.of("latitude", c[0], "longitude", c[1])))));
        }
        return waypoints;
    }
}
