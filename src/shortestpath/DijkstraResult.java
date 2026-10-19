package shortestpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DijkstraResult {
    public static final long INF = Long.MAX_VALUE;

    public final int source;
    public final long[] dist;
    public final int[] parent;

    public long timeNanos = 0;
    public long edgesExamined = 0;
    public long relaxations = 0;
    public long heapPushes = 0;
    public long heapPolls = 0;
    public long comparisons = 0;

    public DijkstraResult(int source, int V) {
        this.source = source;
        this.dist = new long[V];
        this.parent = new int[V];
    }

    public boolean isReachable(int v) {
        return dist[v] != INF;
    }

    public List<Integer> getPath(int v) {
        List<Integer> path = new ArrayList<>();
        if (!isReachable(v)) return path;
        for (int cur = v; cur != -1; cur = parent[cur]) {
            path.add(cur);
        }
        Collections.reverse(path);
        return path;
    }

    public int reachableCount() {
        int c = 0;
        for (long d : dist) if (d != INF) c++;
        return c;
    }
}