package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.Arrays;
import java.util.PriorityQueue;

public class Dijkstra {

    public static DijkstraResult run(Graph g, int source) {
        int V = g.getV();

        for (Edge e : g.getEdges()) {
            if (e.weight < 0) {
                throw new IllegalArgumentException(
                        "Dijkstra теріс салмақты қабырғамен жұмыс істемейді: " + e);
            }
        }

        DijkstraResult result = new DijkstraResult(source, V);
        long start = System.nanoTime();

        Arrays.fill(result.dist, DijkstraResult.INF);
        Arrays.fill(result.parent, -1);
        result.dist[source] = 0;

        final long[] cmp = {0};

        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> {
            cmp[0]++;
            return Long.compare(a[0], b[0]);
        });
        pq.add(new long[]{0, source});
        result.heapPushes++;

        boolean[] done = new boolean[V];
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            result.heapPolls++;
            int u = (int) top[1];
            if (done[u]) continue;
            done[u] = true;

            for (Edge e : g.getNeighbors(u)) {
                result.edgesExamined++;
                long nd = result.dist[u] + e.weight;
                if (nd < result.dist[e.to]) {
                    result.dist[e.to] = nd;
                    result.parent[e.to] = u;
                    result.relaxations++;
                    pq.add(new long[]{nd, e.to});
                    result.heapPushes++;
                }
            }
        }

        result.comparisons = cmp[0];
        result.timeNanos = System.nanoTime() - start;
        return result;
    }
}