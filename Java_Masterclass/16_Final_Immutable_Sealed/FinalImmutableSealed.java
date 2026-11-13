/*
 * ============================================================================
 *  SECTION 16 : final, IMMUTABLE CLASSES, INITIALIZERS & INITIALISATION ORDER,
 *               RECORDS, ENUMS AS SINGLETONS, SEALED CLASSES/INTERFACES
 * ============================================================================
 *
 *  THE final KEYWORD
 *   final variable : assigned exactly once (a constant if it's a primitive/immutable)
 *   final field    : must be set by the end of the constructor
 *   final method   : cannot be OVERRIDDEN
 *   final class    : cannot be EXTENDED (String, Integer, LocalDate are final)
 *   CAUTION: a final REFERENCE can't be re-pointed, but the object it points
 *   to can still change:  final List<String> l = new ArrayList<>(); l.add("x"); // fine
 *
 *  IMMUTABLE CLASS RECIPE (Effective Java, item 17)
 *   1. make the class final (or use a private constructor + factory)
 *   2. all fields private final
 *   3. no setters / no methods that change state
 *   4. DEFENSIVE COPY mutable inputs in the constructor and mutable outputs in getters
 *   5. "modification" returns a NEW object (withX(...) methods)
 *   Benefits: thread-safe for free, safe as HashMap keys, easy to reason about.
 *
 *  INITIALISATION ORDER (when "new Child()" runs for the first time)
 *   1. Parent static fields/blocks   2. Child static fields/blocks   (once per class)
 *   3. Parent instance fields/blocks 4. Parent constructor
 *   5. Child instance fields/blocks  6. Child constructor
 *
 *  SEALED TYPES (Java 17):  sealed interface Shape permits Circle, Square {}
 *   Only the listed classes may extend/implement it. Each permitted subclass
 *   must be final, sealed, or non-sealed. The compiler then KNOWS every
 *   subtype, which makes exhaustive pattern matching possible.
 *
 *  HOW TO RUN:  javac FinalImmutableSealed.java && java FinalImmutableSealed
 * ============================================================================
 */
import java.util.*;

public class FinalImmutableSealed {

    // ======================== final in all its forms ========================
    static final double GST_RATE = 0.18;                 // compile-time constant (static final)

    static class Base {
        final String id;                                 // final field: set once in the constructor
        Base(String id) { this.id = id; }
        final String audit() { return "audit:" + id; }   // final method: subclasses can't override
        String describe() { return "Base " + id; }
    }

    static final class Leaf extends Base {               // final class: nobody can extend Leaf
        Leaf(String id) { super(id); }
        @Override String describe() { return "Leaf " + id; }
        // @Override String audit() {...}                 // compile error: audit() is final
    }

    // ======================== a properly immutable class ========================
    static final class Money {
        private final long paise;                        // store money as integer paise: no float errors
        private final Currency currency;

        private Money(long paise, Currency currency) { this.paise = paise; this.currency = currency; }

        static Money ofRupees(double rupees) { return new Money(Math.round(rupees * 100), Currency.getInstance("INR")); }

        Money plus(Money other) { return new Money(paise + other.paise, currency); }   // new object, never mutate
        Money times(double factor) { return new Money(Math.round(paise * factor), currency); }

        @Override public boolean equals(Object o) { return o instanceof Money m && m.paise == paise && m.currency.equals(currency); }
        @Override public int hashCode() { return Objects.hash(paise, currency); }
        @Override public String toString() { return String.format("Rs %,.2f", paise / 100.0); }
    }

    static final class Invoice {
        private final String number;
        private final List<String> lines;                // List is MUTABLE in general...
        private final Date issued;                       // ...and so is java.util.Date

        Invoice(String number, List<String> lines, Date issued) {
            this.number = number;
            this.lines = List.copyOf(lines);             // defensive copy IN (and immutable)
            this.issued = new Date(issued.getTime());    // defensive copy IN
        }
        List<String> lines() { return lines; }           // already unmodifiable: safe to share
        Date issued() { return new Date(issued.getTime()); }   // defensive copy OUT
        Invoice withLine(String line) {                  // "modify" = build a new Invoice
            List<String> copy = new ArrayList<>(lines);
            copy.add(line);
            return new Invoice(number, copy, issued);
        }
        @Override public String toString() { return number + " " + lines; }
    }

    static final class LeakyInvoice {                    // what NOT to do
        private final List<String> lines;
        LeakyInvoice(List<String> lines) { this.lines = lines; }   // stores the caller's list
        List<String> lines() { return lines; }
    }

    // ======================== initialisation order ========================
    static StringBuilder trace = new StringBuilder();

    static class Parent {
        static { trace.append(" 1:Parent-static"); }
        { trace.append(" 3:Parent-instance-block"); }
        Parent() { trace.append(" 4:Parent()"); }
    }

    static class Child extends Parent {
        static final List<String> CONFIG;
        static {                                          // static initializer: complex constant setup
            trace.append(" 2:Child-static");
            List<String> c = new ArrayList<>(List.of("db=prod", "retries=3"));
            CONFIG = Collections.unmodifiableList(c);
        }
        private final int created;
        { trace.append(" 5:Child-instance-block"); }      // instance initializer: runs before the constructor body
        Child() { created = trace.length(); trace.append(" 6:Child()"); }
    }

    // ======================== enum singleton ========================
    enum AppConfig {
        INSTANCE;                                         // the JVM guarantees exactly one, even with reflection/serialisation
        private final Map<String, String> settings = Map.of("env", "prod", "region", "ap-south-1");
        String get(String key) { return settings.getOrDefault(key, "?"); }
    }

    // ======================== sealed hierarchy ========================
    sealed interface Shape permits Circle, Rect, Triangle { }
    record Circle(double r) implements Shape { }                        // records are implicitly final
    record Rect(double w, double h) implements Shape { }
    static non-sealed class Triangle implements Shape {                 // non-sealed: re-opens extension
        final double b, h;
        Triangle(double b, double h) { this.b = b; this.h = h; }
    }
    static class RightTriangle extends Triangle { RightTriangle(double b, double h) { super(b, h); } }

    static double area(Shape s) {
        // Java 17: instanceof patterns. (Java 21 adds 'switch (s) { case Circle c -> ... }' with
        // compile-time exhaustiveness checking because the hierarchy is sealed.)
        if (s instanceof Circle c) return Math.PI * c.r() * c.r();
        if (s instanceof Rect r) return r.w() * r.h();
        if (s instanceof Triangle t) return 0.5 * t.b * t.h;
        throw new IllegalStateException("impossible: Shape is sealed");
    }

    public static void main(String[] args) {
        System.out.println("=== final ===");
        final int maxUsers = 100;
        // maxUsers = 200;                                 // compile error
        final List<String> names = new ArrayList<>();
        names.add("Srikar");                               // allowed: the LIST changes, the reference doesn't
        System.out.println("  final int " + maxUsers + "; final List after add -> " + names);
        Base b = new Leaf("L1");
        System.out.println("  " + b.describe() + " / " + b.audit() + " / GST_RATE " + GST_RATE);

        System.out.println("\n=== IMMUTABLE VALUE OBJECT: Money ===");
        Money price = Money.ofRupees(1_299.99);
        Money total = price.times(3).plus(Money.ofRupees(49));
        System.out.println("  price " + price + " x3 + delivery = " + total + "   (price is unchanged: " + price + ")");
        Map<Money, String> tiers = new HashMap<>();
        tiers.put(Money.ofRupees(499), "free delivery threshold");
        System.out.println("  safe as a HashMap key: " + tiers.get(Money.ofRupees(499)));
        System.out.println("  0.1 + 0.2 in paise: " + Money.ofRupees(0.1).plus(Money.ofRupees(0.2)));

        System.out.println("\n=== DEFENSIVE COPIES ===");
        List<String> source = new ArrayList<>(List.of("Laptop x1"));
        Date when = new Date(0);
        Invoice inv = new Invoice("INV-7", source, when);
        LeakyInvoice leaky = new LeakyInvoice(source);
        source.add("HACKED x999");                         // caller mutates its own list afterwards
        when.setTime(999_999_999_999L);
        System.out.println("  immutable invoice : " + inv + ", issued " + inv.issued().toInstant() + " (unchanged)");
        System.out.println("  leaky invoice     : " + leaky.lines() + "  <- changed behind its back");
        try { inv.lines().add("x"); } catch (UnsupportedOperationException e) { System.out.println("  inv.lines().add -> UnsupportedOperationException"); }
        System.out.println("  withLine creates a new object: " + inv.withLine("Mouse x1") + " (original still " + inv + ")");

        System.out.println("\n=== INITIALISATION ORDER ===");
        new Child();
        System.out.println("  first new Child():" + trace);
        trace.setLength(0);
        new Child();
        System.out.println("  second new Child():" + trace + "   (static blocks never run again)");
        System.out.println("  static config built in a static block: " + Child.CONFIG);

        System.out.println("\n=== ENUM SINGLETON ===");
        System.out.println("  AppConfig.INSTANCE.get(region) = " + AppConfig.INSTANCE.get("region") + ", same object: " + (AppConfig.INSTANCE == AppConfig.valueOf("INSTANCE")));

        System.out.println("\n=== SEALED HIERARCHY ===");
        List<Shape> shapes = List.of(new Circle(1), new Rect(2, 3), new Triangle(4, 5), new RightTriangle(3, 4));
        for (Shape s : shapes) System.out.printf("  %-14s area %.2f%n", s.getClass().getSimpleName(), area(s));
        System.out.println("  Shape.class.isSealed() = " + Shape.class.isSealed() + ", permitted = "
                + Arrays.stream(Shape.class.getPermittedSubclasses()).map(Class::getSimpleName).toList());
    }
}
