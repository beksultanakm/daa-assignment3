import connectivity.SCCResult;
import connectivity.TarjanSCC;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    static final long SEED = 42;
    static final int RUNS = 5;
    static final int WARMUP = 50;
    static final int MAX_W = 100;
    static final String DIR = "results/tables";

    static final String[] NAMES = {"Small", "Medium", "Large"};
    static final int[] VS = {10, 100, 1000};
    static final int[] ES = {30, 500, 5000};

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get(DIR));
        mstTable();
        dsuAblationTable();
        dsuMicroTable();
        sccTable();
        topoTable();
        dijkstraTable();
        scalingTable();
        System.out.println("Done. CSV files are in " + DIR);
    }

    private static PrintWriter open(String name) throws IOException {
        return new PrintWriter(Files.newBufferedWriter(Paths.get(DIR, name)));
    }

    private static String f(String fmt, Object... a) {
        return String.format(Locale.US, fmt, a);
    }

    private static MSTResult runMst(String algo, Graph g) {
        return algo.equals("Kruskal") ? Kruskal.run(g) : Prim.run(g);
    }

    private static void mstTable() throws IOException {
        try (PrintWriter out = open("mst.csv")) {
            out.println("graph,V,E,algorithm,avg_time_us,total_weight,connected,"
                    + "edges_examined,comparisons,dsu_finds,dsu_unions,heap_pushes,heap_polls");
            for (int i = 0; i < VS.length; i++) {
                Graph g = GraphGenerator.connectedUndirected(VS[i], ES[i], SEED + VS[i], MAX_W);
                long[] weights = new long[2];
                int idx = 0;
                for (String algo : new String[]{"Kruskal", "Prim"}) {
                    for (int w = 0; w < WARMUP; w++) runMst(algo, g);
                    long total = 0;
                    MSTResult r = null;
                    for (int k = 0; k < RUNS; k++) {
                        r = runMst(algo, g);
                        total += r.timeNanos;
                    }
                    weights[idx++] = r.totalWeight;
                    out.println(f("%s,%d,%d,%s,%.3f,%d,%b,%d,%d,%d,%d,%d,%d",
                            NAMES[i], VS[i], ES[i], algo, total / (double) RUNS / 1000.0,
                            r.totalWeight, r.connected, r.edgesExamined, r.comparisons,
                            r.dsuFinds, r.dsuUnions, r.heapPushes, r.heapPolls));
                }
                System.out.println(NAMES[i] + ": Kruskal weight=" + weights[0]
                        + ", Prim weight=" + weights[1]
                        + (weights[0] == weights[1] ? "  [OK]" : "  [MISMATCH!]"));
            }
        }
    }

    private static void dsuAblationTable() throws IOException {
        boolean[][] configs = {{false, false}, {true, false}, {false, true}, {true, true}};
        try (PrintWriter out = open("dsu_kruskal.csv")) {
            out.println("graph,V,E,path_compression,union_by_rank,avg_time_us,dsu_finds,dsu_unions");
            for (int i = 0; i < VS.length; i++) {
                Graph g = GraphGenerator.connectedUndirected(VS[i], ES[i], SEED + VS[i], MAX_W);
                for (boolean[] c : configs) {
                    for (int w = 0; w < WARMUP; w++) Kruskal.run(g, c[0], c[1]);
                    long total = 0;
                    MSTResult r = null;
                    for (int k = 0; k < RUNS; k++) {
                        r = Kruskal.run(g, c[0], c[1]);
                        total += r.timeNanos;
                    }
                    out.println(f("%s,%d,%d,%b,%b,%.3f,%d,%d", NAMES[i], VS[i], ES[i], c[0], c[1],
                            total / (double) RUNS / 1000.0, r.dsuFinds, r.dsuUnions));
                }
            }
        }
    }

    private static void dsuMicroTable() throws IOException {
        final int N = 10_000;
        final int RANDOM_OPS = 50_000;
        boolean[][] configs = {{false, false}, {true, false}, {false, true}, {true, true}};
        try (PrintWriter out = open("dsu_micro.csv")) {
            out.println("path_compression,union_by_rank,n,ops,avg_time_us,find_calls,parent_steps");
            for (boolean[] c : configs) {
                try {
                    for (int w = 0; w < 3; w++) dsuStress(N, RANDOM_OPS, c);
                    long total = 0;
                    long finds = 0;
                    for (int k = 0; k < RUNS; k++) {
                        long start = System.nanoTime();
                        finds = dsuStress(N, RANDOM_OPS, c);
                        total += System.nanoTime() - start;
                    }
                    out.println(f("%b,%b,%d,%d,%.3f,%d,%d", c[0], c[1], N, N - 1 + RANDOM_OPS,
                            total / (double) RUNS / 1000.0, finds, lastSteps));
                } catch (StackOverflowError e) {
                    out.println(f("%b,%b,%d,%d,StackOverflow,,", c[0], c[1], N, N - 1 + RANDOM_OPS));
                }
            }
        }
    }

    static long lastSteps;

    private static long dsuStress(int n, int ops, boolean[] c) {
        DSU d = new DSU(n, c[0], c[1]);
        for (int i = 0; i + 1 < n; i++) d.union(i + 1, i);
        Random rnd = new Random(SEED);
        for (int i = 0; i < ops; i++) d.union(rnd.nextInt(n), rnd.nextInt(n));
        lastSteps = d.parentSteps;
        return d.findCalls;
    }

    private static void sccTable() throws IOException {
        try (PrintWriter out = open("scc.csv")) {
            out.println("graph,V,E,avg_time_us,num_scc,largest_scc,vertices_visited,edges_examined");
            for (int i = 0; i < VS.length; i++) {
                Graph g = GraphGenerator.randomDirected(VS[i], ES[i], SEED + VS[i], MAX_W);
                for (int w = 0; w < WARMUP; w++) TarjanSCC.run(g);
                long total = 0;
                SCCResult r = null;
                for (int k = 0; k < RUNS; k++) {
                    r = TarjanSCC.run(g);
                    total += r.timeNanos;
                }
                int largest = 0;
                for (List<Integer> comp : r.components) largest = Math.max(largest, comp.size());
                out.println(f("%s,%d,%d,%.3f,%d,%d,%d,%d", NAMES[i], VS[i], ES[i],
                        total / (double) RUNS / 1000.0, r.components.size(), largest,
                        r.verticesVisited, r.edgesExamined));
            }
        }
    }

    private static void topoTable() throws IOException {
        try (PrintWriter out = open("topo.csv")) {
            out.println("graph,V,E,type,avg_time_us,is_dag,order_length,valid_order,"
                    + "vertices_processed,edges_examined");
            for (int i = 0; i < VS.length; i++) {
                Graph dag = GraphGenerator.randomDAG(VS[i], ES[i], SEED + VS[i], MAX_W);
                Graph cyc = GraphGenerator.randomDirected(VS[i], ES[i], SEED + VS[i], MAX_W);
                Graph[] gs = {dag, cyc};
                String[] types = {"DAG", "random_directed"};
                for (int t = 0; t < 2; t++) {
                    for (int w = 0; w < WARMUP; w++) TopologicalSort.run(gs[t]);
                    long total = 0;
                    TopoResult r = null;
                    for (int k = 0; k < RUNS; k++) {
                        r = TopologicalSort.run(gs[t]);
                        total += r.timeNanos;
                    }
                    boolean valid = r.isDAG && TopologicalSort.isValidOrder(gs[t], r.order);
                    out.println(f("%s,%d,%d,%s,%.3f,%b,%d,%b,%d,%d", NAMES[i], VS[i], ES[i],
                            types[t], total / (double) RUNS / 1000.0, r.isDAG, r.order.size(),
                            valid, r.verticesProcessed, r.edgesExamined));
                }
            }
        }
    }

    private static void dijkstraTable() throws IOException {
        try (PrintWriter out = open("dijkstra.csv")) {
            out.println("graph,V,E,avg_time_us,reachable,edges_examined,relaxations,"
                    + "comparisons,heap_pushes,heap_polls");
            for (int i = 0; i < VS.length; i++) {
                Graph g = GraphGenerator.randomDirected(VS[i], ES[i], SEED + VS[i], MAX_W);
                for (int w = 0; w < WARMUP; w++) Dijkstra.run(g, 0);
                long total = 0;
                DijkstraResult r = null;
                for (int k = 0; k < RUNS; k++) {
                    r = Dijkstra.run(g, 0);
                    total += r.timeNanos;
                }
                int reachable = 0;
                for (long d : r.dist) if (d != DijkstraResult.INF) reachable++;
                out.println(f("%s,%d,%d,%.3f,%d,%d,%d,%d,%d,%d", NAMES[i], VS[i], ES[i],
                        total / (double) RUNS / 1000.0, reachable, r.edgesExamined,
                        r.relaxations, r.comparisons, r.heapPushes, r.heapPolls));
            }
        }
    }

    private static final String[] ALGOS = {"Kruskal", "Prim", "Tarjan SCC", "Topological sort", "Dijkstra"};

    private static Graph make(String algo, int V, int E) {
        long s = SEED + V;
        switch (algo) {
            case "Kruskal":
            case "Prim":
                return GraphGenerator.connectedUndirected(V, E, s, MAX_W);
            case "Topological sort":
                return GraphGenerator.randomDAG(V, E, s, MAX_W);
            default:
                return GraphGenerator.randomDirected(V, E, s, MAX_W);
        }
    }

    private static long timeOf(String algo, Graph g) {
        switch (algo) {
            case "Kruskal": return Kruskal.run(g).timeNanos;
            case "Prim": return Prim.run(g).timeNanos;
            case "Tarjan SCC": return TarjanSCC.run(g).timeNanos;
            case "Topological sort": return TopologicalSort.run(g).timeNanos;
            default: return Dijkstra.run(g, 0).timeNanos;
        }
    }

    private static double avgUs(String algo, Graph g) {
        for (int w = 0; w < WARMUP; w++) timeOf(algo, g);
        long total = 0;
        for (int k = 0; k < RUNS; k++) total += timeOf(algo, g);
        return total / (double) RUNS / 1000.0;
    }

    private static void scalingTable() throws IOException {
        try (PrintWriter out = open("scaling.csv")) {
            out.println("sweep,algorithm,V,E,avg_time_us");
            // Sweep 1: E = 5V, V grows
            int[] vs = {100, 250, 500, 1000, 2000, 4000};
            for (int V : vs) {
                for (String a : ALGOS) {
                    Graph g = make(a, V, 5 * V);
                    out.println(f("vary_V,%s,%d,%d,%.3f", a, V, 5 * V, avgUs(a, g)));
                }
            }

            int[] es = {1000, 2500, 5000, 10000, 20000, 40000};
            for (int E : es) {
                for (String a : ALGOS) {
                    Graph g = make(a, 1000, E);
                    out.println(f("vary_E,%s,%d,%d,%.3f", a, 1000, E, avgUs(a, g)));
                }
            }
        }
    }
}
