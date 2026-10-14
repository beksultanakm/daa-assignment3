package connectivity;

import java.util.ArrayList;
import java.util.List;

public class TopoResult {
    public final List<Integer> order = new ArrayList<>();
    public boolean isDAG = false;
    public long timeNanos = 0;


    public long edgesExamined = 0;
    public long verticesProcessed = 0;
}