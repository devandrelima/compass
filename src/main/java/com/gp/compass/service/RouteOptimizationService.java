package com.gp.compass.service;

import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.TripStop;
import com.gp.compass.service.routing.DistanceMatrixService;
import com.gp.compass.service.routing.GeneticAlgorithmSolver;
import com.gp.compass.service.routing.HeldKarpSolver;
import com.gp.compass.service.routing.SimulatedAnnealingSolver;
import com.gp.compass.service.routing.Waypoint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteOptimizationService {

    private final DistanceMatrixService distanceMatrixService;

    /**
     * Hard-priority TSP with precedence constraints: CRITICAL → HIGH → NORMAL.
     * Within each group, the cost matrix is the real road distance (Google Routes,
     * falling back to Haversine) and the visiting order is solved with
     * {@link HeldKarpSolver} (exact, up to {@link HeldKarpSolver#MAX_N} waypoints)
     * or {@link SimulatedAnnealingSolver} (above that).
     *
     * Only waypoints not yet checked (embarque/desembarque pendentes) are
     * considered. If {@code origin} is provided (e.g. driver's current
     * location), it anchors the start of the first non-empty priority group
     * but is never included in the returned list.
     *
     * Returns waypoints in optimized order; list index = sequence position.
     */
    public List<Waypoint> optimize(List<TripStop> stops, Waypoint origin) {
        List<Waypoint> all = buildWaypoints(stops);
        if (all.size() <= 1) return all;

        List<Waypoint> critical = byPriority(all, StopPriority.CRITICAL);
        List<Waypoint> high     = byPriority(all, StopPriority.HIGH);
        List<Waypoint> normal   = byPriority(all, StopPriority.NORMAL);

        List<Waypoint> result = new ArrayList<>(all.size());
        Waypoint tail = origin;
        tail = appendGroup(critical, tail, result);
        tail = appendGroup(high,     tail, result);
               appendGroup(normal,   tail, result);

        return result;
    }

    /**
     * Wraps the driver's current position as a synthetic, non-persisted
     * waypoint used only to anchor the start of the route.
     */
    public Waypoint buildOrigin(Double lat, Double lng) {
        if (lat == null || lng == null) return null;
        return new Waypoint(null, false, lat, lng, StopPriority.NORMAL);
    }

    // ── Pipeline ──────────────────────────────────────────────────────────────

    /**
     * Solves the group's visiting order, seeded by the previous group's last
     * waypoint (used only to anchor the route's starting point, not part of
     * the group's own output).
     */
    private Waypoint appendGroup(List<Waypoint> group, Waypoint predecessor, List<Waypoint> out) {
        if (group.isEmpty()) return predecessor;

        if (predecessor == null && group.size() == 1) {
            Waypoint only = group.get(0);
            out.add(only);
            return only;
        }

        List<Waypoint> nodes = new ArrayList<>(group.size() + 1);
        if (predecessor != null) nodes.add(predecessor);
        nodes.addAll(group);

        double[][] matrix = distanceMatrixService.buildMatrix(nodes);
        int[] precedence = buildPrecedence(nodes);

        int[] order = nodes.size() <= HeldKarpSolver.MAX_N
                ? HeldKarpSolver.solve(matrix, precedence)
                : solveLargeGroup(matrix, precedence);

        List<Waypoint> ordered = new ArrayList<>(group.size());
        for (int idx : order) {
            if (predecessor != null && idx == 0) continue;
            ordered.add(nodes.get(idx));
        }

        out.addAll(ordered);
        return ordered.get(ordered.size() - 1);
    }

    /**
     * Solves groups above {@link HeldKarpSolver#MAX_N} with {@link GeneticAlgorithmSolver}
     * (best-performing approximate algorithm besides the exact one), falling back to
     * {@link SimulatedAnnealingSolver} only if the GA run fails unexpectedly.
     */
    private int[] solveLargeGroup(double[][] matrix, int[] precedence) {
        try {
            return GeneticAlgorithmSolver.solve(matrix, precedence);
        } catch (Exception e) {
            return SimulatedAnnealingSolver.solve(matrix, precedence);
        }
    }

    /**
     * Builds the precedence array for the Held-Karp/GA/SA solvers: a desembarque
     * waypoint must be visited after the embarque waypoint of the same stop.
     */
    private int[] buildPrecedence(List<Waypoint> nodes) {
        int n = nodes.size();
        int[] precedence = new int[n];
        Arrays.fill(precedence, -1);

        for (int i = 0; i < n; i++) {
            Waypoint w = nodes.get(i);
            if (!w.isDesembarque()) continue;
            for (int j = 0; j < n; j++) {
                Waypoint candidate = nodes.get(j);
                if (!candidate.isDesembarque() && Objects.equals(candidate.stopId(), w.stopId())) {
                    precedence[i] = j;
                    break;
                }
            }
        }
        return precedence;
    }

    // ── Waypoint construction ────────────────────────────────────────────────

    /**
     * Builds waypoints for stops not yet checked: embarque is included
     * unless already checked, desembarque unless already checked (or absent).
     */
    private List<Waypoint> buildWaypoints(List<TripStop> stops) {
        List<Waypoint> waypoints = new ArrayList<>();
        for (TripStop stop : stops) {
            if (!stop.isEmbarqueChecked()) {
                waypoints.add(new Waypoint(
                        stop.getId(), false,
                        stop.getEmbarque().getLat(), stop.getEmbarque().getLng(),
                        stop.getPriority()
                ));
            }
            if (stop.getDesembarque() != null && stop.getDesembarque().getLat() != null
                    && !stop.isDesembarqueChecked()) {
                waypoints.add(new Waypoint(
                        stop.getId(), true,
                        stop.getDesembarque().getLat(), stop.getDesembarque().getLng(),
                        stop.getPriority()
                ));
            }
        }
        return waypoints;
    }

    private List<Waypoint> byPriority(List<Waypoint> waypoints, StopPriority p) {
        return waypoints.stream().filter(w -> w.priority() == p).collect(Collectors.toList());
    }

    // ── Preview helpers ──────────────────────────────────────────────────────

    /**
     * Builds the not-yet-checked waypoints in the trip's current visiting order,
     * merging embarque and desembarque waypoints by their respective
     * {@code sequenceOrder}.
     */
    public List<Waypoint> buildCurrentOrder(List<TripStop> stops) {
        record Indexed(Waypoint waypoint, double order) {}

        List<Indexed> indexed = new ArrayList<>();
        for (TripStop stop : stops) {
            if (!stop.isEmbarqueChecked()) {
                indexed.add(new Indexed(new Waypoint(
                        stop.getId(), false,
                        stop.getEmbarque().getLat(), stop.getEmbarque().getLng(),
                        stop.getPriority()
                ), stop.getEmbarqueSequenceOrder()));
            }

            if (stop.getDesembarque() != null && stop.getDesembarque().getLat() != null
                    && !stop.isDesembarqueChecked()) {
                // Sem otimização prévia, desembarqueSequenceOrder é null: posiciona logo
                // após o embarque da mesma parada (mesmo critério do frontend).
                double order = stop.getDesembarqueSequenceOrder() != null
                        ? stop.getDesembarqueSequenceOrder()
                        : stop.getEmbarqueSequenceOrder() + 0.5;
                indexed.add(new Indexed(new Waypoint(
                        stop.getId(), true,
                        stop.getDesembarque().getLat(), stop.getDesembarque().getLng(),
                        stop.getPriority()
                ), order));
            }
        }

        return indexed.stream()
                .sorted(Comparator.comparingDouble(Indexed::order))
                .map(Indexed::waypoint)
                .toList();
    }

    /**
     * Sum of the real road distances (meters) between consecutive waypoints
     * in {@code ordered}, falling back to Haversine when the road matrix is
     * unavailable (see {@link DistanceMatrixService}).
     */
    public double totalDistanceMeters(List<Waypoint> ordered) {
        if (ordered.size() < 2) return 0;

        double[][] matrix = distanceMatrixService.buildMatrix(ordered);
        double total = 0;
        for (int i = 0; i < ordered.size() - 1; i++) {
            total += matrix[i][i + 1];
        }
        return total;
    }

    /**
     * Same as {@link #totalDistanceMeters(List)}, but measuring from
     * {@code origin} (e.g. driver's current location) to the first waypoint
     * when present.
     */
    public double totalDistanceMeters(List<Waypoint> ordered, Waypoint origin) {
        if (origin == null) return totalDistanceMeters(ordered);
        if (ordered.isEmpty()) return 0;

        List<Waypoint> withOrigin = new ArrayList<>(ordered.size() + 1);
        withOrigin.add(origin);
        withOrigin.addAll(ordered);
        return totalDistanceMeters(withOrigin);
    }
}
