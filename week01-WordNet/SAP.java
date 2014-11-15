public class SAP {
    private class ResultCache {
        private int length;
        private int ancestor;

        public ResultCache(int l, int a) {
            length = l;
            ancestor = a;
        }

        public int length() {
            return length;
        }

        public int ancestor() {
            return ancestor;
        }
    }

    private Digraph g;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        g = new Digraph(G);
    }

    private ResultCache lengthAncestor(int v, int w) {
        if (v == w)
            return new ResultCache(0, v);

        BreadthFirstDirectedPaths pv = new BreadthFirstDirectedPaths(g, v);
        BreadthFirstDirectedPaths pw = new BreadthFirstDirectedPaths(g, w);

        int l = Integer.MAX_VALUE;
        int a = -1;

        for (int i = 0; i < g.V(); i++) {
            if (pv.hasPathTo(i) && pw.hasPathTo(i)) {
                int tmpl = pv.distTo(i) + pw.distTo(i);
                if (tmpl < l) {
                    l = tmpl;
                    a = i;
                }
            }
        }

        if (l == Integer.MAX_VALUE)
            return new ResultCache(-1, -1);
        else
            return new ResultCache(l, a);
    }

    private ResultCache lengthAncestor(Iterable<Integer> v, Iterable<Integer> w) {
        BreadthFirstDirectedPaths pv = new BreadthFirstDirectedPaths(g, v);
        BreadthFirstDirectedPaths pw = new BreadthFirstDirectedPaths(g, w);

        int l = Integer.MAX_VALUE;
        int a = -1;

        for (int i = 0; i < g.V(); i++) {
            if (pv.hasPathTo(i) && pw.hasPathTo(i)) {
                int tmpl = pv.distTo(i) + pw.distTo(i);
                if (tmpl < l) {
                    l = tmpl;
                    a = i;
                }
            }
        }

        if (l == Integer.MAX_VALUE)
            return new ResultCache(-1, -1);
        else
            return new ResultCache(l, a);
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        return lengthAncestor(v, w).length();
    }

    // a common ancestor of v and w that participates
    // in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        return lengthAncestor(v, w).ancestor();
    }

    // length of shortest ancestral path between any vertex
    // in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        return lengthAncestor(v, w).length();
    }

    // a common ancestor that participates
    // in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        return lengthAncestor(v, w).ancestor();
    }

    // do unit testing of this class
    public static void main(String[] args) {
        In in = new In(args[0]);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
        while (!StdIn.isEmpty()) {
            int v = StdIn.readInt();
            int w = StdIn.readInt();
            int length   = sap.length(v, w);
            int ancestor = sap.ancestor(v, w);
            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
        }
    }
}
