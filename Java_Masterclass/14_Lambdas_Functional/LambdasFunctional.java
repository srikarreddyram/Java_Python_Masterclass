/*
 * ============================================================================
 *  SECTION 14 : LAMBDA EXPRESSIONS, FUNCTIONAL INTERFACES, METHOD REFERENCES
 * ============================================================================
 *
 *  FUNCTIONAL INTERFACE = an interface with exactly ONE abstract method
 *  (annotate with @FunctionalInterface so the compiler checks it).
 *  A LAMBDA is a compact way to supply that one method:
 *
 *      Comparator<String> c = new Comparator<>() {            // anonymous class
 *          public int compare(String a, String b) { return a.length() - b.length(); }
 *      };
 *      Comparator<String> c = (a, b) -> a.length() - b.length();   // lambda
 *
 *  LAMBDA SYNTAX
 *      () -> 42                         no parameters
 *      x -> x * 2                       one parameter, parentheses optional
 *      (a, b) -> a + b                  several parameters
 *      (String s) -> { ...; return s; } explicit types + block body
 *
 *  THE BIG FOUR in java.util.function (+ variants)
 *   | Interface        | Method             | Shape            | Example                    |
 *   |------------------|--------------------|------------------|----------------------------|
 *   | Function<T,R>    | R apply(T)         | T -> R           | String::length             |
 *   | Predicate<T>     | boolean test(T)    | T -> boolean     | s -> s.isEmpty()           |
 *   | Consumer<T>      | void accept(T)     | T -> nothing     | System.out::println        |
 *   | Supplier<T>      | T get()            | nothing -> T     | ArrayList::new             |
 *   BiFunction<T,U,R>, UnaryOperator<T> (T->T), BinaryOperator<T> ((T,T)->T),
 *   BiConsumer, BiPredicate, plus primitive versions IntPredicate, ToIntFunction...
 *
 *  METHOD REFERENCES (4 kinds)
 *   static method          Integer::parseInt        s -> Integer.parseInt(s)
 *   bound instance         greeting::concat         s -> greeting.concat(s)
 *   unbound instance       String::toUpperCase      s -> s.toUpperCase()
 *   constructor            StringBuilder::new       () -> new StringBuilder()
 *
 *  CAPTURE: lambdas may use local variables only if effectively final.
 *
 *  HOW TO RUN:  javac LambdasFunctional.java && java LambdasFunctional
 * ============================================================================
 */
import java.util.*;
import java.util.function.*;

public class LambdasFunctional {

    @FunctionalInterface
    interface DiscountRule {                              // our own functional interface
        double apply(double price);
        default DiscountRule then(DiscountRule next) {    // default methods are allowed
            return p -> next.apply(apply(p));
        }
    }

    record Product(String name, String category, double price, double rating) { }

    static <T, R> List<R> mapAll(List<T> items, Function<? super T, ? extends R> fn) {   // passing behaviour as data
        List<R> out = new ArrayList<>();
        for (T t : items) out.add(fn.apply(t));
        return out;
    }

    public static void main(String[] args) {
        System.out.println("=== FROM ANONYMOUS CLASS TO LAMBDA ===");
        List<String> words = new ArrayList<>(List.of("banana", "kiwi", "apple", "fig", "cherry"));
        words.sort(new Comparator<String>() {
            @Override public int compare(String a, String b) { return a.length() - b.length(); }
        });
        System.out.println("  anonymous class sort: " + words);
        words.sort((a, b) -> b.compareTo(a));
        System.out.println("  lambda (reverse alpha): " + words);

        System.out.println("\n=== THE BIG FOUR ===");
        Function<String, Integer> length = String::length;
        Predicate<String> longWord = s -> s.length() > 4;
        Consumer<String> shout = s -> System.out.print(s.toUpperCase() + " ");
        Supplier<List<String>> listMaker = ArrayList::new;
        System.out.println("  Function  length(\"masterclass\") = " + length.apply("masterclass"));
        System.out.println("  Predicate longWord(\"fig\") = " + longWord.test("fig"));
        System.out.print("  Consumer  ");
        words.forEach(shout);
        System.out.println();
        List<String> fresh = listMaker.get();
        fresh.add("made by a Supplier");
        System.out.println("  Supplier  " + fresh);

        System.out.println("\n=== MORE SHAPES ===");
        BiFunction<Double, Integer, Double> total = (price, qty) -> price * qty;
        UnaryOperator<String> trimUpper = s -> s.strip().toUpperCase();
        BinaryOperator<Integer> maxOp = BinaryOperator.maxBy(Comparator.naturalOrder());
        IntPredicate even = n -> n % 2 == 0;                   // primitive specialisation: no boxing
        System.out.println("  BiFunction 249.5 x 3 = " + total.apply(249.5, 3));
        System.out.println("  UnaryOperator '  java  ' -> '" + trimUpper.apply("  java  ") + "'");
        System.out.println("  BinaryOperator.maxBy(7, 12) = " + maxOp.apply(7, 12));
        System.out.println("  IntPredicate even(10) = " + even.test(10));

        System.out.println("\n=== COMPOSING FUNCTIONS ===");
        Function<Integer, Integer> plus2 = x -> x + 2, times3 = x -> x * 3;
        System.out.println("  plus2.andThen(times3)(4) = " + plus2.andThen(times3).apply(4) + "   (4+2)*3");
        System.out.println("  plus2.compose(times3)(4) = " + plus2.compose(times3).apply(4) + "   4*3+2");
        Predicate<String> startsWithC = s -> s.startsWith("c");
        System.out.println("  longWord.and(startsWithC) on cherry = " + longWord.and(startsWithC).test("cherry")
                + ", .negate() on kiwi = " + longWord.negate().test("kiwi"));
        DiscountRule festive = p -> p * 0.9, coupon = p -> p - 100, gstInclusive = p -> p * 1.18;
        System.out.printf("  custom interface chain: Rs 2000 -> 10%% off -> Rs 100 coupon -> +18%% GST = Rs %.2f%n",
                festive.then(coupon).then(gstInclusive).apply(2000));

        System.out.println("\n=== METHOD REFERENCES ===");
        Function<String, Integer> parse = Integer::parseInt;               // static
        String greeting = "Hello, ";
        Function<String, String> greet = greeting::concat;                 // bound instance
        Function<String, String> upper = String::toUpperCase;              // unbound instance
        Supplier<StringBuilder> sbMaker = StringBuilder::new;              // constructor
        Function<Integer, int[]> arrayMaker = int[]::new;                  // array constructor
        System.out.println("  Integer::parseInt(\"2026\") + 1 = " + (parse.apply("2026") + 1));
        System.out.println("  greeting::concat(\"Srikar\") = " + greet.apply("Srikar"));
        System.out.println("  String::toUpperCase(\"lambda\") = " + upper.apply("lambda"));
        System.out.println("  StringBuilder::new -> " + sbMaker.get().append("built").reverse());
        System.out.println("  int[]::new(4).length = " + arrayMaker.apply(4).length);

        System.out.println("\n=== COLLECTION METHODS THAT TAKE LAMBDAS ===");
        List<Product> catalog = new ArrayList<>(List.of(
                new Product("Mouse", "electronics", 699, 4.2), new Product("Desk Lamp", "home", 1299, 4.5),
                new Product("Keyboard", "electronics", 2499, 4.6), new Product("Mug", "home", 299, 3.9),
                new Product("Headphones", "electronics", 3999, 4.4)));
        catalog.sort(Comparator.comparing(Product::category).thenComparing(Product::price, Comparator.reverseOrder()));
        catalog.forEach(p -> System.out.printf("  %-11s %-11s Rs %5.0f  %.1f*%n", p.category(), p.name(), p.price(), p.rating()));
        catalog.removeIf(p -> p.rating() < 4.0);
        System.out.println("  after removeIf(rating < 4): " + mapAll(catalog, Product::name));
        Map<String, Double> spendByCategory = new TreeMap<>();
        catalog.forEach(p -> spendByCategory.merge(p.category(), p.price(), Double::sum));
        System.out.println("  merge with Double::sum: " + spendByCategory);
        List<String> names = new ArrayList<>(mapAll(catalog, Product::name));
        names.replaceAll(String::toUpperCase);
        System.out.println("  replaceAll(String::toUpperCase): " + names);

        System.out.println("\n=== CAPTURING VARIABLES ===");
        int minRating = 4;                                    // effectively final
        Predicate<Product> good = p -> p.rating() >= minRating;
        // minRating = 5;                                     // would make the lambda above a compile error
        System.out.println("  products rated >= " + minRating + ": " + catalog.stream().filter(good).count());
        int[] counter = {0};                                  // workaround: mutate the CONTENTS of a final array
        catalog.forEach(p -> counter[0]++);
        System.out.println("  counted via array trick: " + counter[0] + "  (prefer streams' count() in real code)");
    }
}
