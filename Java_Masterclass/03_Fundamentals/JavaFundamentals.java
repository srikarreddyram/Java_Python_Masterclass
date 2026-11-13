/*
 * ============================================================================
 *  SECTION 03 : JAVA FUNDAMENTALS - variables, primitive types, casting,
 *               operators, char/boolean, and String basics
 * ============================================================================
 *
 *  VARIABLES
 *  ---------
 *  A variable is a named, TYPED storage location. Java is statically typed:
 *  the type is fixed at compile time.      int age = 21;   type name = value;
 *  'var' (Java 10+) lets the compiler infer the type from the right-hand side
 *  (still static: var x = 5;  x = "hi"; is a compile error).
 *
 *  THE 8 PRIMITIVE TYPES
 *  ---------------------
 *   | type    | bits | range / notes                          | default |
 *   |---------|------|----------------------------------------|---------|
 *   | byte    | 8    | -128 .. 127                            | 0       |
 *   | short   | 16   | -32,768 .. 32,767                      | 0       |
 *   | int     | 32   | about +-2.1 billion (the default int)  | 0       |
 *   | long    | 64   | about +-9.2 x 10^18 (suffix L)         | 0L      |
 *   | float   | 32   | ~7 decimal digits (suffix f)           | 0.0f    |
 *   | double  | 64   | ~15-16 digits (the default decimal)    | 0.0d    |
 *   | char    | 16   | one UTF-16 code unit, 'A', 'A'    | '\u0000'|
 *   | boolean | -    | true / false                           | false   |
 *  Everything else (String, arrays, your classes) is a REFERENCE type.
 *
 *  CASTING
 *  -------
 *   Widening  (small -> big)  is automatic:   long l = someInt;
 *   Narrowing (big -> small)  needs a cast:   int i = (int) someLong;  (may lose data!)
 *   int / int is INTEGER division (7/2 == 3); make one side double for 3.5.
 *
 *  OPERATORS (precedence high -> low, simplified)
 *   ++ --   * / %   + -   < > <= >=   == !=   &&   ||   ?:   = += -= ...
 *
 *  HOW TO RUN:  javac JavaFundamentals.java && java JavaFundamentals
 * ============================================================================
 */
public class JavaFundamentals {

    public static void main(String[] args) {
        primitives();
        overflowAndPrecision();
        casting();
        operators();
        charsAndBooleans();
        stringBasics();
    }

    static void primitives() {
        System.out.println("=== PRIMITIVE TYPES AND THEIR LIMITS ===");
        System.out.printf("  byte   %d .. %d%n", Byte.MIN_VALUE, Byte.MAX_VALUE);
        System.out.printf("  short  %d .. %d%n", Short.MIN_VALUE, Short.MAX_VALUE);
        System.out.printf("  int    %,d .. %,d%n", Integer.MIN_VALUE, Integer.MAX_VALUE);
        System.out.printf("  long   %,d .. %,d%n", Long.MIN_VALUE, Long.MAX_VALUE);
        System.out.printf("  float  max %e   double max %e%n", Float.MAX_VALUE, Double.MAX_VALUE);

        int population = 1_428_627_663;        // underscores make big literals readable
        long worldPopulation = 8_100_000_000L; // too big for int -> long with L suffix
        var inferred = 3.14;                   // compiler infers double
        System.out.println("  India population (int): " + population);
        System.out.println("  World population (long): " + worldPopulation);
        System.out.println("  var inferred type is double: " + ((Object) inferred).getClass().getSimpleName());
    }

    static void overflowAndPrecision() {
        System.out.println("\n=== OVERFLOW AND FLOATING-POINT PRECISION ===");
        int max = Integer.MAX_VALUE;
        System.out.println("  Integer.MAX_VALUE + 1 = " + (max + 1) + "   <- silently wraps around!");
        System.out.println("  Math.addExact detects it: ");
        try {
            Math.addExact(max, 1);
        } catch (ArithmeticException e) {
            System.out.println("    ArithmeticException: " + e.getMessage());
        }
        System.out.println("  0.1 + 0.2 = " + (0.1 + 0.2) + "   <- binary floating point can't store 0.1 exactly");
        System.out.println("  For money use BigDecimal: " + new java.math.BigDecimal("0.1").add(new java.math.BigDecimal("0.2")));
    }

    static void casting() {
        System.out.println("\n=== CASTING ===");
        int i = 300;
        long widened = i;                      // automatic widening
        byte narrowed = (byte) i;              // 300 doesn't fit: 300 - 256 = 44
        double d = 9.99;
        int truncated = (int) d;               // casting truncates, it does NOT round
        System.out.println("  int 300 -> long " + widened + ", -> byte " + narrowed);
        System.out.println("  (int) 9.99 = " + truncated + ", Math.round(9.99) = " + Math.round(d));
        System.out.println("  7 / 2 = " + (7 / 2) + "   7 / 2.0 = " + (7 / 2.0) + "   (double) 7 / 2 = " + ((double) 7 / 2));
        byte b = 10;
        b += 5;                                // compound assignment casts implicitly
        // b = b + 5;                          // would NOT compile: b + 5 is an int
        System.out.println("  byte b += 5 -> " + b + " (compound operators include a hidden cast)");
    }

    static void operators() {
        System.out.println("\n=== OPERATORS ===");
        int a = 17, b = 5;
        System.out.printf("  %d + %d = %d, %d - %d = %d, %d * %d = %d, %d / %d = %d, %d %% %d = %d%n",
                a, b, a + b, a, b, a - b, a, b, a * b, a, b, a / b, a, b, a % b);
        int x = 5;
        int post = x++;                        // use, THEN increment
        int pre = ++x;                         // increment, THEN use
        System.out.println("  x=5: x++ gives " + post + ", then ++x gives " + pre);
        System.out.println("  2 + 3 * 4 = " + (2 + 3 * 4) + "  (2 + 3) * 4 = " + ((2 + 3) * 4));
        boolean adult = 21 >= 18, hasId = false;
        System.out.println("  adult && hasId = " + (adult && hasId) + ", adult || hasId = " + (adult || hasId) + ", !hasId = " + !hasId);
        String label = a > b ? "a is bigger" : "b is bigger";   // ternary operator
        System.out.println("  ternary: " + label);
        // Short-circuit: the right side is NOT evaluated when the left decides the result
        String s = null;
        System.out.println("  short-circuit null check: " + (s != null && s.length() > 0));
    }

    static void charsAndBooleans() {
        System.out.println("\n=== CHAR AND BOOLEAN ===");
        char c = 'A';
        char unicode = 'A';               // also 'A'
        int code = c;                          // char is a 16-bit number underneath
        System.out.println("  'A' == '\\u0041': " + (c == unicode) + ", code point = " + code);
        System.out.println("  'A' + 2 = " + (char) (c + 2) + "   Character.isDigit('7') = " + Character.isDigit('7'));
        boolean isJavaFun = true;
        System.out.println("  boolean has no numeric value in Java (unlike C): isJavaFun = " + isJavaFun);
    }

    static void stringBasics() {
        System.out.println("\n=== STRING BASICS (a reference type, immutable) ===");
        String first = "Srikar", course = "Java";
        String msg = first + " is learning " + course + " " + 17;   // + concatenates
        System.out.println("  " + msg);
        System.out.println("  \"10\" + 5 + 5 = " + ("10" + 5 + 5) + "   5 + 5 + \"10\" = " + (5 + 5 + "10") + "  (left to right!)");
        String a = "hello", b = "hel" + "lo", c = new String("hello");
        System.out.println("  a == b (both literals, string pool): " + (a == b));
        System.out.println("  a == new String(\"hello\"): " + (a == c) + "  <- different objects");
        System.out.println("  a.equals(c): " + a.equals(c) + "  <- ALWAYS compare strings with equals()");
    }
}
