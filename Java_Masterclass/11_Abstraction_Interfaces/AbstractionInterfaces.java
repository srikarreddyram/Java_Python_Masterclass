/*
 * ============================================================================
 *  SECTION 11 : ABSTRACTION - abstract classes and interfaces
 * ============================================================================
 *
 *  ABSTRACTION = expose WHAT an object does, hide HOW it does it.
 *
 *  ABSTRACT CLASS
 *  --------------
 *   abstract class Animal { abstract void sound(); void breathe() {...} }
 *   - cannot be instantiated (new Animal() is a compile error)
 *   - may mix abstract methods (no body) with concrete methods and fields
 *   - can have constructors and state; a class extends only ONE abstract class
 *   - use it for "is-a" families that SHARE code and state
 *
 *  INTERFACE
 *  ---------
 *   interface Trackable { String location(); }
 *   - a CONTRACT: a list of capabilities ("can-do"): Comparable, Runnable...
 *   - a class can implement MANY interfaces
 *   - fields are implicitly public static final (constants)
 *   - Java 8+: default methods (with a body) and static methods
 *   - Java 9+: private methods (helpers for defaults)
 *
 *   | Feature                 | abstract class       | interface             |
 *   |-------------------------|----------------------|-----------------------|
 *   | instantiate directly    | no                   | no                    |
 *   | instance fields/state   | yes                  | no (constants only)   |
 *   | constructors            | yes                  | no                    |
 *   | multiple inheritance    | no (extend one)      | yes (implement many)  |
 *   | method bodies           | yes                  | default/static/private|
 *   | access modifiers        | any                  | public (or private)   |
 *
 *  HOW TO RUN:  javac AbstractionInterfaces.java && java AbstractionInterfaces
 * ============================================================================
 */
import java.util.ArrayList;
import java.util.List;

public class AbstractionInterfaces {

    // ======================== abstract class ========================
    abstract static class Animal {
        protected final String name;
        Animal(String name) { this.name = name; }      // abstract classes CAN have constructors

        abstract String sound();                        // every animal must define this
        abstract double dailyFoodKg();

        // concrete method shared by all subclasses, written once
        String feedingNote() { return String.format("%s says %s and eats %.1f kg/day", name, sound(), dailyFoodKg()); }
    }

    static class Dog extends Animal {
        Dog(String n) { super(n); }
        @Override String sound() { return "Woof"; }
        @Override double dailyFoodKg() { return 0.6; }
    }

    static class Elephant extends Animal {
        Elephant(String n) { super(n); }
        @Override String sound() { return "Trumpet"; }
        @Override double dailyFoodKg() { return 150; }
    }

    // ======================== interfaces ========================
    interface Flyer {
        double CRUISE_ALTITUDE_M = 1_000;              // implicitly public static final
        void takeOff();
        default void land() { System.out.println("    " + id() + " lands using the DEFAULT landing routine"); }
        String id();
    }

    interface Trackable {
        String location();
        static String describe(Trackable t) { return "tracking -> " + t.location(); }   // static interface method
    }

    interface Rechargeable {
        int batteryPercent();
        default boolean needsCharge() { return belowThreshold(20); }
        private boolean belowThreshold(int t) { return batteryPercent() < t; }          // private helper (Java 9+)
    }

    // A class can extend ONE class and implement MANY interfaces
    static class Bird extends Animal implements Flyer {
        Bird(String n) { super(n); }
        @Override String sound() { return "Tweet"; }
        @Override double dailyFoodKg() { return 0.05; }
        @Override public void takeOff() { System.out.println("    " + name + " flaps wings and takes off"); }
        @Override public String id() { return name; }
    }

    static class Drone implements Flyer, Trackable, Rechargeable {
        private final String serial;
        private int battery;
        Drone(String serial, int battery) { this.serial = serial; this.battery = battery; }
        @Override public void takeOff() { battery -= 5; System.out.println("    drone " + serial + " spins rotors to " + CRUISE_ALTITUDE_M + " m"); }
        @Override public void land() { System.out.println("    drone " + serial + " runs its OWN precision landing (overrides default)"); }
        @Override public String id() { return serial; }
        @Override public String location() { return "13.0827N, 80.2707E @ " + CRUISE_ALTITUDE_M + " m"; }
        @Override public int batteryPercent() { return battery; }
    }

    // ======================== diamond problem with default methods ========================
    interface Camera { default String capture() { return "photo"; } }
    interface Scanner { default String capture() { return "scan"; } }
    static class Phone implements Camera, Scanner {
        @Override public String capture() {                         // MUST resolve the conflict
            return Camera.super.capture() + " + " + Scanner.super.capture();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== ABSTRACT CLASS ===");
        // Animal a = new Animal("x");   // compile error: Animal is abstract
        List<Animal> zoo = List.of(new Dog("Bruno"), new Elephant("Ganesh"), new Bird("Tweety"));
        for (Animal a : zoo) System.out.println("  " + a.feedingNote());

        System.out.println("\n=== INTERFACES: program to the capability, not the class ===");
        List<Flyer> flyers = new ArrayList<>();
        flyers.add(new Bird("Rio"));
        flyers.add(new Drone("DX-9", 18));
        for (Flyer f : flyers) {
            f.takeOff();
            f.land();
        }

        System.out.println("\n=== MULTIPLE INTERFACES ON ONE CLASS ===");
        Drone d = new Drone("DX-9", 18);
        System.out.println("  " + Trackable.describe(d));
        System.out.println("  battery " + d.batteryPercent() + "% -> needsCharge() = " + d.needsCharge());
        System.out.println("  instanceof Flyer/Trackable/Rechargeable: " + (d instanceof Flyer) + "/" + (d instanceof Trackable) + "/" + (d instanceof Rechargeable));

        System.out.println("\n=== DEFAULT-METHOD DIAMOND ===");
        System.out.println("  Phone.capture() -> " + new Phone().capture() + "  (class explicitly picks via X.super.capture())");

        System.out.println("\n=== ANONYMOUS IMPLEMENTATION (preview of Section 13) ===");
        Trackable parcel = new Trackable() {
            @Override public String location() { return "Chennai sorting hub"; }
        };
        System.out.println("  " + Trackable.describe(parcel));
    }
}
