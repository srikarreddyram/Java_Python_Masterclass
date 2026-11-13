/*
 * ============================================================================
 *  SECTION 10 : LISTS, ArrayList, LinkedList, ITERATORS, AUTOBOXING, ENUMS
 * ============================================================================
 *
 *  List<E> = an ORDERED collection that allows duplicates and index access.
 *   ArrayList  : backed by a resizable array. get(i) O(1); add/remove in the
 *                middle O(n) (shifting). The default choice.
 *   LinkedList : doubly linked nodes. add/remove at either END O(1); get(i) O(n).
 *                Also implements Deque (addFirst, pollLast...).
 *
 *  Declare with the INTERFACE type:  List<String> names = new ArrayList<>();
 *  so you can swap implementations later without touching the rest of the code.
 *
 *  ITERATORS
 *   Iterator<E>     : hasNext(), next(), remove()  - the ONLY safe way to remove
 *                     while looping (for-each + list.remove -> ConcurrentModificationException)
 *   ListIterator<E> : can also go backwards (hasPrevious/previous), add, set
 *
 *  AUTOBOXING: collections hold OBJECTS, so int <-> Integer convert automatically.
 *   - Integer caches -128..127, so == "works" for small values and FAILS above.
 *   - Unboxing a null Integer throws NullPointerException.
 *
 *  ENUM: a fixed set of named constants that are real objects - they can have
 *  fields, constructors, methods, and work in switch.
 *
 *  HOW TO RUN:  javac ListsIteratorsEnums.java && java ListsIteratorsEnums
 * ============================================================================
 */
import java.util.*;

public class ListsIteratorsEnums {

    enum Size {
        SMALL("S", 250), MEDIUM("M", 350), LARGE("L", 500);    // each constant calls the constructor

        private final String code;
        private final int ml;

        Size(String code, int ml) { this.code = code; this.ml = ml; }   // enum constructors are private

        int ml() { return ml; }
        Size upgrade() { return this == LARGE ? LARGE : values()[ordinal() + 1]; }
        @Override public String toString() { return code + "(" + ml + "ml)"; }
    }

    enum Planet {
        MERCURY(3.303e23, 2.4397e6), EARTH(5.976e24, 6.37814e6), JUPITER(1.9e27, 7.1492e7);
        private final double mass, radius;
        Planet(double mass, double radius) { this.mass = mass; this.radius = radius; }
        double surfaceGravity() { return 6.67300E-11 * mass / (radius * radius); }
    }

    public static void main(String[] args) {
        arrayListDemo();
        linkedListDemo();
        iteratorDemo();
        autoboxingDemo();
        enumDemo();
    }

    static void arrayListDemo() {
        System.out.println("=== ArrayList ===");
        List<String> groceries = new ArrayList<>(List.of("milk", "eggs", "bread"));
        groceries.add("apples");                        // append
        groceries.add(1, "butter");                     // insert at index (shifts the rest)
        groceries.set(0, "oat milk");                   // replace
        groceries.addAll(List.of("tea", "eggs"));       // duplicates allowed
        System.out.println("  list         : " + groceries + " size=" + groceries.size());
        System.out.println("  get(2)       : " + groceries.get(2) + ", indexOf(eggs)=" + groceries.indexOf("eggs") + ", lastIndexOf(eggs)=" + groceries.lastIndexOf("eggs"));
        groceries.remove("eggs");                       // removes FIRST match (by object)
        groceries.remove(0);                            // removes by INDEX
        System.out.println("  after removes: " + groceries + ", contains(tea)=" + groceries.contains("tea"));
        groceries.sort(Comparator.naturalOrder());
        System.out.println("  sorted       : " + groceries);
        groceries.removeIf(s -> s.startsWith("b"));     // bulk remove with a condition
        System.out.println("  removeIf b*  : " + groceries);
        String[] asArray = groceries.toArray(new String[0]);
        System.out.println("  toArray      : " + Arrays.toString(asArray));
        List<Integer> fixed = List.of(1, 2, 3);         // IMMUTABLE list
        try { fixed.add(4); } catch (UnsupportedOperationException e) { System.out.println("  List.of(...) is immutable -> UnsupportedOperationException"); }
        List<Integer> arrView = Arrays.asList(5, 6, 7); // fixed-SIZE view: set ok, add not
        arrView.set(0, 50);
        System.out.println("  Arrays.asList set ok: " + arrView);
    }

    static void linkedListDemo() {
        System.out.println("\n=== LinkedList as a list, queue and stack ===");
        LinkedList<String> tasks = new LinkedList<>();
        tasks.add("write code");                        // tail
        tasks.addFirst("plan");                         // head, O(1)
        tasks.addLast("deploy");                        // tail, O(1)
        tasks.add(2, "test");                           // walks to index 2
        System.out.println("  tasks: " + tasks + "  first=" + tasks.getFirst() + " last=" + tasks.getLast());
        System.out.println("  queue poll()  -> " + tasks.poll() + ", now " + tasks);
        tasks.push("hotfix");                           // stack push = addFirst
        System.out.println("  stack push/pop-> pop()=" + tasks.pop() + ", peekLast=" + tasks.peekLast());
    }

    static void iteratorDemo() {
        System.out.println("\n=== ITERATORS ===");
        List<Integer> nums = new ArrayList<>(List.of(4, 7, 10, 13, 16, 19));
        try {
            for (Integer x : nums) if (x % 2 == 0) nums.remove(x);   // WRONG
        } catch (ConcurrentModificationException e) {
            System.out.println("  removing inside for-each -> ConcurrentModificationException");
        }
        nums = new ArrayList<>(List.of(4, 7, 10, 13, 16, 19));
        Iterator<Integer> it = nums.iterator();
        while (it.hasNext()) if (it.next() % 2 == 0) it.remove();   // RIGHT
        System.out.println("  iterator.remove() evens -> " + nums);

        ListIterator<String> li = new ArrayList<>(List.of("a", "b", "c")).listIterator();
        StringBuilder forward = new StringBuilder(), backward = new StringBuilder();
        while (li.hasNext()) { String s = li.next(); li.set(s.toUpperCase()); forward.append(s); }
        while (li.hasPrevious()) backward.append(li.previous());
        System.out.println("  ListIterator forward=" + forward + " (set to upper) backward=" + backward);
    }

    static void autoboxingDemo() {
        System.out.println("\n=== AUTOBOXING / UNBOXING ===");
        List<Integer> list = new ArrayList<>();
        list.add(42);                                   // autobox: Integer.valueOf(42)
        int back = list.get(0);                         // unbox: .intValue()
        System.out.println("  boxed and unboxed: " + back);
        Integer a = 127, b = 127, c = 128, d = 128;
        System.out.println("  Integer 127 == 127: " + (a == b) + "  (cached)");
        System.out.println("  Integer 128 == 128: " + (c == d) + " <- different objects! use equals(): " + c.equals(d));
        Integer missing = null;
        try {
            int boom = missing;                         // unboxing null
            System.out.println(boom);
        } catch (NullPointerException e) {
            System.out.println("  unboxing null -> NullPointerException");
        }
        List<Integer> idx = new ArrayList<>(List.of(10, 20, 30));
        idx.remove(Integer.valueOf(10));                // remove(Object) - the VALUE 10
        idx.remove(0);                                  // remove(int index) - the element 20
        System.out.println("  remove(Integer.valueOf(10)) then remove(0) -> " + idx);
    }

    static void enumDemo() {
        System.out.println("\n=== ENUMS ===");
        Size s = Size.valueOf("MEDIUM");
        System.out.println("  values(): " + Arrays.toString(Size.values()) + ", MEDIUM.ordinal()=" + s.ordinal() + ", name()=" + s.name());
        System.out.println("  upgrade: " + s + " -> " + s.upgrade() + " -> " + s.upgrade().upgrade());
        String price = switch (s) {                     // exhaustive switch on an enum
            case SMALL -> "Rs 120";
            case MEDIUM -> "Rs 160";
            case LARGE -> "Rs 210";
        };
        System.out.println("  switch on enum: " + s + " costs " + price);
        for (Planet p : Planet.values()) System.out.printf("  %-8s gravity %.2f m/s^2%n", p, p.surfaceGravity());
        EnumMap<Size, Integer> orders = new EnumMap<>(Size.class);
        orders.merge(Size.LARGE, 1, Integer::sum);
        orders.merge(Size.SMALL, 2, Integer::sum);
        orders.merge(Size.LARGE, 1, Integer::sum);
        System.out.println("  EnumMap order counts (kept in declaration order): " + orders);
    }
}
