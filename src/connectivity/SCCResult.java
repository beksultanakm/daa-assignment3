package connectivity;

import java.util.ArrayList;
import java.util.List;

public class SCCResult {
    public final List<List<Integer>> components = new ArrayList<>();
    public int[] componentId;
    public long timeNanos = 0;

    public long edgesExamined = 0;
    public long verticesVisited = 0;

    public int count() {
        return components.size();
    }
}