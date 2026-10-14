package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class GraphGenerator {

    public static Graph connectedUndirected(int V, int E, long seed, int maxWeight) {
        if (E < V - 1) throw new IllegalArgumentException("Байланысты граф үшін E >= V-1 керек");
        if (E > maxUndirectedEdges(V)) throw new IllegalArgumentException("E тым көп");
        Random rnd = new Random(seed);
        Graph g = new Graph(V, false);
        Set<Long> used = new HashSet<>();

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < V; i++) order.add(i);
        Collections.shuffle(order, rnd);

        for (int i = 1; i < V; i++) {
            int u = order.get(i);
            int v = order.get(rnd.nextInt(i));
            g.addEdge(u, v, randomWeight(rnd, maxWeight));
            used.add(undirectedKey(u, v, V));
        }
        int count = V - 1;
        while (count < E) {
            int u = rnd.nextInt(V);
            int v = rnd.nextInt(V);
            if (u == v) continue;
            if (!used.add(undirectedKey(u, v, V))) continue;
            g.addEdge(u, v, randomWeight(rnd, maxWeight));
            count++;
        }
        return g;
    }

    public static Graph randomDirected(int V, int E, long seed, int maxWeight) {
        if (E > (long) V * (V - 1)) throw new IllegalArgumentException("E тым көп");
        Random rnd = new Random(seed);
        Graph g = new Graph(V, true);
        Set<Long> used = new HashSet<>();
        int count = 0;
        while (count < E) {
            int u = rnd.nextInt(V);
            int v = rnd.nextInt(V);
            if (u == v) continue;
            if (!used.add((long) u * V + v)) continue;
            g.addEdge(u, v, randomWeight(rnd, maxWeight));
            count++;
        }
        return g;
    }
    public static Graph randomDAG(int V, int E, long seed, int maxWeight) {
        if (E > maxUndirectedEdges(V)) throw new IllegalArgumentException("E тым көп");
        Random rnd = new Random(seed);
        Graph g = new Graph(V, true);

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < V; i++) order.add(i);
        Collections.shuffle(order, rnd);

        Set<Long> used = new HashSet<>();
        int count = 0;
        while (count < E) {
            int i = rnd.nextInt(V);
            int j = rnd.nextInt(V);
            if (i == j) continue;
            if (i > j) { int t = i; i = j; j = t; } // i < j
            if (!used.add((long) i * V + j)) continue;
            g.addEdge(order.get(i), order.get(j), randomWeight(rnd, maxWeight));
            count++;
        }
        return g;
    }

    private static int randomWeight(Random rnd, int maxWeight) {
        return 1 + rnd.nextInt(maxWeight);
    }

    private static long undirectedKey(int u, int v, int V) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        return (long) a * V + b;
    }
    private static long maxUndirectedEdges(int V) {
        return (long) V * (V - 1) / 2;
    }
}