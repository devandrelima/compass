package com.gp.compass.service.routing;

import java.util.Random;

/**
 * Simulated Annealing with a 2-opt neighborhood (open path starting at index 0),
 * respecting precedence constraints. Used when the group is too large for
 * {@link HeldKarpSolver} (n > {@link HeldKarpSolver#MAX_N}).
 */
public final class SimulatedAnnealingSolver {

    private static final long SEED = 42L;
    private static final double COOLING = 0.9995;
    private static final int ITERATIONS = 20_000;

    private SimulatedAnnealingSolver() {
    }

    /**
     * @param cost       NxN cost matrix.
     * @param precedence precedence[k] = index that must be visited before k, or -1.
     * @return visiting order (order[0] == 0).
     */
    public static int[] solve(double[][] cost, int[] precedence) {
        int n = cost.length;

        int[] current = initialTour(cost, precedence);
        double currentCost = tourCost(current, cost);

        int[] best = current.clone();
        double bestCost = currentCost;

        Random rng = new Random(SEED);
        double temp = Math.max(currentCost, 1.0);

        for (int iter = 0; iter < ITERATIONS && temp > 1e-6; iter++) {
            int i = 1 + rng.nextInt(n - 2);
            int k = i + 1 + rng.nextInt(n - i - 1);

            int[] candidate = reverseSegment(current, i, k);
            if (!feasible(candidate, precedence)) {
                temp *= COOLING;
                continue;
            }

            double candidateCost = tourCost(candidate, cost);
            double delta = candidateCost - currentCost;
            if (delta < 0 || rng.nextDouble() < Math.exp(-delta / temp)) {
                current = candidate;
                currentCost = candidateCost;
                if (currentCost < bestCost) {
                    best = current.clone();
                    bestCost = currentCost;
                }
            }
            temp *= COOLING;
        }

        return best;
    }

    /** Nearest-neighbor tour starting at index 0, only visiting a node once its precedence is satisfied. */
    private static int[] initialTour(double[][] cost, int[] precedence) {
        int n = cost.length;
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        order[0] = 0;
        visited[0] = true;

        for (int step = 1; step < n; step++) {
            int current = order[step - 1];
            int next = -1;
            double bestDist = Double.POSITIVE_INFINITY;
            for (int k = 0; k < n; k++) {
                if (visited[k]) continue;
                if (precedence[k] != -1 && !visited[precedence[k]]) continue;
                if (cost[current][k] < bestDist) {
                    bestDist = cost[current][k];
                    next = k;
                }
            }
            order[step] = next;
            visited[next] = true;
        }
        return order;
    }

    private static int[] reverseSegment(int[] order, int from, int to) {
        int[] result = order.clone();
        while (from < to) {
            int tmp = result[from];
            result[from] = result[to];
            result[to] = tmp;
            from++;
            to--;
        }
        return result;
    }

    private static boolean feasible(int[] order, int[] precedence) {
        int n = order.length;
        int[] position = new int[n];
        for (int p = 0; p < n; p++) position[order[p]] = p;
        for (int node = 0; node < n; node++) {
            int pre = precedence[node];
            if (pre != -1 && position[pre] >= position[node]) return false;
        }
        return true;
    }

    private static double tourCost(int[] order, double[][] cost) {
        double total = 0;
        for (int i = 0; i < order.length - 1; i++) {
            total += cost[order[i]][order[i + 1]];
        }
        return total;
    }
}
