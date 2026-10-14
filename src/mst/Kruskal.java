package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;

public class Kruskal {

    public static MSTResult run(Graph g) {
        return run(g, true, true);
    }

    public static MSTResult run(Graph g, boolean pathCompression, boolean unionByRank) {
        MSTResult result = new MSTResult();
        long start = System.nanoTime();

        int V = g.getV();
        List<Edge> sorted = new ArrayList<>(g.getEdges());

        final long[] cmp = {0};
        sorted.sort((a, b) -> {
            cmp[0]++;
            return Integer.compare(a.weight, b.weight);
        });
        result.comparisons = cmp[0];

        DSU dsu = new DSU(V, pathCompression, unionByRank);
        for (Edge e : sorted) {
            result.edgesExamined++;
            if (dsu.union(e.from, e.to)) {
                result.add(e);
                if (result.edges.size() == V - 1) break;
            }
        }

        result.dsuFinds = dsu.findCalls;
        result.dsuUnions = dsu.unionCalls;
        result.connected = (dsu.getComponents() == 1);
        result.timeNanos = System.nanoTime() - start;
        return result;
    }
}