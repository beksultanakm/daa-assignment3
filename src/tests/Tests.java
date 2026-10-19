package tests;

import connectivity.SCCResult;
import connectivity.TarjanSCC;
import connectivity.TopoResult;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;
import shortestpath.DijkstraResult;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Tests {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testDSU();
        testSmallGraph();
        testDisconnectedGraph();
        testMultipleValidMSTs();
        testDuplicateWeights();
        testParallelEdges();
        testSingleVertex();
        testNoEdges();
        testKruskalEqualsPrimRandom();
        testTopologicalSortDAG();
        testTopologicalSortCycle();
        testSCCKnownGraph();
        testSCCRandom();
        testDijkstraKnownGraph();
        testDijkstraUnreachable();
        testDijkstraNegativeWeight();
        testDijkstraRandom();

        System.out.println("\n==============================");
        System.out.println("Барлығы: " + (passed + failed) + ", өтті: " + passed + ", құлады: " + failed);
        if (failed > 0) {
            System.out.println("КЕЙБІР ТЕСТТЕР ҚҰЛАДЫ!");
            System.exit(1);
        } else {
            System.out.println("БАРЛЫҚ ТЕСТТЕР ӨТТІ!");
        }
    }

    private static void testDSU() {
        section("DSU");
        DSU d = new DSU(6);
        check("басында 6 компонент", d.getComponents() == 6);
        check("union(0,1) бірігеді", d.union(0, 1));
        check("union(1,2) бірігеді", d.union(1, 2));
        check("union(0,2) қайта біріктірмейді (бір жиында)", !d.union(0, 2));
        check("connected(0,2) = true", d.connected(0, 2));
        check("connected(0,3) = false", !d.connected(0, 3));
        check("4 компонент қалды", d.getComponents() == 4);

        boolean[][] flags = {{false, false}, {true, false}, {false, true}, {true, true}};
        boolean same = true;
        Random rnd = new Random(1);
        int n = 200;
        int[][] ops = new int[300][2];
        for (int[] op : ops) { op[0] = rnd.nextInt(n); op[1] = rnd.nextInt(n); }
        int expected = -1;
        for (boolean[] f : flags) {
            DSU x = new DSU(n, f[0], f[1]);
            for (int[] op : ops) x.union(op[0], op[1]);
            if (expected == -1) expected = x.getComponents();
            else if (x.getComponents() != expected) same = false;
        }
        check("4 DSU нұсқасы бірдей компонент санын береді", same);
    }

    private static void testSmallGraph() {
        section("MST: шағын граф (күтілетін салмақ 11)");
        Graph g = new Graph(5, false);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(1, 3, 5);
        g.addEdge(2, 3, 8);
        g.addEdge(3, 4, 3);
        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check("Kruskal салмағы = 11", k.totalWeight == 11);
        check("Prim салмағы = 11", p.totalWeight == 11);
        check("Kruskal: граф байланысты", k.connected);
        check("Prim: граф байланысты", p.connected);
        check("Kruskal: V-1 = 4 қабырға", k.edges.size() == 4);
        check("Kruskal MST - дұрыс spanning tree", isSpanningTree(g, k));
        check("Prim MST - дұрыс spanning tree", isSpanningTree(g, p));
    }

    private static void testDisconnectedGraph() {
        section("MST: байланыссыз граф");
        Graph g = new Graph(6, false);
        g.addEdge(0, 1, 3);
        g.addEdge(1, 2, 1);
        g.addEdge(3, 4, 2);
        g.addEdge(4, 5, 7);
        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check("Kruskal: connected = false", !k.connected);
        check("Prim: connected = false", !p.connected);
        check("Kruskal салмағы (орман) = 13", k.totalWeight == 13);
        check("Prim салмағы (орман) = 13", p.totalWeight == 13);
        check("Орманда V-2 = 4 қабырға (2 компонент)", k.edges.size() == 4 && p.edges.size() == 4);
    }

    private static void testMultipleValidMSTs() {
        section("MST: бірнеше дұрыс MST (барлық салмақ тең)");
        Graph tri = new Graph(3, false);
        tri.addEdge(0, 1, 5);
        tri.addEdge(1, 2, 5);
        tri.addEdge(0, 2, 5);
        check("үшбұрыш: Kruskal = 10", Kruskal.run(tri).totalWeight == 10);
        check("үшбұрыш: Prim = 10", Prim.run(tri).totalWeight == 10);

        Graph sq = new Graph(4, false);
        sq.addEdge(0, 1, 1);
        sq.addEdge(1, 2, 1);
        sq.addEdge(2, 3, 1);
        sq.addEdge(3, 0, 1);
        sq.addEdge(0, 2, 1);
        MSTResult k = Kruskal.run(sq);
        MSTResult p = Prim.run(sq);
        check("төртбұрыш: салмақ екеуінде де 3", k.totalWeight == 3 && p.totalWeight == 3);
        check("төртбұрыш: екеуі де дұрыс spanning tree", isSpanningTree(sq, k) && isSpanningTree(sq, p));
    }

    private static void testDuplicateWeights() {
        section("MST: қайталанатын салмақтар");
        boolean allEqual = true;
        boolean allTrees = true;
        for (int seed = 1; seed <= 30; seed++) {
            Graph g = GraphGenerator.connectedUndirected(30, 120, seed, 3);
            MSTResult k = Kruskal.run(g);
            MSTResult p = Prim.run(g);
            if (k.totalWeight != p.totalWeight) allEqual = false;
            if (!isSpanningTree(g, k) || !isSpanningTree(g, p)) allTrees = false;
        }
        check("30 граф (салмақ 1..3): Kruskal = Prim", allEqual);
        check("30 граф (салмақ 1..3): екеуі де spanning tree", allTrees);
    }

    private static void testParallelEdges() {
        section("MST: екі төбе арасында бірнеше қабырға");
        Graph g = new Graph(2, false);
        g.addEdge(0, 1, 5);
        g.addEdge(0, 1, 3);
        check("Kruskal арзанын таңдайды (3)", Kruskal.run(g).totalWeight == 3);
        check("Prim арзанын таңдайды (3)", Prim.run(g).totalWeight == 3);
    }

    private static void testSingleVertex() {
        section("Бір төбелі граф");
        Graph u = new Graph(1, false);
        MSTResult k = Kruskal.run(u);
        MSTResult p = Prim.run(u);
        check("Kruskal: салмақ 0, қабырға 0, connected", k.totalWeight == 0 && k.edges.isEmpty() && k.connected);
        check("Prim: салмақ 0, қабырға 0, connected", p.totalWeight == 0 && p.edges.isEmpty() && p.connected);

        Graph d = new Graph(1, true);
        TopoResult t = TopologicalSort.run(d);
        check("Топологиялық сұрыптау: DAG, реттілік [0]", t.isDAG && t.order.equals(List.of(0)));
        SCCResult s = TarjanSCC.run(d);
        check("SCC: бір компонент [0]", s.count() == 1 && s.components.get(0).equals(List.of(0)));
        DijkstraResult r = Dijkstra.run(d, 0);
        check("Dijkstra: dist[0] = 0, жол [0]", r.dist[0] == 0 && r.getPath(0).equals(List.of(0)));
    }

    private static void testNoEdges() {
        section("Қабырғасыз граф (3 төбе)");
        Graph g = new Graph(3, false);
        MSTResult k = Kruskal.run(g);
        MSTResult p = Prim.run(g);
        check("Kruskal: connected = false, салмақ 0", !k.connected && k.totalWeight == 0);
        check("Prim: connected = false, салмақ 0", !p.connected && p.totalWeight == 0);
        SCCResult s = TarjanSCC.run(new Graph(3, true));
        check("SCC: 3 жеке компонент", s.count() == 3);
    }
    private static void testKruskalEqualsPrimRandom() {
        section("Kruskal = Prim (100 кездейсоқ байланысты граф)");
        Random rnd = new Random(123);
        boolean equal = true, trees = true, connected = true;
        for (int i = 0; i < 100; i++) {
            int V = 2 + rnd.nextInt(80);
            int maxE = Math.min(V * (V - 1) / 2, 5 * V);
            int E = (V - 1) + rnd.nextInt(maxE - (V - 1) + 1);
            Graph g = GraphGenerator.connectedUndirected(V, E, rnd.nextLong(), 100);
            MSTResult k = Kruskal.run(g);
            MSTResult p = Prim.run(g);
            if (k.totalWeight != p.totalWeight) equal = false;
            if (!isSpanningTree(g, k) || !isSpanningTree(g, p)) trees = false;
            if (!k.connected || !p.connected) connected = false;
        }
        check("барлығында Kruskal салмағы = Prim салмағы", equal);
        check("барлығында екеуі де spanning tree", trees);
        check("барлығында граф байланысты деп танылды", connected);
    }

    private static void testTopologicalSortDAG() {
        section("Топологиялық сұрыптау: DAG");
        Graph dag = new Graph(5, true);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 3, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(3, 4, 1);
        TopoResult r = TopologicalSort.run(dag);
        check("қолмен жасалған DAG: isDAG = true", r.isDAG);
        check("қолмен жасалған DAG: реттілік дұрыс", TopologicalSort.isValidOrder(dag, r.order));

        Random rnd = new Random(7);
        boolean ok = true;
        for (int i = 0; i < 100; i++) {
            int V = 2 + rnd.nextInt(80);
            int E = rnd.nextInt(Math.min(V * (V - 1) / 2, 4 * V) + 1);
            Graph g = GraphGenerator.randomDAG(V, E, rnd.nextLong(), 10);
            TopoResult t = TopologicalSort.run(g);
            if (!t.isDAG || !TopologicalSort.isValidOrder(g, t.order)) ok = false;
        }
        check("100 кездейсоқ DAG: әрқайсысында реттілік дұрыс", ok);
    }

    private static void testTopologicalSortCycle() {
        section("Топологиялық сұрыптау: циклі бар граф");
        Graph cyc = new Graph(4, true);
        cyc.addEdge(0, 1, 1);
        cyc.addEdge(1, 2, 1);
        cyc.addEdge(2, 0, 1);
        cyc.addEdge(2, 3, 1);
        TopoResult r = TopologicalSort.run(cyc);
        check("цикл анықталды (isDAG = false)", !r.isDAG);
        check("циклде реттілік қайтарылмайды (бос)", r.order.isEmpty());

        Graph self = new Graph(2, true);
        self.addEdge(0, 0, 1); // self-loop - ең қысқа цикл
        check("self-loop цикл деп танылады", !TopologicalSort.run(self).isDAG);

        Random rnd = new Random(11);
        boolean agree = true;
        for (int i = 0; i < 100; i++) {
            int V = 2 + rnd.nextInt(60);
            int E = rnd.nextInt(Math.min(V * (V - 1), 3 * V) + 1);
            Graph g = GraphGenerator.randomDirected(V, E, rnd.nextLong(), 10);
            boolean dagByKahn = TopologicalSort.run(g).isDAG;
            boolean dagBySCC = TarjanSCC.run(g).count() == V;
            if (dagByKahn != dagBySCC) agree = false;
        }
        check("100 кездейсоқ граф: Kahn мен Tarjan нәтижелері сәйкес", agree);
    }

    private static void testSCCKnownGraph() {
        section("SCC: белгілі граф");
        Graph g = new Graph(7, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        g.addEdge(1, 3, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(4, 5, 1);
        g.addEdge(5, 3, 1);
        g.addEdge(5, 6, 1);
        SCCResult r = TarjanSCC.run(g);
        Set<Set<Integer>> found = new HashSet<>();
        for (List<Integer> c : r.components) found.add(new HashSet<>(c));
        Set<Set<Integer>> expected = new HashSet<>();
        expected.add(Set.of(0, 1, 2));
        expected.add(Set.of(3, 4, 5));
        expected.add(Set.of(6));
        check("SCC = {0,1,2}, {3,4,5}, {6}", found.equals(expected));
        check("SCC саны = 3", r.count() == 3);
    }

    private static void testSCCRandom() {
        section("SCC: әр төбе дәл бір компонентте");
        Random rnd = new Random(5);
        boolean partition = true, idsMatch = true;
        for (int i = 0; i < 100; i++) {
            int V = 1 + rnd.nextInt(80);
            int E = V < 2 ? 0 : rnd.nextInt(Math.min(V * (V - 1), 4 * V) + 1);
            Graph g = GraphGenerator.randomDirected(V, E, rnd.nextLong(), 10);
            SCCResult r = TarjanSCC.run(g);
            int[] seen = new int[V];
            for (int c = 0; c < r.components.size(); c++) {
                for (int v : r.components.get(c)) {
                    seen[v]++;
                    if (r.componentId[v] != c) idsMatch = false;
                }
            }
            for (int s : seen) if (s != 1) partition = false;
        }
        check("100 граф: әр төбе дәл 1 SCC-де", partition);
        check("100 граф: componentId тізіммен сәйкес", idsMatch);
    }

    private static void testDijkstraKnownGraph() {
        section("Dijkstra: белгілі граф");
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 1, 2);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 5);
        g.addEdge(3, 4, 3);
        DijkstraResult r = Dijkstra.run(g, 0);
        check("қашықтықтар: [0, 3, 1, 4, 7]", Arrays.equals(r.dist, new long[]{0, 3, 1, 4, 7}));
        check("0 -> 4 жолы: [0, 2, 1, 3, 4]", r.getPath(4).equals(List.of(0, 2, 1, 3, 4)));
    }

    private static void testDijkstraUnreachable() {
        section("Dijkstra: қолжетімсіз төбелер");
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 2);
        g.addEdge(3, 4, 1); // 3 пен 4 - 0-ден қолжетімсіз
        DijkstraResult r = Dijkstra.run(g, 0);
        check("2 қолжетімсіз (INF)", !r.isReachable(2) && r.dist[2] == DijkstraResult.INF);
        check("3 және 4 қолжетімсіз", !r.isReachable(3) && !r.isReachable(4));
        check("қолжетімсіз төбенің жолы бос", r.getPath(3).isEmpty());
        check("қолжетімді төбелер саны = 2", r.reachableCount() == 2);
    }

    private static void testDijkstraNegativeWeight() {
        section("Dijkstra: теріс салмақ");
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 4);
        g.addEdge(1, 2, -2);
        boolean thrown = false;
        try {
            Dijkstra.run(g, 0);
        } catch (IllegalArgumentException e) {
            thrown = true;
        }
        check("теріс салмақта қате шығарады", thrown);
    }

    private static void testDijkstraRandom() {
        section("Dijkstra = Bellman-Ford (200 кездейсоқ граф)");
        Random rnd = new Random(99);
        boolean distOk = true, pathOk = true;
        for (int i = 0; i < 200; i++) {
            int V = 2 + rnd.nextInt(60);
            Graph g;
            if (i % 2 == 0) {
                int maxE = Math.min(V * (V - 1) / 2, 5 * V);
                g = GraphGenerator.connectedUndirected(V, (V - 1) + rnd.nextInt(maxE - (V - 1) + 1), rnd.nextLong(), 50);
            } else {
                g = GraphGenerator.randomDirected(V, rnd.nextInt(Math.min(V * (V - 1), 4 * V) + 1), rnd.nextLong(), 50);
            }
            int source = rnd.nextInt(V);
            DijkstraResult r = Dijkstra.run(g, source);
            if (!Arrays.equals(r.dist, bellmanFord(g, source))) distOk = false;
            for (int v = 0; v < V; v++) {
                if (!r.isReachable(v)) continue;
                List<Integer> path = r.getPath(v);
                if (path.get(0) != source || path.get(path.size() - 1) != v
                        || pathWeight(g, path) != r.dist[v]) pathOk = false;
            }
        }
        check("барлық қашықтықтар Bellman-Ford нәтижесімен тең", distOk);
        check("әр жолдың ұзындығы dist-ке тең және дұрыс төбелерден басталып аяқталады", pathOk);
    }
    private static boolean isSpanningTree(Graph g, MSTResult r) {
        int V = g.getV();
        if (r.edges.size() != V - 1) return false;
        DSU d = new DSU(V);
        for (Edge e : r.edges) {
            if (!d.union(e.from, e.to)) return false;
        }
        return d.getComponents() == 1;
    }

    private static long pathWeight(Graph g, List<Integer> path) {
        long sum = 0;
        for (int i = 0; i + 1 < path.size(); i++) {
            int a = path.get(i), b = path.get(i + 1);
            long best = Long.MAX_VALUE;
            for (Edge e : g.getNeighbors(a)) {
                if (e.to == b) best = Math.min(best, e.weight);
            }
            if (best == Long.MAX_VALUE) return -1; // жол жоқ қабырғамен жүріп тұр
            sum += best;
        }
        return sum;
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
                for (Edge e : g.getNeighbors(u)) {
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

    private static void section(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("[OK]   " + name);
        } else {
            failed++;
            System.out.println("[FAIL] " + name);
        }
    }
}