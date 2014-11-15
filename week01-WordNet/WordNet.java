import java.util.LinkedList;
import java.util.HashMap;

public class WordNet {
    private HashMap<String, LinkedList<Integer>> synmap;
    private HashMap<Integer, String> idSyn;
    private Digraph graph;
    private SAP sap;

    // constructor takes the name of the two input files
    public WordNet(String synsets, String hypernyms) {
        synmap = new HashMap<String, LinkedList<Integer>>();
        idSyn = new HashMap<Integer, String>();
        int synum = 0;
        In syn = new In(synsets);
        while (true) {
            String s = syn.readLine();
            if (s == null)
                break;

            synum++;
            String[] spl = s.split(",");
            int id = Integer.parseInt(spl[0]);
            idSyn.put(id, spl[1]);

            for (String w : spl[1].split(" ")) {
                if (!synmap.containsKey(w))
                    synmap.put(w, new LinkedList());
                synmap.get(w).add(id);
            }
        }

        graph = new Digraph(synum);

        In hyp = new In(hypernyms);
        while (true) {
            String s = hyp.readLine();
            if (s == null)
                break;

            String[] spl = s.split(",");

            int from = Integer.parseInt(spl[0]);
            for (int i = 1; i < spl.length; i++) {
                graph.addEdge(from, Integer.parseInt(spl[i]));
            }
        }

        // sanity check
        // FIXME: Learn more
        if (new DirectedCycle(graph).hasCycle())
            throw new java.lang.IllegalArgumentException();

        boolean getRoot = false;
        for (int i = 0; i < graph.V(); i++) {
            Iterable<Integer> adj = graph.adj(i);
            if (!adj.iterator().hasNext()) {
                if (getRoot)
                    throw new java.lang.IllegalArgumentException();
                else
                    getRoot = true;
            }
        }
        if (!getRoot)
            throw new java.lang.IllegalArgumentException();

        sap = new SAP(graph);
    }

    // returns all WordNet nouns
    public Iterable<String> nouns() {
        return synmap.keySet();
    }

    // is the word a WordNet noun?
    public boolean isNoun(String word) {
        if (word == null)
            throw new java.lang.NullPointerException();
        return synmap.containsKey(word);
    }

    // distance between nounA and nounB (defined below)
    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new java.lang.IllegalArgumentException();

        return sap.length(synmap.get(nounA), synmap.get(nounB));
    }

    // a synset (second field of synsets.txt) that is
    // the common ancestor of nounA and nounB
    // in a shortest ancestral path (defined below)
    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB))
            throw new java.lang.IllegalArgumentException();

        int id = sap.ancestor(synmap.get(nounA), synmap.get(nounB));
        return idSyn.get(id);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        WordNet w = new WordNet(args[0], args[1]);
        StdOut.printf("re: %d\n", w.distance("Black_Plague", "black_marlin"));
    }
}
