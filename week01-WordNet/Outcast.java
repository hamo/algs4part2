public class Outcast {
    private WordNet wn;

    // constructor takes a WordNet object
    public Outcast(WordNet wordnet) {
        wn = wordnet;
    }

    // given an array of WordNet nouns, return an outcast
    public String outcast(String[] nouns) {
        int max = 0;
        int maxNum = 0;

        for (int i = 0; i < nouns.length; i++) {
            int acc = 0;
            for (int j = 0; j < nouns.length; j++)
                acc += wn.distance(nouns[i], nouns[j]);

            if (acc > max) {
                max = acc;
                maxNum = i;
            }
        }

        return nouns[maxNum];
    }

    // see test client below
    public static void main(String[] args) {
        WordNet wordnet = new WordNet(args[0], args[1]);
        Outcast outcast = new Outcast(wordnet);
        for (int t = 2; t < args.length; t++) {
            In in = new In(args[t]);
            String[] nouns = in.readAllStrings();
            StdOut.println(args[t] + ": " + outcast.outcast(nouns));
        }
    }
}
