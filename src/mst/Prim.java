package mst;

import graph.Edge;
import graph.Graph;
import java.util.PriorityQueue;

public class Prim {

    public static MSTResult run(Graph g) {
        MSTResult result = new MSTResult();
        long start = System.nanoTime();

        int V = g.getV();
        boolean[] visited = new boolean[V];

        final long[] cmp = {0};
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            cmp[0]++;
            return Integer.compare(a.weight, b.weight);
        });

        int components = 0;
        for (int s = 0; s < V; s++) {
            if (visited[s]) continue;
            components++;
            visited[s] = true;
            pushEdges(g, s, visited, pq, result);

            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                result.heapPolls++;
                if (visited[e.to]) continue;
                visited[e.to] = true;
                result.add(e);
                pushEdges(g, e.to, visited, pq, result);
            }
        }

        result.comparisons = cmp[0];
        result.connected = (components == 1);
        result.timeNanos = System.nanoTime() - start;
        return result;
    }

    private static void pushEdges(Graph g, int u, boolean[] visited,
                                  PriorityQueue<Edge> pq, MSTResult result) {
        for (Edge e : g.getNeighbors(u)) {
            result.edgesExamined++;
            if (!visited[e.to]) {
                pq.add(e);
                result.heapPushes++;
            }
        }
    }
}