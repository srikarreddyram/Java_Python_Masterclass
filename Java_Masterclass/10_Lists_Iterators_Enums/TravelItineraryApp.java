/*
 * ============================================================================
 *  SECTION 10 : REAL-WORLD APP - Road-trip Itinerary Planner
 * ============================================================================
 *
 *  A trip is an ORDERED list of stops where we constantly insert detours in the
 *  middle and step forwards/backwards through the plan - a perfect fit for
 *  LinkedList + ListIterator. Each stop has a category (an enum with fields).
 *
 *  Features
 *   - stops kept in route order; insert a stop "after X" while iterating
 *   - navigate the itinerary with next / previous (ListIterator)
 *   - remove closed attractions safely with Iterator.remove()
 *   - budget/time summary per category with an EnumMap
 *   - ArrayList for the random-access "highlights" view
 *
 *  HOW TO RUN:  javac TravelItineraryApp.java && java TravelItineraryApp
 * ============================================================================
 */
import java.util.*;

public class TravelItineraryApp {

    enum Category {
        FOOD("Food stop", 1.0), SIGHTSEEING("Sightseeing", 2.5), FUEL("Fuel/charging", 0.5), STAY("Overnight", 10.0);

        final String label;
        final double typicalHours;
        Category(String label, double typicalHours) { this.label = label; this.typicalHours = typicalHours; }
    }

    record Stop(String place, int kmFromStart, Category category, int costRs, boolean openToday) {
        @Override public String toString() { return place + " (km " + kmFromStart + ")"; }
    }

    static void print(String title, List<Stop> stops) {
        System.out.println("=== " + title + " ===");
        int i = 1;
        for (Stop s : stops) System.out.printf("  %2d. %-34s %-13s Rs %,5d%s%n", i++, s, s.category().label, s.costRs(), s.openToday() ? "" : "  [CLOSED TODAY]");
    }

    public static void main(String[] args) {
        LinkedList<Stop> trip = new LinkedList<>(List.of(
                new Stop("Chennai (start)", 0, Category.FUEL, 0, true),
                new Stop("Mahabalipuram", 58, Category.SIGHTSEEING, 600, true),
                new Stop("Pondicherry", 150, Category.STAY, 3_500, true),
                new Stop("Chidambaram Temple", 215, Category.SIGHTSEEING, 0, false),
                new Stop("Tanjore", 330, Category.SIGHTSEEING, 200, true),
                new Stop("Madurai", 480, Category.STAY, 2_800, true)));
        print("ORIGINAL PLAN", trip);

        // 1) Insert in route order: walk with a ListIterator and add() just before the first stop further away.
        Stop[] detours = {
                new Stop("Murugan Idli Shop", 470, Category.FOOD, 350, true),
                new Stop("EV charger, Kumbakonam", 280, Category.FUEL, 450, true),
                new Stop("Auroville", 160, Category.SIGHTSEEING, 0, true)};
        for (Stop d : detours) {
            ListIterator<Stop> it = trip.listIterator();
            while (it.hasNext()) {
                if (it.next().kmFromStart() > d.kmFromStart()) {
                    it.previous();          // step back so add() inserts BEFORE that stop
                    break;
                }
            }
            it.add(d);                      // O(1) insert at the iterator position
        }
        print("WITH DETOURS (inserted in km order)", trip);

        // 2) Remove closed attractions safely while iterating
        Iterator<Stop> it = trip.iterator();
        while (it.hasNext()) {
            Stop s = it.next();
            if (!s.openToday()) {
                it.remove();
                System.out.println("  removed closed stop: " + s.place());
            }
        }

        // 3) Driver navigation: forward and backward through the plan
        System.out.println("\n=== NAVIGATION (ListIterator forward/back) ===");
        ListIterator<Stop> nav = trip.listIterator();
        String[] commands = {"next", "next", "next", "back", "next", "next", "next"};
        Stop current = null;
        boolean goingForward = true;
        for (String cmd : commands) {
            if (cmd.equals("next")) {
                if (!goingForward && nav.hasNext()) nav.next();      // direction change: skip the repeat
                goingForward = true;
                current = nav.hasNext() ? nav.next() : current;
            } else {
                if (goingForward && nav.hasPrevious()) nav.previous();
                goingForward = false;
                current = nav.hasPrevious() ? nav.previous() : current;
            }
            System.out.printf("  %-4s -> now at %s%n", cmd, current);
        }

        // 4) Summary by category using an EnumMap (iterates in enum order)
        System.out.println("\n=== BUDGET & TIME BY CATEGORY ===");
        EnumMap<Category, int[]> totals = new EnumMap<>(Category.class);   // {count, cost}
        for (Stop s : trip) {
            int[] t = totals.computeIfAbsent(s.category(), c -> new int[2]);
            t[0]++;
            t[1] += s.costRs();
        }
        int grand = 0;
        double hours = 0;
        for (Map.Entry<Category, int[]> e : totals.entrySet()) {
            double h = e.getValue()[0] * e.getKey().typicalHours;
            System.out.printf("  %-13s x%d  Rs %,6d  ~%.1f h%n", e.getKey().label, e.getValue()[0], e.getValue()[1], h);
            grand += e.getValue()[1];
            hours += h;
        }
        int km = trip.getLast().kmFromStart();
        System.out.printf("  Total: %d stops, %d km, Rs %,d, ~%.1f h at stops + %.1f h driving (50 km/h)%n",
                trip.size(), km, grand, hours, km / 50.0);

        // 5) ArrayList copy for fast random access (e.g. a UI carousel)
        List<Stop> highlights = new ArrayList<>(trip);
        highlights.removeIf(s -> s.category() != Category.SIGHTSEEING);
        System.out.println("\n  Highlights carousel (ArrayList, get(i) is O(1)): " + highlights + " -> slide 2 = " + highlights.get(1).place());
    }
}
