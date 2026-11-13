/*
 * ============================================================================
 *  SECTION 07 : OOP PART 1 - classes, objects, constructors, this, static vs
 *               instance, getters/setters, records, inheritance, super,
 *               overriding, the Object class, String & StringBuilder
 * ============================================================================
 *
 *  CLASS vs OBJECT
 *  ---------------
 *   A CLASS is a blueprint (fields = state, methods = behaviour).
 *   An OBJECT (instance) is one thing built from it with 'new'.
 *     Car myCar = new Car("Tata", "Nexon");
 *     ^ reference variable  ^ object on the heap
 *   Two references can point at the SAME object; '==' compares references.
 *
 *  CONSTRUCTORS
 *   - same name as the class, no return type; run once when 'new' is called
 *   - overloaded constructors can delegate with this(...)  (must be FIRST line)
 *   - if you write none, Java adds a no-arg default constructor
 *
 *  STATIC vs INSTANCE
 *   static field  : ONE copy shared by all objects (e.g. a counter)
 *   instance field: one copy PER object
 *   static methods can't use 'this' (there is no object).
 *
 *  INHERITANCE ("is-a"): class Dog extends Animal
 *   - Dog gets Animal's non-private members; super(...) calls the parent constructor
 *   - @Override replaces a parent method; super.method() calls the parent version
 *   - Java has SINGLE class inheritance; every class ultimately extends Object
 *
 *  RECORD (Java 16+): an immutable data carrier. One line gives you
 *   private final fields, a constructor, accessors, equals, hashCode, toString.
 *
 *  HOW TO RUN:  javac ClassesAndInheritance.java && java ClassesAndInheritance
 * ============================================================================
 */
import java.util.Objects;

public class ClassesAndInheritance {

    // ======================== a plain class (POJO) ========================
    static class Car {
        private static int carsBuilt = 0;       // static: shared by ALL cars
        private final String make;               // instance fields: per car
        private final String model;
        private int speed;                       // starts at default 0

        Car(String make, String model) {         // constructor
            this.make = make;                    // 'this' = the object being built
            this.model = model;
            carsBuilt++;
        }

        Car(String model) {                      // overloaded constructor
            this("Generic", model);              // constructor chaining - must be first
        }

        void accelerate(int delta) {             // instance method changes THIS car's state
            speed = Math.max(0, Math.min(180, speed + delta));
        }

        int getSpeed() { return speed; }         // getter (no setter: speed changes only via accelerate)
        static int getCarsBuilt() { return carsBuilt; }   // static method

        @Override
        public String toString() { return make + " " + model + " @ " + speed + " km/h"; }
    }

    // ======================== inheritance ========================
    static class Employee {
        protected final String name;             // protected: visible to subclasses
        protected final double baseSalary;

        Employee(String name, double baseSalary) {
            this.name = name;
            this.baseSalary = baseSalary;
        }

        double monthlyPay() { return baseSalary / 12; }

        String describe() { return String.format("%-8s %-9s pays Rs %,10.2f/month", getClass().getSimpleName(), name, monthlyPay()); }
    }

    static class Manager extends Employee {
        private final double bonusPercent;

        Manager(String name, double baseSalary, double bonusPercent) {
            super(name, baseSalary);             // parent constructor runs FIRST
            this.bonusPercent = bonusPercent;
        }

        @Override
        double monthlyPay() {                    // overriding: same signature, new behaviour
            return super.monthlyPay() * (1 + bonusPercent / 100);   // reuse parent logic
        }
    }

    static class Intern extends Employee {
        Intern(String name, double stipendPerMonth) { super(name, stipendPerMonth * 12); }

        @Override
        String describe() { return super.describe() + "  (stipend)"; }
    }

    // ======================== equals / hashCode by hand ========================
    static class Point {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;                          // same object
            if (!(o instanceof Point p)) return false;           // pattern matching instanceof (Java 16)
            return x == p.x && y == p.y;
        }

        @Override
        public int hashCode() { return Objects.hash(x, y); }    // equal objects MUST have equal hashes
    }

    // ======================== record: the same thing in one line ========================
    record GeoPoint(double lat, double lon) {
        GeoPoint {                                               // compact constructor: validation
            if (lat < -90 || lat > 90) throw new IllegalArgumentException("bad latitude " + lat);
        }
        double distanceTo(GeoPoint o) {                          // records can have methods
            double r = 6371, dLat = Math.toRadians(o.lat - lat), dLon = Math.toRadians(o.lon - lon);
            double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(o.lat)) * Math.pow(Math.sin(dLon / 2), 2);
            return 2 * r * Math.asin(Math.sqrt(a));
        }
    }

    public static void main(String[] args) {
        System.out.println("=== CLASSES, OBJECTS, REFERENCES ===");
        Car a = new Car("Tata", "Nexon");
        Car b = new Car("Thar");                               // uses the chained constructor
        Car alias = a;                                         // second reference, SAME object
        alias.accelerate(60);
        System.out.println("  a = " + a + "   (changed through 'alias')");
        System.out.println("  b = " + b);
        System.out.println("  a == alias: " + (a == alias) + ", cars built (static): " + Car.getCarsBuilt());

        System.out.println("\n=== INHERITANCE & OVERRIDING ===");
        Employee[] staff = {
                new Employee("Asha", 9_00_000),
                new Manager("Ravi", 18_00_000, 20),
                new Intern("Srikar", 25_000)};
        for (Employee e : staff) System.out.println("  " + e.describe());   // each object's own version runs

        System.out.println("\n=== THE OBJECT CLASS: equals, hashCode, toString ===");
        Point p1 = new Point(3, 4), p2 = new Point(3, 4);
        System.out.println("  p1 == p2: " + (p1 == p2) + "   p1.equals(p2): " + p1.equals(p2) + "   same hash: " + (p1.hashCode() == p2.hashCode()));
        Object generic = p1;
        System.out.println("  every class extends Object -> " + (generic instanceof Object) + ", class = " + generic.getClass().getSimpleName());

        System.out.println("\n=== RECORDS ===");
        GeoPoint chennai = new GeoPoint(13.0827, 80.2707), goa = new GeoPoint(15.4909, 73.8278);
        System.out.println("  " + chennai + "  (auto toString)");
        System.out.printf("  Chennai -> Goa straight-line distance: %.0f km%n", chennai.distanceTo(goa));
        System.out.println("  equals: " + chennai.equals(new GeoPoint(13.0827, 80.2707)));
        try {
            new GeoPoint(95, 0);
        } catch (IllegalArgumentException e) {
            System.out.println("  compact constructor rejected: " + e.getMessage());
        }

        System.out.println("\n=== STRING METHODS (Strings are immutable) ===");
        String s = "  Java Masterclass 2025  ";
        System.out.println("  strip()        : '" + s.strip() + "'");
        System.out.println("  toUpperCase()  : " + s.strip().toUpperCase());
        System.out.println("  indexOf(\"Master\"): " + s.strip().indexOf("Master") + ", substring(5,11): " + s.strip().substring(5, 11));
        System.out.println("  replace/contains: " + s.strip().replace("2025", "2026") + ", contains Java? " + s.contains("Java"));
        System.out.println("  split + join   : " + String.join(" | ", "a,b,c".split(",")));
        System.out.println("  repeat / isBlank: " + "ab".repeat(3) + ", '   '.isBlank() = " + "   ".isBlank());
        System.out.println("  format         : " + String.format("%-6s|%5.1f|%03d", "id", 9.87, 7));

        System.out.println("\n=== STRINGBUILDER (mutable, for building strings in loops) ===");
        StringBuilder sb = new StringBuilder("Hello");
        sb.append(", World").insert(0, ">> ").replace(3, 8, "Hi").reverse().reverse();
        System.out.println("  " + sb + "   length=" + sb.length() + " capacity=" + sb.capacity());
        long t0 = System.nanoTime();
        String slow = "";
        for (int i = 0; i < 5_000; i++) slow += i;             // creates 5,000 new String objects
        long t1 = System.nanoTime();
        StringBuilder fast = new StringBuilder();
        for (int i = 0; i < 5_000; i++) fast.append(i);         // one growing buffer
        long t2 = System.nanoTime();
        System.out.printf("  5,000 appends: String += %.1f ms vs StringBuilder %.2f ms (same text: %b)%n",
                (t1 - t0) / 1e6, (t2 - t1) / 1e6, slow.contentEquals(fast));
    }
}
