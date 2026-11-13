/*
 * ============================================================================
 *  SECTION 13 : NESTED CLASSES AND TYPES - static nested, inner, local,
 *               anonymous classes, local records/enums/interfaces
 * ============================================================================
 *
 *  FOUR KINDS OF NESTED CLASS
 *  --------------------------
 *   | Kind          | Declared              | Needs outer instance? | Typical use                       |
 *   |---------------|-----------------------|-----------------------|-----------------------------------|
 *   | static nested | static class X {}     | no                    | helper/builder/Node grouped with owner |
 *   | inner         | class X {} (no static)| YES (outer.new X())   | iterator over the outer's data    |
 *   | local         | inside a method       | if in instance method | one-off helper used by one method |
 *   | anonymous     | new Iface() { ... }   | as above              | one-shot implementation/callback  |
 *
 *  ACCESS RULES
 *   - Nested classes can access the OUTER class's private members, and vice versa.
 *   - Inner classes hold a hidden reference to their outer object (Outer.this),
 *     which costs memory and can cause leaks -> prefer static nested unless you
 *     truly need the outer instance.
 *   - Local and anonymous classes can capture local variables only if they are
 *     final or EFFECTIVELY final (never reassigned).
 *
 *  Java 16+: records, enums and interfaces can be declared LOCALLY (inside a method).
 *
 *  HOW TO RUN:  javac NestedClasses.java && java NestedClasses
 * ============================================================================
 */
import java.util.*;

public class NestedClasses {

    // ======================== 1) static nested: the Builder pattern ========================
    static class Pizza {
        private final String size;
        private final List<String> toppings;
        private final boolean extraCheese;

        private Pizza(Builder b) {                         // only the Builder can construct
            size = b.size; toppings = b.toppings; extraCheese = b.extraCheese;
        }

        static class Builder {                             // static nested: no Pizza needed to exist yet
            private String size = "medium";
            private final List<String> toppings = new ArrayList<>();
            private boolean extraCheese;
            Builder size(String s) { size = s; return this; }
            Builder topping(String t) { toppings.add(t); return this; }
            Builder extraCheese() { extraCheese = true; return this; }
            Pizza build() { return new Pizza(this); }      // can call Pizza's PRIVATE constructor
        }

        @Override public String toString() { return size + " pizza with " + toppings + (extraCheese ? " + extra cheese" : ""); }
    }

    // ======================== 2) inner class: needs the outer instance ========================
    static class Playlist implements Iterable<String> {
        private final String[] songs;
        private final String owner;
        Playlist(String owner, String... songs) { this.owner = owner; this.songs = songs; }

        class ShuffleIterator implements Iterator<String> {   // INNER: reads Playlist.this.songs
            private final int[] order;
            private int pos;
            ShuffleIterator(long seed) {
                List<Integer> idx = new ArrayList<>();
                for (int i = 0; i < songs.length; i++) idx.add(i);
                Collections.shuffle(idx, new Random(seed));
                order = idx.stream().mapToInt(Integer::intValue).toArray();
            }
            @Override public boolean hasNext() { return pos < order.length; }
            @Override public String next() { return songs[order[pos++]] + " (" + Playlist.this.owner + "'s list)"; }
        }

        @Override public Iterator<String> iterator() { return new ShuffleIterator(42); }
    }

    // ======================== instance counter to show Outer.this ========================
    private int clicks = 0;                                // private field of the OUTER class

    class Button {                                         // inner class of NestedClasses
        private final String label;
        Button(String label) { this.label = label; }
        void click() { clicks++; System.out.println("  [" + label + "] clicked, total clicks on this screen = " + clicks); }
    }

    // ======================== 3) local and 4) anonymous classes ========================
    static List<String> sortEmployees(List<String[]> rows, String mode) {
        // LOCAL record (Java 16+): only meaningful inside this method
        record Emp(String name, String dept, int salary) { }

        List<Emp> emps = new ArrayList<>();
        for (String[] r : rows) emps.add(new Emp(r[0], r[1], Integer.parseInt(r[2])));

        int bonusThreshold = 90_000;                       // effectively final -> may be captured

        // LOCAL class: a named comparator used only here
        class BySalaryDesc implements Comparator<Emp> {
            @Override public int compare(Emp a, Emp b) { return Integer.compare(b.salary(), a.salary()); }
        }

        Comparator<Emp> cmp = switch (mode) {
            case "salary" -> new BySalaryDesc();
            // ANONYMOUS class: implement Comparator inline, no name
            case "dept" -> new Comparator<Emp>() {
                @Override public int compare(Emp a, Emp b) {
                    int c = a.dept().compareTo(b.dept());
                    return c != 0 ? c : a.name().compareTo(b.name());
                }
            };
            default -> Comparator.comparing(Emp::name);    // lambda (next section) replaces most anonymous classes
        };
        emps.sort(cmp);
        List<String> out = new ArrayList<>();
        for (Emp e : emps) out.add(String.format("%-7s %-6s %,7d%s", e.name(), e.dept(), e.salary(), e.salary() >= bonusThreshold ? "  *bonus*" : ""));
        // bonusThreshold = 1;                             // would break the capture above -> compile error
        return out;
    }

    public static void main(String[] args) {
        System.out.println("=== STATIC NESTED CLASS: Builder ===");
        Pizza p = new Pizza.Builder().size("large").topping("paneer").topping("capsicum").extraCheese().build();
        System.out.println("  " + p);
        System.out.println("  " + new Pizza.Builder().topping("corn").build());

        System.out.println("\n=== INNER CLASS: iterator that reads the outer object ===");
        Playlist mine = new Playlist("Srikar", "Kesariya", "Believer", "Levitating", "Naatu Naatu");
        for (String s : mine) System.out.println("  " + s);
        Playlist.ShuffleIterator explicit = mine.new ShuffleIterator(7);   // outer.new Inner() syntax
        System.out.println("  explicitly created inner iterator, first = " + explicit.next());

        System.out.println("\n=== INNER CLASS SHARING OUTER STATE ===");
        NestedClasses screen = new NestedClasses();
        NestedClasses.Button ok = screen.new Button("OK"), cancel = screen.new Button("Cancel");
        ok.click(); cancel.click(); ok.click();             // both buttons update the SAME outer counter

        System.out.println("\n=== LOCAL RECORD + LOCAL CLASS + ANONYMOUS CLASS ===");
        List<String[]> rows = List.of(new String[]{"Asha", "ENG", "95000"}, new String[]{"Ravi", "SALES", "60000"},
                new String[]{"Meera", "ENG", "120000"}, new String[]{"Karan", "HR", "55000"}, new String[]{"Divya", "SALES", "91000"});
        for (String mode : List.of("salary", "dept", "name")) {
            System.out.println("  sorted by " + mode + ":");
            for (String line : sortEmployees(rows, mode)) System.out.println("    " + line);
        }

        System.out.println("\n=== ANONYMOUS CLASS WITH STATE ===");
        Runnable counter = new Runnable() {
            private int runs;                               // anonymous classes CAN have fields (lambdas can't)
            @Override public void run() { System.out.println("  run #" + (++runs)); }
        };
        counter.run(); counter.run();
        System.out.println("  anonymous class name at runtime: " + counter.getClass().getName());
    }
}
