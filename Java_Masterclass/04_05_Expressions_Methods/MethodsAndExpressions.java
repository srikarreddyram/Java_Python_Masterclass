/*
 * ============================================================================
 *  SECTIONS 04-05 : EXPRESSIONS, STATEMENTS, CODE BLOCKS, IF-ELSE, METHODS,
 *                   OVERLOADING, VARARGS, SCOPE AND PASS-BY-VALUE
 * ============================================================================
 *
 *  EXPRESSION vs STATEMENT
 *  -----------------------
 *   Expression : produces a value              score * 2,  x > 5,  name.length()
 *   Statement  : a complete instruction (;)    int total = score * 2;
 *   Code block : statements inside { } ; variables declared inside a block
 *                are only visible (in SCOPE) inside that block.
 *
 *  METHODS
 *  -------
 *   [modifiers] returnType name(parameters) { body }
 *   static int add(int a, int b) { return a + b; }
 *   - parameters = the variables in the declaration; arguments = the values passed
 *   - 'void' means no return value; 'return' exits immediately
 *
 *  OVERLOADING: same method name, DIFFERENT parameter lists (number, type or
 *  order). The compiler picks the best match at COMPILE time. A different
 *  return type alone is NOT enough.
 *
 *  JAVA IS ALWAYS PASS-BY-VALUE
 *  ----------------------------
 *   Primitives : the method gets a copy of the number -> caller unaffected.
 *   References : the method gets a copy of the REFERENCE (the arrow), so it can
 *                change the object's contents, but re-assigning the parameter
 *                to a new object doesn't affect the caller's variable.
 *
 *  HOW TO RUN:  javac MethodsAndExpressions.java && java MethodsAndExpressions
 * ============================================================================
 */
import java.util.Arrays;

public class MethodsAndExpressions {

    // ---------------- overloading ----------------
    static int area(int side) { return side * side; }                          // square
    static int area(int length, int width) { return length * width; }          // rectangle
    static double area(double radius) { return Math.PI * radius * radius; }    // circle

    // ---------------- varargs: zero or more ints packed into an array ----------------
    static int sum(int... numbers) {
        int total = 0;
        for (int n : numbers) total += n;
        return total;
    }

    // ---------------- early return keeps code flat ----------------
    static String grade(int marks) {
        if (marks < 0 || marks > 100) return "invalid";
        if (marks >= 90) return "A+";
        if (marks >= 75) return "A";
        if (marks >= 60) return "B";
        if (marks >= 40) return "C";
        return "F";
    }

    // ---------------- pass-by-value demos ----------------
    static void tryToChange(int number, int[] array, StringBuilder sb) {
        number = 999;                  // changes only the local copy
        array[0] = 999;                // changes the SHARED object
        sb.append(" (modified)");      // changes the SHARED object
        sb = new StringBuilder("new"); // re-points the local copy only
    }

    // ---------------- recursion: a method calling itself ----------------
    static long factorial(int n) {
        return n <= 1 ? 1 : n * factorial(n - 1);   // base case stops the recursion
    }

    public static void main(String[] args) {
        System.out.println("=== EXPRESSIONS & STATEMENTS ===");
        int score = 87;                                   // statement containing expression 87
        boolean passed = score >= 40;                     // boolean expression
        System.out.println("  score=" + score + " passed=" + passed + " grade=" + grade(score));

        System.out.println("\n=== CODE BLOCKS & SCOPE ===");
        int outer = 1;
        {
            int inner = 2;                                // only exists inside these braces
            System.out.println("  inside block: outer=" + outer + " inner=" + inner);
        }
        // System.out.println(inner);                     // compile error: out of scope

        System.out.println("\n=== IF / ELSE IF / ELSE ===");
        for (int temp : new int[]{-2, 18, 29, 41}) {
            String advice;
            if (temp < 0) advice = "freezing - stay in";
            else if (temp < 20) advice = "cool - take a jacket";
            else if (temp < 35) advice = "pleasant";
            else advice = "heatwave - stay hydrated";
            System.out.printf("  %3d C -> %s%n", temp, advice);
        }

        System.out.println("\n=== OVERLOADING (compile-time choice) ===");
        System.out.println("  area(4)       square    = " + area(4));
        System.out.println("  area(4, 6)    rectangle = " + area(4, 6));
        System.out.printf("  area(4.0)     circle    = %.2f%n", area(4.0));

        System.out.println("\n=== VARARGS ===");
        System.out.println("  sum() = " + sum() + ", sum(5) = " + sum(5) + ", sum(1,2,3,4) = " + sum(1, 2, 3, 4));

        System.out.println("\n=== PASS-BY-VALUE ===");
        int n = 1;
        int[] arr = {1, 2, 3};
        StringBuilder sb = new StringBuilder("original");
        tryToChange(n, arr, sb);
        System.out.println("  int after call      : " + n + "  (unchanged - copy of the value)");
        System.out.println("  array after call    : " + Arrays.toString(arr) + "  (contents changed - shared object)");
        System.out.println("  builder after call  : " + sb + "  (appended, but NOT replaced by \"new\")");

        System.out.println("\n=== RECURSION ===");
        for (int i : new int[]{0, 5, 10, 20}) System.out.println("  " + i + "! = " + factorial(i));

        System.out.println("\n=== JAVA KEYWORDS YOU CAN'T USE AS NAMES (sample) ===");
        System.out.println("  class, public, static, void, int, if, else, for, while, return, new, this, final, var(contextual)");
    }
}
