import java.util.HashMap;
import java.util.Vector;

public class BaseballElimination {

    private int teamNumber;

    private HashMap<String, Integer> teamMap;
    private String[] teamIndex;

    private int maxWin = Integer.MIN_VALUE;
    private int[] winByTeam;
    private int[] losseByTeam;
    private int[] remainingByTeam;

    private int[][] gameMatrix;

    private FordFulkerson[] ffCache;

    // create a baseball division from given filename in format specified below
    public BaseballElimination(String filename) {
        if (filename == null)
            throw new java.lang.NullPointerException();

        In reader = new In(filename);
        if (!reader.exists())
            throw new java.lang.IllegalArgumentException();

        teamMap = new HashMap<String, Integer>();

        teamNumber = reader.readInt();

        teamIndex = new String[teamNumber];

        winByTeam = new int[teamNumber];
        losseByTeam = new int[teamNumber];
        remainingByTeam = new int[teamNumber];

        gameMatrix = new int[teamNumber][teamNumber];
        ffCache = new FordFulkerson[teamNumber];
        for (int i = 0; i < teamNumber; i++)
            ffCache[i] = null;

        for (int i = 0; i < teamNumber; i++) {
            String team = reader.readString();
            teamMap.put(team, i);
            teamIndex[i] = team;
            winByTeam[i] = reader.readInt();
            if (winByTeam[i] > maxWin)
                maxWin = winByTeam[i];
            losseByTeam[i] = reader.readInt();
            remainingByTeam[i] = reader.readInt();

            for (int j = 0; j < teamNumber; j++) {
                gameMatrix[i][j] = reader.readInt();
            }
        }
    }

    // number of teams
    public int numberOfTeams() {
        return teamNumber;
    }

    // all teams
    public Iterable<String> teams() {
        return teamMap.keySet();
    }

    private int getTeam(String team) {
        if (!teamMap.containsKey(team))
            throw new java.lang.IllegalArgumentException();

        return teamMap.get(team);
    }

    // number of wins for given team
    public int wins(String team) {
        int teamN = getTeam(team);
        return winByTeam[teamN];
    }

    // number of losses for given team
    public int losses(String team) {
        int teamN = getTeam(team);
        return losseByTeam[teamN];
    }

    // number of remaining games for given team
    public int remaining(String team) {
        int teamN = getTeam(team);
        return remainingByTeam[teamN];
    }

    // number of remaining games between team1 and team2
    public int against(String team1, String team2) {
        int team1N = getTeam(team1);
        int team2N = getTeam(team2);

        return gameMatrix[team1N][team2N];
    }

    private void initFF(int teamN) {
        int s = 1;
        int against = (teamNumber-1)*(teamNumber-2)/2;
        int teams = teamNumber - 1;
        int t = 1;

        FlowNetwork fn = new FlowNetwork(s+against+teams+t);

        int againstStart = 1;
        int againstEnd = againstStart + against;

        int curr = againstStart;
        for (int i = 0; i < teamNumber; i++) {
            if (i == teamN)
                continue;
            for (int j = i+1; j < teamNumber; j++) {
                if (j == teamN)
                    continue;

                FlowEdge fe = new FlowEdge(0, curr, gameMatrix[i][j]);
                fn.addEdge(fe);

                FlowEdge fei, fej;

                if (i < teamN) {
                    fei = new FlowEdge(curr, againstEnd + i, Double.POSITIVE_INFINITY);
                } else {
                    fei = new FlowEdge(curr, againstEnd + i - 1, Double.POSITIVE_INFINITY);
                }
                fn.addEdge(fei);

                if (j < teamN) {
                    fej = new FlowEdge(curr, againstEnd + j, Double.POSITIVE_INFINITY);
                } else {
                    fej = new FlowEdge(curr, againstEnd + j - 1, Double.POSITIVE_INFINITY);
                }
                fn.addEdge(fej);

                curr++;
            }
        }

        for (int i = 0; i < teamNumber; i++) {
            FlowEdge fet;
            if (i < teamN) {
                fet = new FlowEdge(againstEnd+i, s+against+teams+t-1, winByTeam[teamN]+remainingByTeam[teamN]-winByTeam[i]);
            } else if (i > teamN) {
                fet = new FlowEdge(againstEnd+i-1, s+against+teams+t-1, winByTeam[teamN]+remainingByTeam[teamN]-winByTeam[i]);
            } else
                continue;

            fn.addEdge(fet);
        }

        ffCache[teamN] = new FordFulkerson(fn, 0, s+against+teams+t-1);
    }

    // is given team eliminated?
    public boolean isEliminated(String team) {
        int teamN = getTeam(team);

        // basic check
        if (winByTeam[teamN] + remainingByTeam[teamN] < maxWin)
            return true;

        if (ffCache[teamN] == null)
            initFF(teamN);

        int desValue = 0;
        for (int i = 0; i < teamNumber; i++) {
            if (i == teamN)
                continue;
            for (int j = i+1; j < teamNumber; j++) {
                if (j == teamN)
                    continue;
                desValue += gameMatrix[i][j];
            }
        }

        //        StdOut.printf("desValue: %d, value: %f\n", desValue, ffCache[teamN].value());

        return ffCache[teamN].value() != desValue;
    }

    // subset R of teams that eliminates given team; null if not eliminated
    public Iterable<String> certificateOfElimination(String team) {
        int teamN = getTeam(team);

        Vector<String> re = null;

        // trival
        if (winByTeam[teamN] + remainingByTeam[teamN] < maxWin) {
            for (int i = 0; i < teamNumber; i++)
                if (winByTeam[i] == maxWin) {
                    re = new Vector<String>();
                    re.add(teamIndex[i]);
                    return re;
                }
        }

        if (!isEliminated(team))
            return re;
        else {
            re = new Vector<String>();
            int against = (teamNumber-1)*(teamNumber-2)/2;
            int againstStart = 1;
            int againstEnd = againstStart + against;

            for (int i = 0; i < teamNumber; i++) {
                if (i == teamN)
                    continue;

                if (i < teamN) {
                    if (ffCache[teamN].inCut(againstEnd+i)) {
                        re.add(teamIndex[i]);
                    }
                } else {
                    if (ffCache[teamN].inCut(againstEnd+i-1)) {
                        re.add(teamIndex[i]);
                    }
                }
            }

            return re;
        }
    }


    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination(args[0]);
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
