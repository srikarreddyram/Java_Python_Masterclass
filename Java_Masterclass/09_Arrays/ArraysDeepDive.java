/*
 * ============================================================================
 *  SECTION 09 : ARRAYS - declaring, initialising, the java.util.Arrays class,
 *               references, 2D / jagged arrays, varargs, common algorithms
 * ============================================================================
 *
 *  ARRAY BASICS
 *  ------------
 *   int[] scores = new int[5];          // 5 slots, all default 0
 *   int[] primes = {2, 3, 5, 7, 11};    // array initializer
 *   scores.length                       // a FIELD, not a method (String uses length())
 *   Indexes run 0 .. length-1; anything else -> ArrayIndexOutOfBoundsException.
 *   Size is FIXED at creation. Need to grow? Copy (Arrays.copyOf) or use ArrayList.
 *
 *  ARRAYS ARE OBJECTS (reference types)
 *   int[] a = {1,2}; int[] b = a;   -> b and a point to the SAME array.
 *   a == b compares references; Arrays.equals(a, b) compares contents.
 *
 *  java.util.Arrays HELPERS
 *   toString, deepToString, sort, binarySearch (sorted only!), fill, copyOf,
 *   copyOfRange, equals, asList, stream
 *
 *  2D ARRAYS are "arrays of arrays":  int[][] m = new int[3][4];
 *   Rows may have different lengths (jagged): int[][] j = {{1}, {2,3}, {4,5,6}};
 *
 *  HOW TO RUN:  javac ArraysDeepDive.java && java ArraysDeepDive
 * ============================================================================
 */
import java.util.Arrays;
import java.util.Random;

public class ArraysDeepDive {

    public static void main(String[] args) {
        basics();
        referencesAndCopies();
        arraysClass();
        twoDimensional();
        algorithms();
        varargsDemo("north", "south", "east");
    }

    static void basics() {
        System.out.println("=== DECLARE, INITIALISE, ACCESS ===");
        int[] marks = new int[5];                    // defaults: 0
        String[] names = new String[3];              // defaults: null
        double[] prices = {49.5, 120.0, 15.25};      // initializer
        marks[0] = 91;
        marks[marks.length - 1] = 77;                // last element
        System.out.println("  marks  = " + Arrays.toString(marks));
        System.out.println("  names  = " + Arrays.toString(names) + "  (reference defaults are null)");
        System.out.println("  prices = " + Arrays.toString(prices) + ", length = " + prices.length);
        try {
            marks[5] = 10;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("  marks[5] -> " + e.getMessage());
        }
        // printing the array directly shows type@hash, not contents:
        System.out.println("  println(marks) prints " + marks.getClass().getSimpleName() + "@... so use Arrays.toString()");
    }

    static void referencesAndCopies() {
        System.out.println("\n=== REFERENCES vs COPIES ===");
        int[] original = {1, 2, 3};
        int[] sameArray = original;                  // copy of the REFERENCE
        int[] realCopy = Arrays.copyOf(original, original.length);
        sameArray[0] = 100;
        System.out.println("  original after sameArray[0]=100 : " + Arrays.toString(original));
        System.out.println("  independent copy                : " + Arrays.toString(realCopy));
        System.out.println("  original == realCopy: " + (original == realCopy) + ", Arrays.equals: " + Arrays.equals(new int[]{1, 2, 3}, realCopy));
        int[] bigger = Arrays.copyOf(original, 6);   // "resizing" = allocate + copy
        System.out.println("  copyOf to length 6 (grow): " + Arrays.toString(bigger));
    }

    static void arraysClass() {
        System.out.println("\n=== java.util.Arrays ===");
        int[] data = new Random(7).ints(10, 1, 100).toArray();   // 10 random ints 1..99
        System.out.println("  random      : " + Arrays.toString(data));
        Arrays.sort(data);                                       // dual-pivot quicksort
        System.out.println("  sorted      : " + Arrays.toString(data));
        int key = data[6];
        System.out.println("  binarySearch(" + key + ") -> index " + Arrays.binarySearch(data, key));
        System.out.println("  binarySearch(1000) -> " + Arrays.binarySearch(data, 1000) + "  (negative = -(insertionPoint) - 1)");
        int[] filled = new int[5];
        Arrays.fill(filled, -1);
        System.out.println("  fill(-1)    : " + Arrays.toString(filled));
        System.out.println("  copyOfRange(2,5): " + Arrays.toString(Arrays.copyOfRange(data, 2, 5)));
        String[] cities = {"Pune", "Chennai", "Goa", "Agra"};
        Arrays.sort(cities);                                     // natural (alphabetical) order
        System.out.println("  sorted strings: " + Arrays.toString(cities));
        Arrays.sort(cities, (x, y) -> x.length() - y.length());  // custom order (lambda, Section 14)
        System.out.println("  by length     : " + Arrays.toString(cities));
        System.out.println("  sum via stream: " + Arrays.stream(data).sum() + ", max: " + Arrays.stream(data).max().getAsInt());
    }

    static void twoDimensional() {
        System.out.println("\n=== 2D AND JAGGED ARRAYS ===");
        int[][] matrix = new int[3][4];
        for (int r = 0; r < matrix.length; r++)
            for (int c = 0; c < matrix[r].length; c++)
                matrix[r][c] = r * 4 + c;
        System.out.println("  3x4 matrix   : " + Arrays.deepToString(matrix));
        int[][] transposed = new int[4][3];
        for (int r = 0; r < 3; r++) for (int c = 0; c < 4; c++) transposed[c][r] = matrix[r][c];
        System.out.println("  transposed   : " + Arrays.deepToString(transposed));
        int[][] pascal = new int[6][];                           // jagged: rows created separately
        for (int r = 0; r < pascal.length; r++) {
            pascal[r] = new int[r + 1];
            pascal[r][0] = pascal[r][r] = 1;
            for (int c = 1; c < r; c++) pascal[r][c] = pascal[r - 1][c - 1] + pascal[r - 1][c];
        }
        System.out.println("  Pascal (jagged):");
        for (int[] row : pascal) System.out.println("    " + Arrays.toString(row));
    }

    static void algorithms() {
        System.out.println("\n=== CLASSIC ARRAY ALGORITHMS ===");
        int[] a = {5, 3, 9, 1, 7};
        int[] reversed = a.clone();
        for (int i = 0, j = reversed.length - 1; i < j; i++, j--) {   // two-pointer reverse
            int t = reversed[i]; reversed[i] = reversed[j]; reversed[j] = t;
        }
        System.out.println("  reverse " + Arrays.toString(a) + " -> " + Arrays.toString(reversed));
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
        for (int v : a) { min = Math.min(min, v); max = Math.max(max, v); }
        System.out.println("  min " + min + ", max " + max);
        int[] rotated = new int[a.length];
        for (int i = 0; i < a.length; i++) rotated[(i + 2) % a.length] = a[i];   // rotate right by 2
        System.out.println("  rotate right by 2 -> " + Arrays.toString(rotated));
        int[] prefix = new int[a.length];
        for (int i = 0; i < a.length; i++) prefix[i] = a[i] + (i > 0 ? prefix[i - 1] : 0);
        System.out.println("  prefix sums -> " + Arrays.toString(prefix) + "  (sum of a[1..3] = " + (prefix[3] - prefix[0]) + ")");
    }

    static void varargsDemo(String... directions) {             // String... is really String[]
        System.out.println("\n=== VARARGS ARE ARRAYS ===");
        System.out.println("  received " + directions.length + " args: " + Arrays.toString(directions));
        System.out.println("  main(String[] args) is the same idea - command-line args arrive as an array");
    }
}
