import graph.Graph;
import graph.GraphGenerator;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 1, 2);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 5);
        g.addEdge(3, 4, 3);
        System.out.println("=== Dijkstra, source = 0 (күтілетіні: 0, 3, 1, 4, 7, INF) ===");
        DijkstraResult r = Dijkstra.run(g, 0);
        for (int v = 0; v < g.getV(); v++) {
            if (r.isReachable(v)) {
                System.out.println("0 -> " + v + ": қашықтық=" + r.dist[v] + ", жол=" + r.getPath(v));
            } else {
                System.out.println("0 -> " + v + ": қолжетімсіз");
            }
        }

        Graph neg = new Graph(3, true);
        neg.addEdge(0, 1, 4);
        neg.addEdge(1, 2, -2);
        System.out.println("\n=== Теріс салмақ ===");
        try {
            Dijkstra.run(neg, 0);
        } catch (IllegalArgumentException ex) {
            System.out.println("Қате ұсталды: " + ex.getMessage());
        }

        Graph single = new Graph(1, true);
        DijkstraResult rs = Dijkstra.run(single, 0);
        System.out.println("\n=== Бір төбелі граф: dist[0] = " + rs.dist[0] + " ===");

        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] s : sizes) {
            Graph und = GraphGenerator.connectedUndirected(s[0], s[1], 42, 100);
            Graph dir = GraphGenerator.randomDirected(s[0], s[1], 42, 100);
            DijkstraResult a = Dijkstra.run(und, 0);
            DijkstraResult b = Dijkstra.run(dir, 0);
            System.out.println("\n=== V=" + s[0] + ", E=" + s[1] + " ===");
            System.out.println("Undirected: reachable=" + a.reachableCount() + "/" + s[0]
                    + ", edgesExamined=" + a.edgesExamined + ", relaxations=" + a.relaxations
                    + ", pushes=" + a.heapPushes + ", comparisons=" + a.comparisons
                    + ", дұрыс=" + Arrays.equals(a.dist, bellmanFord(und, 0)));
            System.out.println("Directed  : reachable=" + b.reachableCount() + "/" + s[0]
                    + ", edgesExamined=" + b.edgesExamined + ", relaxations=" + b.relaxations
                    + ", pushes=" + b.heapPushes + ", comparisons=" + b.comparisons
                    + ", дұрыс=" + Arrays.equals(b.dist, bellmanFord(dir, 0)));
        }
    }

    private static long[] bellmanFord(Graph g, int source) {
        int V = g.getV();
        long[] d = new long[V];
        Arrays.fill(d, DijkstraResult.INF);
        d[source] = 0;
        for (int i = 0; i < V - 1; i++) {
            boolean changed = false;
            for (int u = 0; u < V; u++) {
                if (d[u] == DijkstraResult.INF) continue;
                for (graph.Edge e : g.getNeighbors(u)) {
                    if (d[u] + e.weight < d[e.to]) {
                        d[e.to] = d[u] + e.weight;
                        changed = true;
                    }
                }
            }
            if (!changed) break;
        }
        return d;
    }
}