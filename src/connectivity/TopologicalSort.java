package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.Queue;

public class TopologicalSort {

    public static TopoResult run(Graph g) {
        TopoResult result = new TopoResult();
        long start = System.nanoTime();

        int V = g.getV();
        int[] inDegree = new int[V];
        for (int u = 0; u < V; u++) {
            for (Edge e : g.getNeighbors(u)) {
                result.edgesExamined++;
                inDegree[e.to]++;
            }
        }

        Queue<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < V; v++) {
            if (inDegree[v] == 0) queue.add(v);
        }

        while (!queue.isEmpty()) {
            int u = queue.poll();
            result.verticesProcessed++;
            result.order.add(u);
            for (Edge e : g.getNeighbors(u)) {
                result.edgesExamined++;
                inDegree[e.to]--;
                if (inDegree[e.to] == 0) queue.add(e.to);
            }
        }

        result.isDAG = (result.order.size() == V);
        if (!result.isDAG) {
            result.order.clear();
        }
        result.timeNanos = System.nanoTime() - start;
        return result;
    }

    public static boolean isValidOrder(Graph g, java.util.List<Integer> order) {
        if (order.size() != g.getV()) return false;
        int[] pos = new int[g.getV()];
        for (int i = 0; i < order.size(); i++) pos[order.get(i)] = i;
        for (Edge e : g.getEdges()) {
            if (pos[e.from] >= pos[e.to]) return false;
        }
        return true;
    }
}