/*
 * ============================================================================
 *  SECTION 08 : OOP PART 2 - composition, encapsulation, polymorphism
 * ============================================================================
 *
 *  COMPOSITION ("has-a")
 *  ---------------------
 *  Build a complex object out of smaller objects it OWNS:
 *     Smartphone HAS-A Battery, HAS-A Camera, HAS-A Screen
 *  Prefer composition over inheritance when the relationship isn't truly
 *  "is-a": it is more flexible (swap parts at runtime) and avoids deep,
 *  fragile class hierarchies.
 *
 *  ENCAPSULATION
 *  -------------
 *  Hide internal state (private fields) and expose behaviour through methods
 *  that ENFORCE the rules. Callers can't put the object into an invalid state.
 *    bad : player.health = -50;          good: player.takeDamage(50);
 *
 *  POLYMORPHISM ("many forms")
 *  ---------------------------
 *   - A variable of a PARENT type can hold any SUBCLASS object.
 *   - Calling an overridden method runs the version of the ACTUAL object
 *     (decided at RUNTIME = dynamic dispatch / late binding).
 *   - Overloading is decided at COMPILE time; overriding at RUN time.
 *   - instanceof + pattern matching lets you safely use subclass-only methods.
 *
 *  HOW TO RUN:  javac CompositionPolymorphism.java && java CompositionPolymorphism
 * ============================================================================
 */
import java.util.List;

public class CompositionPolymorphism {

    // ============================ COMPOSITION ============================
    static class Battery {
        private final int capacityMah;
        private int chargePercent = 100;
        Battery(int capacityMah) { this.capacityMah = capacityMah; }
        void drain(int percent) { chargePercent = Math.max(0, chargePercent - percent); }
        int getChargePercent() { return chargePercent; }
        int getCapacityMah() { return capacityMah; }
    }

    static class Camera {
        private final int megapixels;
        Camera(int megapixels) { this.megapixels = megapixels; }
        String shoot(String subject) { return megapixels + "MP photo of " + subject; }
    }

    static class Screen {
        private final double inches;
        private int brightness = 50;
        Screen(double inches) { this.inches = inches; }
        void setBrightness(int b) { brightness = Math.max(0, Math.min(100, b)); }   // validated
        int getBrightness() { return brightness; }
    }

    static class Smartphone {
        private final String model;
        private final Battery battery;   // HAS-A
        private final Camera camera;     // HAS-A
        private final Screen screen;     // HAS-A

        Smartphone(String model, Battery battery, Camera camera, Screen screen) {
            this.model = model; this.battery = battery; this.camera = camera; this.screen = screen;
        }

        // The phone DELEGATES work to its parts and coordinates them.
        String takePhoto(String subject) {
            if (battery.getChargePercent() < 5) return model + ": battery too low for camera";
            battery.drain(2);
            return model + " -> " + camera.shoot(subject);
        }

        void watchVideo(int minutes) {
            screen.setBrightness(80);
            battery.drain(minutes / 3 + screen.getBrightness() / 20);
        }

        String status() {
            return String.format("%s: %d%% of %,d mAh, screen %.1f\" at %d%% brightness",
                    model, battery.getChargePercent(), battery.getCapacityMah(), screen.inches, screen.getBrightness());
        }
    }

    // ============================ ENCAPSULATION ============================
    static class Player {
        private final String name;
        private int health = 100;            // private: cannot be set to -50 from outside
        private int lives = 3;

        Player(String name) { this.name = name; }

        void takeDamage(int dmg) {
            if (dmg <= 0) return;            // rule: ignore nonsense input
            health -= dmg;
            if (health <= 0) {               // rule: losing all health costs a life
                lives--;
                health = lives > 0 ? 100 : 0;
            }
        }

        void heal(int amount) { if (lives > 0) health = Math.min(100, health + amount); }   // rule: cap at 100
        boolean isAlive() { return lives > 0; }
        @Override public String toString() { return name + " [health " + health + ", lives " + lives + "]"; }
    }

    // ============================ POLYMORPHISM ============================
    static class Shape {
        String name() { return "shape"; }
        double area() { return 0; }
    }
    static class Circle extends Shape {
        final double r;
        Circle(double r) { this.r = r; }
        @Override String name() { return "circle r=" + r; }
        @Override double area() { return Math.PI * r * r; }
    }
    static class Rectangle extends Shape {
        final double w, h;
        Rectangle(double w, double h) { this.w = w; this.h = h; }
        @Override String name() { return "rectangle " + w + "x" + h; }
        @Override double area() { return w * h; }
    }
    static class Square extends Rectangle {
        Square(double side) { super(side, side); }
        @Override String name() { return "square " + w; }
        double diagonal() { return w * Math.sqrt(2); }    // only squares have this
    }

    // Overloading (compile-time) vs overriding (run-time)
    static String describe(Shape s) { return "describe(Shape)"; }
    static String describe(Square s) { return "describe(Square)"; }

    public static void main(String[] args) {
        System.out.println("=== COMPOSITION ===");
        Smartphone phone = new Smartphone("Pixel", new Battery(4_700), new Camera(50), new Screen(6.2));
        System.out.println("  " + phone.takePhoto("Marina Beach"));
        phone.watchVideo(45);
        System.out.println("  " + phone.status());

        System.out.println("\n=== ENCAPSULATION ===");
        Player p = new Player("Srikar");
        p.takeDamage(70); System.out.println("  took 70 damage -> " + p);
        p.takeDamage(-999); System.out.println("  'negative damage' ignored -> " + p);
        p.takeDamage(45); System.out.println("  took 45 (lost a life) -> " + p);
        p.heal(500); System.out.println("  healed 500 (capped) -> " + p);

        System.out.println("\n=== POLYMORPHISM (dynamic dispatch) ===");
        List<Shape> shapes = List.of(new Circle(1.5), new Rectangle(2, 5), new Square(3), new Shape());
        double total = 0;
        for (Shape s : shapes) {                       // static type Shape, runtime types differ
            System.out.printf("  %-16s area %6.2f%n", s.name(), s.area());
            total += s.area();
        }
        System.out.printf("  total area %.2f%n", total);

        System.out.println("\n=== instanceof PATTERN MATCHING ===");
        for (Shape s : shapes) {
            if (s instanceof Square sq) {               // test + cast in one step
                System.out.printf("  %s has diagonal %.2f%n", sq.name(), sq.diagonal());
            } else if (s instanceof Circle c && c.r > 1) {
                System.out.println("  " + c.name() + " is a big circle");
            }
        }

        System.out.println("\n=== OVERLOAD (compile time) vs OVERRIDE (run time) ===");
        Shape hidden = new Square(2);                   // declared Shape, really a Square
        System.out.println("  hidden.name()     -> " + hidden.name() + "   (override: runtime type wins)");
        System.out.println("  describe(hidden)  -> " + describe(hidden) + "   (overload: declared type wins)");
    }
}
