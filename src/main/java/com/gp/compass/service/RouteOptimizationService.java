package com.gp.compass.service;

import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.TripStop;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RouteOptimizationService {

    /**
     * Internal waypoint: one per address (embarque or desembarque) in the route.
     * Precedence constraint: isDesembarque=true must come after isDesembarque=false
     * for the same stopId.
     */
    public record Waypoint(UUID stopId, boolean isDesembarque, Double lat, Double lng, StopPriority priority) {}

    /**
     * Hard-priority TSP with precedence constraints: CRITICAL → HIGH → NORMAL.
     * Within each group: constrained Nearest Neighbor (visiting an embarque unlocks
     * its desembarque as a candidate) + 2-opt with feasibility check.
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

    private Waypoint appendGroup(List<Waypoint> group, Waypoint predecessor, List<Waypoint> out) {
        if (group.isEmpty()) return predecessor;
        List<Waypoint> ordered = twoOptFeasible(constrainedNN(group, predecessor));
        out.addAll(ordered);
        return ordered.get(ordered.size() - 1);
    }

    // ── Constrained Nearest Neighbor ──────────────────────────────────────────
    // Embarques start as available candidates. Visiting an embarque unlocks
    // its desembarque (if it has one) as the next candidate.

    private List<Waypoint> constrainedNN(List<Waypoint> group, Waypoint seed) {
        Set<UUID> withDesembarque = group.stream()
                .filter(Waypoint::isDesembarque)
                .map(Waypoint::stopId)
                .collect(Collectors.toSet());

        Map<UUID, Waypoint> desembarqueByStop = group.stream()
                .filter(Waypoint::isDesembarque)
                .collect(Collectors.toMap(Waypoint::stopId, w -> w));

        List<Waypoint> available = group.stream()
                .filter(w -> !w.isDesembarque())
                .collect(Collectors.toCollection(ArrayList::new));

        List<Waypoint> ordered = new ArrayList<>(group.size());
        if (available.isEmpty()) return ordered;

        Waypoint first = (seed != null)
                ? pullNearest(seed, available)
                : available.remove(0);

        ordered.add(first);
        unlockDesembarque(first, withDesembarque, desembarqueByStop, available);
        Waypoint current = first;

        while (!available.isEmpty()) {
            Waypoint next = pullNearest(current, available);
            ordered.add(next);
            if (!next.isDesembarque()) {
                unlockDesembarque(next, withDesembarque, desembarqueByStop, available);
            }
            current = next;
        }

        return ordered;
    }

    private void unlockDesembarque(Waypoint embarque, Set<UUID> withDesembarque,
                                   Map<UUID, Waypoint> desembarqueByStop, List<Waypoint> available) {
        if (withDesembarque.contains(embarque.stopId())) {
            available.add(desembarqueByStop.get(embarque.stopId()));
        }
    }

    // ── 2-opt with feasibility ─────────────────────────────────────────────────
    // A swap is accepted only if the reversed segment keeps every desembarque
    // after its embarque in the resulting route.

    private List<Waypoint> twoOptFeasible(List<Waypoint> route) {
        if (route.size() <= 2) return route;

        List<Waypoint> best = new ArrayList<>(route);
        boolean improved = true;

        while (improved) {
            improved = false;
            outer:
            for (int i = 0; i < best.size() - 1; i++) {
                for (int j = i + 2; j < best.size(); j++) {
                    if (swapGain(best, i, j) > 1e-10) {
                        List<Waypoint> candidate = reversed(best, i + 1, j);
                        if (isFeasible(candidate)) {
                            best = candidate;
                            improved = true;
                            break outer;
                        }
                    }
                }
            }
        }
        return best;
    }

    private boolean isFeasible(List<Waypoint> route) {
        Set<UUID> embarqueSeen = new HashSet<>();
        for (Waypoint w : route) {
            if (!w.isDesembarque()) {
                embarqueSeen.add(w.stopId());
            } else {
                if (!embarqueSeen.contains(w.stopId())) return false;
            }
        }
        return true;
    }

    // ── Nearest Neighbour helper ───────────────────────────────────────────────

    private Waypoint pullNearest(Waypoint from, List<Waypoint> candidates) {
        Waypoint best = null;
        double min = Double.MAX_VALUE;
        for (Waypoint c : candidates) {
            double d = dist(from, c);
            if (d < min) { min = d; best = c; }
        }
        candidates.remove(best);
        return best;
    }

    // ── 2-opt helpers ─────────────────────────────────────────────────────────

    private double swapGain(List<Waypoint> r, int i, int j) {
        double before = edge(r, i, i + 1);
        double after  = edge(r, i, j);
        if (j + 1 < r.size()) {
            before += edge(r, j, j + 1);
            after  += edge(r, i + 1, j + 1);
        }
        return before - after;
    }

    private List<Waypoint> reversed(List<Waypoint> route, int from, int to) {
        List<Waypoint> copy = new ArrayList<>(route);
        while (from < to) {
            Waypoint tmp = copy.get(from);
            copy.set(from++, copy.get(to));
            copy.set(to--, tmp);
        }
        return copy;
    }

    // ── Distance helpers ──────────────────────────────────────────────────────

    private double edge(List<Waypoint> r, int a, int b) {
        return dist(r.get(a), r.get(b));
    }

    private double dist(Waypoint a, Waypoint b) {
        if (a.lat() == null || a.lng() == null || b.lat() == null || b.lng() == null) return 0;
        return haversine(a.lat(), a.lng(), b.lat(), b.lng());
    }

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

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R    = 6371.0;
        double       dLat = Math.toRadians(lat2 - lat1);
        double       dLon = Math.toRadians(lon2 - lon1);
        double       a    = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                          + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                          * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
