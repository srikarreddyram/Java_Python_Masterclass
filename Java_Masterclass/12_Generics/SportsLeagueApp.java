/*
 * ============================================================================
 *  SECTION 12 : REAL-WORLD APP - Type-safe Sports League Manager
 * ============================================================================
 *
 *  One League class serves cricket, football and kabaddi - but the compiler
 *  stops you from putting a football team in a cricket league.
 *
 *    interface Player                       (bound for every player type)
 *      CricketPlayer, FootballPlayer
 *
 *    class Team<P extends Player> implements Comparable<Team<P>>
 *        roster of P, points table logic
 *
 *    class League<T extends Team<?>>
 *        schedule, record results, standings (sorted via Comparable)
 *
 *    class Repository<ID, E>   a tiny generic in-memory store, reused for
 *                              players AND teams (generic code written once)
 *
 *  CONCEPTS USED: bounded type parameters, generic methods, Comparable<T>,
 *  wildcards (Team<?>), generic records, invariance, a reusable generic repo.
 *
 *  HOW TO RUN:  javac SportsLeagueApp.java && java SportsLeagueApp
 * ============================================================================
 */
import java.util.*;
import java.util.function.Predicate;

public class SportsLeagueApp {

    // ============================ players ============================
    interface Player { String name(); int rating(); }
    record CricketPlayer(String name, String role, int rating) implements Player { }
    record FootballPlayer(String name, String position, int rating) implements Player { }

    // ============================ generic team ============================
    static class Team<P extends Player> implements Comparable<Team<P>> {
        private final String name;
        private final List<P> roster = new ArrayList<>();
        private int played, won, drawn, lost, scored, conceded;

        Team(String name) { this.name = name; }

        Team<P> sign(P player) { roster.add(player); return this; }   // only P allowed

        int points() { return won * 3 + drawn; }
        int diff() { return scored - conceded; }
        double avgRating() { return roster.stream().mapToInt(Player::rating).average().orElse(0); }
        P star() { return Collections.max(roster, Comparator.comparingInt(Player::rating)); }   // returns P, not Player

        void record(int forGoals, int againstGoals) {
            played++; scored += forGoals; conceded += againstGoals;
            if (forGoals > againstGoals) won++;
            else if (forGoals == againstGoals) drawn++;
            else lost++;
        }

        @Override
        public int compareTo(Team<P> other) {                       // points, then goal difference, then name
            if (points() != other.points()) return Integer.compare(other.points(), points());
            if (diff() != other.diff()) return Integer.compare(other.diff(), diff());
            return name.compareTo(other.name);
        }

        String name() { return name; }

        String tableRow() {                                         // keeps the counters private
            return String.format("%-16s %2d %2d %2d %2d %+3d  %3d", name, played, won, drawn, lost, diff(), points());
        }
    }

    // ============================ generic league ============================
    static class League<T extends Team<?>> {
        private final String title;
        private final List<T> teams = new ArrayList<>();
        League(String title) { this.title = title; }

        void add(T team) { teams.add(team); }

        /** Deterministic "result" from ratings so the demo is reproducible. */
        void playRoundRobin() {
            for (int i = 0; i < teams.size(); i++)
                for (int j = i + 1; j < teams.size(); j++) {
                    T a = teams.get(i), b = teams.get(j);
                    int ga = (int) Math.round(a.avgRating() / 30 + (i + j) % 2);
                    int gb = (int) Math.round(b.avgRating() / 30 + (i * j) % 2);
                    a.record(ga, gb);
                    b.record(gb, ga);
                }
        }

        void printTable() {
            List<T> sorted = new ArrayList<>(teams);
            sorted.sort(League::compareTeams);
            System.out.println("=== " + title + " ===");
            System.out.println("  Pos Team              P  W  D  L  GD  Pts  Star player");
            int pos = 1;
            for (T t : sorted)
                System.out.printf("  %2d. %s  %s%n", pos++, t.tableRow(), t.star().name());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private static int compareTeams(Team<?> a, Team<?> b) { return ((Team) a).compareTo(b); }
    }

    // ============================ reusable generic repository ============================
    static class Repository<ID, E> {
        private final Map<ID, E> store = new LinkedHashMap<>();
        void save(ID id, E entity) { store.put(id, entity); }
        Optional<E> findById(ID id) { return Optional.ofNullable(store.get(id)); }
        List<E> findAll(Predicate<? super E> filter) {              // ? super E: accepts a filter for E or any supertype
            List<E> out = new ArrayList<>();
            for (E e : store.values()) if (filter.test(e)) out.add(e);
            return out;
        }
    }

    /** Generic method with a wildcard: works for a team of ANY player type. */
    static String rosterSummary(Team<? extends Player> team) {
        StringBuilder sb = new StringBuilder(team.name() + ": ");
        for (Player p : team.roster) sb.append(p.name()).append('(').append(p.rating()).append(") ");
        return sb.toString().trim();
    }

    public static void main(String[] args) {
        Team<FootballPlayer> chennai = new Team<FootballPlayer>("Chennai FC")
                .sign(new FootballPlayer("Arjun", "FW", 82)).sign(new FootballPlayer("Vikram", "GK", 78));
        Team<FootballPlayer> goa = new Team<FootballPlayer>("Goa Waves")
                .sign(new FootballPlayer("Rohan", "MF", 88)).sign(new FootballPlayer("Dev", "DF", 75));
        Team<FootballPlayer> kerala = new Team<FootballPlayer>("Kerala Tuskers")
                .sign(new FootballPlayer("Nikhil", "FW", 70)).sign(new FootballPlayer("Sam", "MF", 72));
        Team<FootballPlayer> mumbai = new Team<FootballPlayer>("Mumbai City")
                .sign(new FootballPlayer("Kabir", "FW", 85)).sign(new FootballPlayer("Ishan", "DF", 81));
        // chennai.sign(new CricketPlayer("x", "bat", 90));   // COMPILE ERROR: wrong player type

        League<Team<FootballPlayer>> isl = new League<>("Super League (football)");
        for (Team<FootballPlayer> t : List.of(chennai, goa, kerala, mumbai)) isl.add(t);
        isl.playRoundRobin();
        isl.printTable();

        Team<CricketPlayer> csk = new Team<CricketPlayer>("Chennai Kings")
                .sign(new CricketPlayer("Ruturaj", "BAT", 86)).sign(new CricketPlayer("Jadeja", "AR", 90));
        Team<CricketPlayer> mi = new Team<CricketPlayer>("Mumbai Blues")
                .sign(new CricketPlayer("Rohit", "BAT", 89)).sign(new CricketPlayer("Bumrah", "BOWL", 95));
        Team<CricketPlayer> rcb = new Team<CricketPlayer>("Bengaluru Reds")
                .sign(new CricketPlayer("Virat", "BAT", 93)).sign(new CricketPlayer("Siraj", "BOWL", 84));
        League<Team<CricketPlayer>> ipl = new League<>("T20 League (cricket)");
        for (Team<CricketPlayer> t : List.of(csk, mi, rcb)) ipl.add(t);
        ipl.playRoundRobin();
        System.out.println();
        ipl.printTable();

        CricketPlayer star = mi.star();                             // typed as CricketPlayer - no cast
        System.out.println("\n  Mumbai Blues star: " + star.name() + " (" + star.role() + ")");

        System.out.println("\n=== WILDCARD METHOD WORKS FOR ANY TEAM ===");
        System.out.println("  " + rosterSummary(goa));
        System.out.println("  " + rosterSummary(rcb));

        System.out.println("\n=== ONE GENERIC REPOSITORY, MANY ENTITY TYPES ===");
        Repository<String, Player> players = new Repository<>();
        for (Team<? extends Player> t : List.of(chennai, goa, csk, mi, rcb))
            for (Player p : t.roster) players.save(p.name().toLowerCase(), p);
        System.out.println("  findById(\"bumrah\") -> " + players.findById("bumrah").map(Player::toString).orElse("none"));
        System.out.println("  findById(\"messi\")  -> " + players.findById("messi").map(Player::toString).orElse("not found"));
        Predicate<Object> anything = o -> true;                     // Predicate<Object> fits Predicate<? super Player>
        System.out.println("  rated 90+: " + players.findAll(p -> p.rating() >= 90).stream().map(Player::name).toList()
                + " | total players: " + players.findAll(anything).size());
    }
}
