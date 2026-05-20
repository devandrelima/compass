package com.gp.compass.service;

import com.gp.compass.dto.OptimizeStopInput;
import com.gp.compass.entity.StopPriority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RouteOptimizationService {

    /**
     * Hard-priority TSP: CRITICAL → HIGH → NORMAL.
     * Within each group: Nearest Neighbor seeded from the tail of the previous
     * group, followed by 2-opt improvement.
     */
    public List<UUID> optimize(List<OptimizeStopInput> stops) {
        if (stops.size() <= 1) {
            return stops.stream().map(OptimizeStopInput::id).collect(Collectors.toList());
        }

        List<OptimizeStopInput> critical = byPriority(stops, StopPriority.CRITICAL);
        List<OptimizeStopInput> high     = byPriority(stops, StopPriority.HIGH);
        List<OptimizeStopInput> normal   = byPriority(stops, StopPriority.NORMAL);

        List<OptimizeStopInput> result = new ArrayList<>(stops.size());
        OptimizeStopInput tail = null;
        tail = appendGroup(critical, tail, result);
        tail = appendGroup(high,     tail, result);
               appendGroup(normal,   tail, result);

        return result.stream().map(OptimizeStopInput::id).collect(Collectors.toList());
    }

    // ── Pipeline ──────────────────────────────────────────────────────────────

    private OptimizeStopInput appendGroup(List<OptimizeStopInput> group, OptimizeStopInput predecessor, List<OptimizeStopInput> out) {
        if (group.isEmpty()) return predecessor;
        List<OptimizeStopInput> ordered = twoOpt(nearestNeighbor(group, predecessor));
        out.addAll(ordered);
        return ordered.get(ordered.size() - 1);
    }

    // ── Nearest Neighbour ─────────────────────────────────────────────────────

    private List<OptimizeStopInput> nearestNeighbor(List<OptimizeStopInput> group, OptimizeStopInput seed) {
        List<OptimizeStopInput> unvisited = new ArrayList<>(group);
        List<OptimizeStopInput> ordered   = new ArrayList<>(group.size());

        OptimizeStopInput current = (seed != null && hasCoords(seed))
                ? pullNearest(seed, unvisited)
                : unvisited.remove(0);
        ordered.add(current);

        while (!unvisited.isEmpty()) {
            current = pullNearest(current, unvisited);
            ordered.add(current);
        }
        return ordered;
    }

    private OptimizeStopInput pullNearest(OptimizeStopInput from, List<OptimizeStopInput> candidates) {
        OptimizeStopInput best = null;
        double min = Double.MAX_VALUE;

        for (OptimizeStopInput c : candidates) {
            if (!hasCoords(from) || !hasCoords(c)) {
                if (best == null) best = c;
                continue;
            }
            double d = haversine(from.lat(), from.lng(), c.lat(), c.lng());
            if (d < min) { min = d; best = c; }
        }
        candidates.remove(best);
        return best;
    }

    // ── 2-opt ─────────────────────────────────────────────────────────────────

    private List<OptimizeStopInput> twoOpt(List<OptimizeStopInput> route) {
        if (route.size() <= 2) return route;

        List<OptimizeStopInput> best     = new ArrayList<>(route);
        boolean                 improved = true;

        while (improved) {
            improved = false;
            for (int i = 0; i < best.size() - 1; i++) {
                for (int j = i + 2; j < best.size(); j++) {
                    if (swapGain(best, i, j) > 1e-10) {
                        best     = reversed(best, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    private double swapGain(List<OptimizeStopInput> r, int i, int j) {
        double before = edge(r, i, i + 1);
        double after  = edge(r, i, j);
        if (j + 1 < r.size()) {
            before += edge(r, j, j + 1);
            after  += edge(r, i + 1, j + 1);
        }
        return before - after;
    }

    private List<OptimizeStopInput> reversed(List<OptimizeStopInput> route, int from, int to) {
        List<OptimizeStopInput> copy = new ArrayList<>(route);
        while (from < to) {
            OptimizeStopInput tmp = copy.get(from);
            copy.set(from++, copy.get(to));
            copy.set(to--, tmp);
        }
        return copy;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double edge(List<OptimizeStopInput> r, int a, int b) {
        return dist(r.get(a), r.get(b));
    }

    private double dist(OptimizeStopInput a, OptimizeStopInput b) {
        if (!hasCoords(a) || !hasCoords(b)) return 0;
        return haversine(a.lat(), a.lng(), b.lat(), b.lng());
    }

    private boolean hasCoords(OptimizeStopInput s) {
        return s != null && s.lat() != null && s.lng() != null;
    }

    private List<OptimizeStopInput> byPriority(List<OptimizeStopInput> stops, StopPriority p) {
        return stops.stream().filter(s -> s.priority() == p).collect(Collectors.toList());
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
