package mst;

import graph.Edge;
import java.util.ArrayList;
import java.util.List;

public class MSTResult {
    public final List<Edge> edges = new ArrayList<>();
    public long totalWeight = 0;
    public boolean connected = false;
    public long timeNanos = 0;

    public long edgesExamined = 0;
    public long comparisons = 0;
    public long dsuFinds = 0;
    public long dsuUnions = 0;
    public long heapPushes = 0;
    public long heapPolls = 0;

    public void add(Edge e) {
        edges.add(e);
        totalWeight += e.weight;
    }
}