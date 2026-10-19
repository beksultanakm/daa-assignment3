package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class TarjanSCC {
    private final Graph g;
    private final int[] disc;
    private final int[] low;
    private final boolean[] onStack;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private int time = 0;
    private final SCCResult result = new SCCResult();

    private TarjanSCC(Graph g) {
        this.g = g;
        int V = g.getV();
        this.disc = new int[V];
        this.low = new int[V];
        this.onStack = new boolean[V];
        Arrays.fill(disc, -1);
        result.componentId = new int[V];
    }

    public static SCCResult run(Graph g) {
        long start = System.nanoTime();
        TarjanSCC t = new TarjanSCC(g);
        for (int v = 0; v < g.getV(); v++) {
            if (t.disc[v] == -1) t.dfs(v);
        }
        t.result.timeNanos = System.nanoTime() - start;
        return t.result;
    }

    private void dfs(int u) {
        disc[u] = low[u] = time++;
        result.verticesVisited++;
        stack.push(u);
        onStack[u] = true;

        for (Edge e : g.getNeighbors(u)) {
            result.edgesExamined++;
            int v = e.to;
            if (disc[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }

        if (low[u] == disc[u]) {
            List<Integer> comp = new ArrayList<>();
            int id = result.components.size();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                result.componentId[w] = id;
                comp.add(w);
            } while (w != u);
            result.components.add(comp);
        }
    }
}