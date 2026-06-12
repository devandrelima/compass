package com.gp.compass.service.routing;

import java.util.Arrays;

/**
 * Held-Karp dynamic programming: exact solution for the open-path TSP
 * starting at index 0, with optional precedence constraints.
 *
 * Viable up to {@link #MAX_N} nodes (2^n * n^2 states); above that callers
 * should fall back to {@link SimulatedAnnealingSolver}.
 */
public final class HeldKarpSolver {

    public static final int MAX_N = 13;

    private HeldKarpSolver() {
    }

    /**
     * @param cost       NxN cost matrix.
     * @param precedence precedence[k] = index that must be visited before k, or -1.
     * @return visiting order (order[0] == 0).
     */
    public static int[] solve(double[][] cost, int[] precedence) {
        int n = cost.length;
        if (n <= 2) {
            int[] order = new int[n];
            for (int i = 0; i < n; i++) order[i] = i;
            return order;
        }

        int full = 1 << n;
        double[][] dp = new double[full][n];
        int[][] parent = new int[full][n];
        for (double[] row : dp) Arrays.fill(row, Double.POSITIVE_INFINITY);
        for (int[] row : parent) Arrays.fill(row, -1);
        dp[1][0] = 0.0;

        for (int mask = 1; mask < full; mask++) {
            if ((mask & 1) == 0) continue; // node 0 is always the start
            for (int j = 0; j < n; j++) {
                if (dp[mask][j] == Double.POSITIVE_INFINITY) continue;
                for (int k = 0; k < n; k++) {
                    if ((mask & (1 << k)) != 0) continue;
                    if (precedence[k] != -1 && (mask & (1 << precedence[k])) == 0) continue;
                    int nextMask = mask | (1 << k);
                    double candidate = dp[mask][j] + cost[j][k];
                    if (candidate < dp[nextMask][k]) {
                        dp[nextMask][k] = candidate;
                        parent[nextMask][k] = j;
                    }
                }
            }
        }

        int endMask = full - 1;
        int last = 0;
        double best = Double.POSITIVE_INFINITY;
        for (int j = 0; j < n; j++) {
            if (dp[endMask][j] < best) {
                best = dp[endMask][j];
                last = j;
            }
        }

        int[] order = new int[n];
        int mask = endMask;
        int j = last;
        for (int idx = n - 1; idx >= 0; idx--) {
            order[idx] = j;
            int prevJ = parent[mask][j];
            mask ^= (1 << j);
            j = prevJ;
        }
        return order;
    }
}
