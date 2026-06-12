package com.gp.compass.service.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Genetic Algorithm with order crossover (OX), binary tournament selection,
 * elitism and swap mutation, respecting precedence constraints (open path
 * starting at index 0). Used when the group is too large for
 * {@link HeldKarpSolver} (n &gt; {@link HeldKarpSolver#MAX_N}).
 */
public final class GeneticAlgorithmSolver {

    private static final long SEED = 42L;
    private static final int POPULATION_SIZE = 60;
    private static final int GENERATIONS = 300;
    private static final double MUTATION_RATE = 0.2;
    private static final int ELITISM = 2;

    private GeneticAlgorithmSolver() {
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

        Random rng = new Random(SEED);
        int m = n - 1;

        List<int[]> population = new ArrayList<>(POPULATION_SIZE);
        population.add(repair(nearestNeighborTail(cost, precedence), precedence));
        while (population.size() < POPULATION_SIZE) {
            int[] candidate = new int[m];
            for (int i = 0; i < m; i++) candidate[i] = i + 1;
            shuffle(candidate, rng);
            population.add(repair(candidate, precedence));
        }

        for (int gen = 0; gen < GENERATIONS; gen++) {
            population.sort((a, b) -> Double.compare(fitness(a, cost), fitness(b, cost)));

            List<int[]> next = new ArrayList<>(POPULATION_SIZE);
            for (int i = 0; i < ELITISM; i++) next.add(population.get(i));

            while (next.size() < POPULATION_SIZE) {
                int[] parentA = tournament(population, rng, cost);
                int[] parentB = tournament(population, rng, cost);
                int[] child = orderCrossover(parentA, parentB, rng);
                if (rng.nextDouble() < MUTATION_RATE) {
                    int i = rng.nextInt(m);
                    int j = rng.nextInt(m);
                    int tmp = child[i];
                    child[i] = child[j];
                    child[j] = tmp;
                }
                next.add(repair(child, precedence));
            }
            population = next;
        }

        int[] best = population.stream()
                .min((a, b) -> Double.compare(fitness(a, cost), fitness(b, cost)))
                .orElseThrow();

        int[] order = new int[n];
        order[0] = 0;
        System.arraycopy(best, 0, order, 1, m);
        return order;
    }

    private static int[] tournament(List<int[]> population, Random rng, double[][] cost) {
        int[] a = population.get(rng.nextInt(population.size()));
        int[] b = population.get(rng.nextInt(population.size()));
        return fitness(a, cost) < fitness(b, cost) ? a : b;
    }

    /** Order crossover (OX): copies a random slice from p1, fills the rest with p2's order. */
    private static int[] orderCrossover(int[] p1, int[] p2, Random rng) {
        int m = p1.length;
        if (m < 2) return p1.clone();

        int a = rng.nextInt(m);
        int b = rng.nextInt(m);
        while (b == a) b = rng.nextInt(m);
        if (a > b) { int tmp = a; a = b; b = tmp; }

        Integer[] child = new Integer[m];
        boolean[] inChild = new boolean[m + 1];
        for (int i = a; i <= b; i++) {
            child[i] = p1[i];
            inChild[p1[i]] = true;
        }

        int fillIdx = 0;
        for (int i = 0; i < m; i++) {
            if (child[i] != null) continue;
            while (inChild[p2[fillIdx]]) fillIdx++;
            child[i] = p2[fillIdx];
            inChild[p2[fillIdx]] = true;
            fillIdx++;
        }

        int[] result = new int[m];
        for (int i = 0; i < m; i++) result[i] = child[i];
        return result;
    }

    /**
     * Fixes precedence violations in-place: each desembarque/embarque pair is
     * disjoint (an embarque is never itself constrained), so a single swap per
     * violated pair is enough to restore feasibility.
     */
    private static int[] repair(int[] perm, int[] precedence) {
        int[] result = perm.clone();
        int[] position = new int[precedence.length];
        for (int p = 0; p < result.length; p++) position[result[p]] = p;

        for (int node = 1; node < precedence.length; node++) {
            int pre = precedence[node];
            if (pre <= 0) continue; // -1: unconstrained; 0: predecessor always visited first

            int posNode = position[node];
            int posPre = position[pre];
            if (posPre > posNode) {
                result[posNode] = pre;
                result[posPre] = node;
                position[pre] = posNode;
                position[node] = posPre;
            }
        }
        return result;
    }

    /** Nearest-neighbor tour starting at index 0, returning only the tail (indices 1..n-1). */
    private static int[] nearestNeighborTail(double[][] cost, int[] precedence) {
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

        int[] tail = new int[n - 1];
        System.arraycopy(order, 1, tail, 0, n - 1);
        return tail;
    }

    private static void shuffle(int[] array, Random rng) {
        for (int i = array.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = array[i];
            array[i] = array[j];
            array[j] = tmp;
        }
    }

    private static double fitness(int[] perm, double[][] cost) {
        double total = cost[0][perm[0]];
        for (int i = 0; i < perm.length - 1; i++) {
            total += cost[perm[i]][perm[i + 1]];
        }
        return total;
    }
}
