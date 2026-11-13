/*
 * ============================================================================
 *  SECTION 12 : GENERICS - generic classes, generic methods, bounded types,
 *               wildcards (? extends / ? super), type erasure, raw types
 * ============================================================================
 *
 *  WHY GENERICS?
 *  -------------
 *   Before Java 5:  List list = new ArrayList();  list.add("x"); list.add(42);
 *                   String s = (String) list.get(1);   // ClassCastException at RUNTIME
 *   With generics:  List<String> list = new ArrayList<>();
 *                   list.add(42);                      // COMPILE error - caught early
 *   -> type safety + no casts + reusable code written once for many types.
 *
 *  SYNTAX
 *   class Box<T> { T value; }                    generic class, T = type parameter
 *   static <T> T firstOf(List<T> list)           generic method
 *   class Stats<T extends Number>                BOUNDED type: T must be a Number
 *   <T extends Comparable<T>>                     T must be comparable with itself
 *   Naming: T type, E element, K key, V value, N number, R result
 *
 *  WILDCARDS  (PECS = Producer Extends, Consumer Super)
 *   List<? extends Number>  : you can READ Numbers out (producer); can't add
 *   List<? super Integer>   : you can ADD Integers in (consumer); reads give Object
 *   List<?>                 : unknown type, read-only as Object
 *   NOTE: List<Integer> is NOT a List<Number> (generics are invariant).
 *
 *  TYPE ERASURE: generic types exist only at compile time. At runtime
 *  List<String> and List<Integer> are both just List. So: no new T(),
 *  no T[] creation, no instanceof List<String>, no primitives (use Integer).
 *
 *  HOW TO RUN:  javac GenericsDeepDive.java && java GenericsDeepDive
 * ============================================================================
 */
import java.util.*;
import java.util.function.Function;

public class GenericsDeepDive {

    // ======================== generic class ========================
    static class Box<T> {
        private final T value;
        Box(T value) { this.value = value; }
        T get() { return value; }
        <R> Box<R> map(Function<T, R> fn) { return new Box<>(fn.apply(value)); }   // generic method in a generic class
        @Override public String toString() { return "Box[" + value + "]"; }
    }

    // ======================== two type parameters ========================
    record Pair<K, V>(K key, V value) {
        <V2> Pair<K, V2> withValue(V2 v) { return new Pair<>(key, v); }
    }

    // ======================== bounded type parameter ========================
    static class Stats<T extends Number> {          // only numeric types allowed
        private final List<T> data;
        Stats(List<T> data) { this.data = data; }
        double mean() {
            double sum = 0;
            for (T t : data) sum += t.doubleValue();    // doubleValue() exists because T extends Number
            return data.isEmpty() ? 0 : sum / data.size();
        }
    }

    // ======================== generic methods ========================
    static <T extends Comparable<T>> T max(List<T> items) {
        T best = items.get(0);
        for (T item : items) if (item.compareTo(best) > 0) best = item;
        return best;
    }

    static <T> void swap(T[] arr, int i, int j) { T t = arr[i]; arr[i] = arr[j]; arr[j] = t; }

    // ======================== wildcards ========================
    static double sumAll(List<? extends Number> nums) {   // PRODUCER: we only read
        double s = 0;
        for (Number n : nums) s += n.doubleValue();
        // nums.add(5);                                     // compile error: unknown exact type
        return s;
    }

    static void addSquares(List<? super Integer> sink, int n) {   // CONSUMER: we only write Integers
        for (int i = 1; i <= n; i++) sink.add(i * i);
    }

    static int countUnknown(List<?> anything) { return anything.size(); }

    public static void main(String[] args) {
        System.out.println("=== GENERIC CLASS ===");
        Box<String> name = new Box<>("Srikar");
        Box<Integer> length = name.map(String::length);    // Box<String> -> Box<Integer>
        System.out.println("  " + name + " -> map(length) -> " + length + ", no cast needed: " + (length.get() + 1));

        System.out.println("\n=== MULTIPLE TYPE PARAMETERS ===");
        Pair<String, Integer> score = new Pair<>("Java", 94);
        Pair<String, String> graded = score.withValue("A+");
        System.out.println("  " + score + " -> " + graded);

        System.out.println("\n=== BOUNDED TYPES ===");
        System.out.printf("  Stats<Integer> mean = %.2f%n", new Stats<>(List.of(3, 5, 10)).mean());
        System.out.printf("  Stats<Double>  mean = %.2f%n", new Stats<>(List.of(2.5, 3.5)).mean());
        // new Stats<>(List.of("a"));                       // compile error: String is not a Number

        System.out.println("\n=== GENERIC METHODS (type inferred at the call) ===");
        System.out.println("  max(ints)    = " + max(List.of(4, 19, 7)));
        System.out.println("  max(strings) = " + max(List.of("pear", "apple", "zucchini")));
        String[] arr = {"a", "b", "c"};
        GenericsDeepDive.<String>swap(arr, 0, 2);          // explicit type argument (rarely needed)
        System.out.println("  swap -> " + Arrays.toString(arr));

        System.out.println("\n=== WILDCARDS: PECS ===");
        List<Integer> ints = List.of(1, 2, 3);
        List<Double> doubles = List.of(1.5, 2.5);
        System.out.println("  sumAll(List<Integer>) = " + sumAll(ints) + ", sumAll(List<Double>) = " + sumAll(doubles));
        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        addSquares(numbers, 3);                             // List<Number> is a List<? super Integer>
        addSquares(objects, 2);                             // so is List<Object>
        System.out.println("  addSquares into List<Number> " + numbers + " and List<Object> " + objects);
        System.out.println("  countUnknown(List<?>) = " + countUnknown(List.of('x', 'y')));
        // List<Number> bad = ints;                         // compile error: generics are INVARIANT

        System.out.println("\n=== TYPE ERASURE ===");
        List<String> strs = new ArrayList<>();
        List<Integer> nums = new ArrayList<>();
        System.out.println("  same runtime class: " + (strs.getClass() == nums.getClass()) + " -> " + strs.getClass().getName());

        rawTypeDemo();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})        // we are deliberately misusing raw types here
    static void rawTypeDemo() {
        System.out.println("\n=== WHY NOT RAW TYPES ===");
        List raw = new ArrayList();                         // compiles, with a warning
        raw.add("text");
        raw.add(42);
        try {
            List<String> typed = raw;
            String s = typed.get(1);                        // fails only at runtime
            System.out.println(s);
        } catch (ClassCastException e) {
            System.out.println("  raw list hid a bug until runtime: ClassCastException");
        }
    }
}
