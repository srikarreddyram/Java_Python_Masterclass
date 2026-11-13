/*
 * ============================================================================
 *  SECTION 08 : REAL-WORLD APP - Ride-hailing fare engine (think Ola / Uber)
 * ============================================================================
 *
 *  Design
 *    Vehicle (parent)            fare(km, minutes) is POLYMORPHIC
 *      |-- Bike                 cheapest, 1 seat
 *      |-- AutoRickshaw         night surcharge rule of its own
 *      |-- Cab                  per-seat AC model, has-a Driver rating
 *      |-- PremiumCab extends Cab  higher base, airport fee
 *
 *    Ride HAS-A Rider, HAS-A Vehicle, HAS-A Route   (composition)
 *    FareEngine works only with the Vehicle type, so adding a new vehicle class
 *    needs ZERO changes in FareEngine (the open/closed principle in action).
 *
 *  CONCEPTS USED
 *   - composition: Ride is assembled from smaller objects
 *   - encapsulation: surge multiplier validated, fields private
 *   - polymorphism: one loop prices every vehicle type via overridden fare()
 *   - super.fare(...) to extend instead of rewrite parent logic
 *   - instanceof pattern matching for a subclass-only feature
 *
 *  HOW TO RUN:  javac RideHailingApp.java && java RideHailingApp
 * ============================================================================
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RideHailingApp {

    record Rider(String name, boolean hasSubscription) { }
    record Route(String from, String to, double km, int minutes, boolean night, boolean airport) { }
    record Driver(String name, double rating) { }

    // ============================== vehicle hierarchy ==============================
    static class Vehicle {
        private final String label;
        protected final double baseFare, perKm, perMinute;

        Vehicle(String label, double baseFare, double perKm, double perMinute) {
            this.label = label; this.baseFare = baseFare; this.perKm = perKm; this.perMinute = perMinute;
        }

        String label() { return label; }
        int seats() { return 4; }

        /** Default pricing: base + distance + time. Subclasses refine it. */
        double fare(Route r) { return baseFare + perKm * r.km() + perMinute * r.minutes(); }
    }

    static class Bike extends Vehicle {
        Bike() { super("Bike", 20, 6, 0.5); }
        @Override int seats() { return 1; }
    }

    static class AutoRickshaw extends Vehicle {
        AutoRickshaw() { super("Auto", 30, 12, 1.0); }
        @Override int seats() { return 3; }
        @Override
        double fare(Route r) {                        // extend, don't rewrite
            double f = super.fare(r);
            return r.night() ? f * 1.5 : f;           // 50% night charge for autos
        }
    }

    static class Cab extends Vehicle {
        private final Driver driver;                  // has-a
        Cab(Driver d) { this("Cab", 50, 14, 1.5, d); }
        protected Cab(String label, double base, double km, double min, Driver d) { super(label, base, km, min); this.driver = d; }
        Driver driver() { return driver; }
        @Override
        double fare(Route r) {
            double f = super.fare(r);
            return r.night() ? f * 1.2 : f;
        }
    }

    static class PremiumCab extends Cab {
        PremiumCab(Driver d) { super("Premier", 90, 20, 2.0, d); }
        @Override
        double fare(Route r) { return super.fare(r) + (r.airport() ? 150 : 0); }   // airport fee on top of Cab rules
    }

    // ============================== composition: a ride ==============================
    static class Ride {
        private final Rider rider;
        private final Vehicle vehicle;
        private final Route route;
        Ride(Rider rider, Vehicle vehicle, Route route) { this.rider = rider; this.vehicle = vehicle; this.route = route; }
    }

    static class FareEngine {
        private double surge = 1.0;
        private static final double PLATFORM_FEE = 8, GST = 0.05;

        void setSurge(double multiplier) {            // encapsulation: rules enforced here
            if (multiplier < 1.0 || multiplier > 3.0) throw new IllegalArgumentException("surge must be 1.0 - 3.0");
            surge = multiplier;
        }

        /** Works for ANY Vehicle subclass - present or future. */
        double quote(Ride ride) {
            double fare = ride.vehicle.fare(ride.route) * surge;          // polymorphic call
            if (ride.rider.hasSubscription()) fare *= 0.9;               // 10% member discount
            fare += PLATFORM_FEE;
            return Math.round(fare * (1 + GST));
        }
    }

    public static void main(String[] args) {
        FareEngine engine = new FareEngine();
        Rider srikar = new Rider("Srikar", false), member = new Rider("Meera", true);
        Route college = new Route("SRM KTR", "Guindy", 24.5, 55, false, false);
        Route airportNight = new Route("T Nagar", "Chennai Airport", 16, 40, true, true);

        List<Vehicle> fleet = List.of(new Bike(), new AutoRickshaw(), new Cab(new Driver("Kumar", 4.8)),
                new PremiumCab(new Driver("Anita", 4.95)));

        for (Route r : List.of(college, airportNight)) {
            System.out.printf("=== %s -> %s (%.1f km, %d min%s%s) ===%n", r.from(), r.to(), r.km(), r.minutes(),
                    r.night() ? ", night" : "", r.airport() ? ", airport" : "");
            List<double[]> quotes = new ArrayList<>();
            for (int i = 0; i < fleet.size(); i++) {
                Vehicle v = fleet.get(i);
                double price = engine.quote(new Ride(srikar, v, r));
                quotes.add(new double[]{i, price});
                String extra = (v instanceof Cab c) ? "  driver " + c.driver().name() + " " + c.driver().rating() + "*" : "";
                System.out.printf("  %-8s seats %d  Rs %6.0f%s%n", v.label(), v.seats(), price, extra);
            }
            quotes.sort(Comparator.comparingDouble(q -> q[1]));
            System.out.println("  cheapest: " + fleet.get((int) quotes.get(0)[0]).label());
        }

        System.out.println("\n=== SURGE + MEMBERSHIP ===");
        engine.setSurge(1.8);
        Vehicle cab = fleet.get(2);
        System.out.printf("  Cab, surge 1.8x, regular rider : Rs %.0f%n", engine.quote(new Ride(srikar, cab, college)));
        System.out.printf("  Cab, surge 1.8x, member (-10%%) : Rs %.0f%n", engine.quote(new Ride(member, cab, college)));
        try {
            engine.setSurge(7.5);
        } catch (IllegalArgumentException e) {
            System.out.println("  setSurge(7.5) rejected: " + e.getMessage());
        }
    }
}
