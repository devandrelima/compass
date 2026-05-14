package com.gp.compass.service;

import com.gp.compass.entity.AddressSnapshot;
import com.gp.compass.entity.StopPriority;
import com.gp.compass.entity.TripStop;
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
     *
     * Stops without coordinates are placed as-is at the end of their group.
     */
    public List<UUID> optimize(List<TripStop> stops) {
        if (stops.size() <= 1) {
            return stops.stream().map(TripStop::getId).collect(Collectors.toList());
        }

        List<TripStop> critical = byPriority(stops, StopPriority.CRITICAL);
        List<TripStop> high     = byPriority(stops, StopPriority.HIGH);
        List<TripStop> normal   = byPriority(stops, StopPriority.NORMAL);

        List<TripStop> result = new ArrayList<>(stops.size());
        TripStop tail = null;
        tail = appendGroup(critical, tail, result);
        tail = appendGroup(high,     tail, result);
               appendGroup(normal,   tail, result);

        return result.stream().map(TripStop::getId).collect(Collectors.toList());
    }

    // ── Pipeline ──────────────────────────────────────────────────────────────

    private TripStop appendGroup(List<TripStop> group, TripStop predecessor, List<TripStop> out) {
        if (group.isEmpty()) return predecessor;
        List<TripStop> ordered = twoOpt(nearestNeighbor(group, predecessor));
        out.addAll(ordered);
        return ordered.get(ordered.size() - 1);
    }

    // ── Nearest Neighbour ─────────────────────────────────────────────────────

    private List<TripStop> nearestNeighbor(List<TripStop> group, TripStop seed) {
        List<TripStop> unvisited = new ArrayList<>(group);
        List<TripStop> ordered   = new ArrayList<>(group.size());

        TripStop current = (seed != null && hasCoords(seed.getEmbarque()))
                ? pullNearest(seed.getEmbarque(), unvisited)
                : unvisited.remove(0);
        ordered.add(current);

        while (!unvisited.isEmpty()) {
            current = pullNearest(current.getEmbarque(), unvisited);
            ordered.add(current);
        }
        return ordered;
    }

    private TripStop pullNearest(AddressSnapshot from, List<TripStop> candidates) {
        TripStop best = null;
        double   min  = Double.MAX_VALUE;

        for (TripStop c : candidates) {
            if (!hasCoords(from) || !hasCoords(c.getEmbarque())) {
                if (best == null) best = c;
                continue;
            }
            double d = haversine(from.getLat(), from.getLng(),
                                 c.getEmbarque().getLat(), c.getEmbarque().getLng());
            if (d < min) { min = d; best = c; }
        }
        candidates.remove(best);
        return best;
    }

    // ── 2-opt ─────────────────────────────────────────────────────────────────

    private List<TripStop> twoOpt(List<TripStop> route) {
        if (route.size() <= 2) return route;

        List<TripStop> best     = new ArrayList<>(route);
        boolean        improved = true;

        while (improved) {
            improved = false;
            for (int i = 0; i < best.size() - 1; i++) {
                for (int j = i + 2; j < best.size(); j++) {
                    double gain = swapGain(best, i, j);
                    if (gain > 1e-10) {
                        best     = reversed(best, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
        return best;
    }

    /** Positive value means the swap is beneficial. */
    private double swapGain(List<TripStop> r, int i, int j) {
        double before = edge(r, i, i + 1);
        double after  = edge(r, i, j);
        if (j + 1 < r.size()) {
            before += edge(r, j, j + 1);
            after  += edge(r, i + 1, j + 1);
        }
        return before - after;
    }

    private List<TripStop> reversed(List<TripStop> route, int from, int to) {
        List<TripStop> copy = new ArrayList<>(route);
        while (from < to) {
            TripStop tmp = copy.get(from);
            copy.set(from++, copy.get(to));
            copy.set(to--, tmp);
        }
        return copy;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private double edge(List<TripStop> r, int a, int b) {
        return dist(r.get(a).getEmbarque(), r.get(b).getEmbarque());
    }

    private double dist(AddressSnapshot a, AddressSnapshot b) {
        if (!hasCoords(a) || !hasCoords(b)) return 0;
        return haversine(a.getLat(), a.getLng(), b.getLat(), b.getLng());
    }

    private boolean hasCoords(AddressSnapshot s) {
        return s != null && s.getLat() != null && s.getLng() != null;
    }

    private List<TripStop> byPriority(List<TripStop> stops, StopPriority p) {
        return stops.stream().filter(s -> s.getPriority() == p).collect(Collectors.toList());
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
