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
     * Returns waypoints in optimized order; list index = sequence position.
     */
    public List<Waypoint> optimize(List<TripStop> stops) {
        List<Waypoint> all = buildWaypoints(stops);
        if (all.size() <= 1) return all;

        List<Waypoint> critical = byPriority(all, StopPriority.CRITICAL);
        List<Waypoint> high     = byPriority(all, StopPriority.HIGH);
        List<Waypoint> normal   = byPriority(all, StopPriority.NORMAL);

        List<Waypoint> result = new ArrayList<>(all.size());
        Waypoint tail = null;
        tail = appendGroup(critical, tail, result);
        tail = appendGroup(high,     tail, result);
               appendGroup(normal,   tail, result);

        return result;
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
                if (!candidate.isDesembarque() && candidate.stopId().equals(w.stopId())) {
                    precedence[i] = j;
                    break;
                }
            }
        }
        return precedence;
    }

    // ── Waypoint construction ────────────────────────────────────────────────

    private List<Waypoint> buildWaypoints(List<TripStop> stops) {
        List<Waypoint> waypoints = new ArrayList<>();
        for (TripStop stop : stops) {
            waypoints.add(new Waypoint(
                    stop.getId(), false,
                    stop.getEmbarque().getLat(), stop.getEmbarque().getLng(),
                    stop.getPriority()
            ));
            if (stop.getDesembarque() != null && stop.getDesembarque().getLat() != null) {
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
     * Builds the waypoints in the trip's current visiting order, merging embarque
     * and desembarque waypoints by their respective {@code sequenceOrder}.
     */
    public List<Waypoint> buildCurrentOrder(List<TripStop> stops) {
        record Indexed(Waypoint waypoint, double order) {}

        List<Indexed> indexed = new ArrayList<>();
        for (TripStop stop : stops) {
            indexed.add(new Indexed(new Waypoint(
                    stop.getId(), false,
                    stop.getEmbarque().getLat(), stop.getEmbarque().getLng(),
                    stop.getPriority()
            ), stop.getEmbarqueSequenceOrder()));

            if (stop.getDesembarque() != null && stop.getDesembarque().getLat() != null) {
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
}
