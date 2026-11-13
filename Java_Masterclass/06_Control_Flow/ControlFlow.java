/*
 * ============================================================================
 *  SECTION 06 : CONTROL FLOW - switch, for, while, do-while, break/continue,
 *               labels, local variable scope, Scanner input, parsing errors
 * ============================================================================
 *
 *  SWITCH
 *  ------
 *   Classic switch statement : case X: ... break;   (forget break = FALL-THROUGH)
 *   Enhanced switch (Java 14+): case X -> ...       (no fall-through, no break)
 *   Switch EXPRESSION         : String s = switch (d) { case 1 -> "Mon"; default -> "?"; };
 *                               use 'yield' to return a value from a { block }.
 *   Works on int-like types, char, String, enums (and patterns in Java 21).
 *
 *  LOOPS
 *  -----
 *   for (init; condition; update)   - you know how many times
 *   for (T item : collection)       - enhanced for, visit each element
 *   while (condition)               - repeat while true, may run 0 times
 *   do { } while (condition);       - runs AT LEAST once (menus, input retry)
 *   break    : leave the loop        continue : skip to the next iteration
 *   label:   : break outer;  exits a NESTED loop in one step
 *
 *  INPUT: java.util.Scanner reads tokens/lines from System.in (or any String/File).
 *  Integer.parseInt throws NumberFormatException on bad input -> try/catch.
 *
 *  HOW TO RUN:  javac ControlFlow.java && java ControlFlow
 * ============================================================================
 */
import java.util.Scanner;

public class ControlFlow {

    public static void main(String[] args) {
        switchDemos();
        loopDemos();
        breakContinueLabels();
        inputParsingDemo();
    }

    static void switchDemos() {
        System.out.println("=== SWITCH ===");
        int day = 6;
        // 1) classic switch with deliberate fall-through
        switch (day) {
            case 6:
            case 7:
                System.out.println("  classic: weekend (6 and 7 share code by falling through)");
                break;
            default:
                System.out.println("  classic: weekday");
        }
        // 2) enhanced arrow switch: no fall-through, several labels per case
        switch (day) {
            case 1, 2, 3, 4, 5 -> System.out.println("  arrow  : weekday");
            case 6, 7 -> System.out.println("  arrow  : weekend");
            default -> System.out.println("  arrow  : invalid day");
        }
        // 3) switch EXPRESSION returns a value; yield returns from a block
        for (String month : new String[]{"FEB", "APR", "DEC", "XYZ"}) {
            int days = switch (month) {
                case "APR", "JUN", "SEP", "NOV" -> 30;
                case "FEB" -> {
                    int year = 2028;
                    yield (year % 4 == 0 && year % 100 != 0) || year % 400 == 0 ? 29 : 28;
                }
                case "XYZ" -> -1;
                default -> 31;
            };
            System.out.println("  expression: " + month + " has " + days + " days");
        }
        // 4) switch on char
        char op = '*';
        double result = switch (op) {
            case '+' -> 6 + 3;
            case '-' -> 6 - 3;
            case '*' -> 6 * 3;
            case '/' -> 6 / 3.0;
            default -> throw new IllegalArgumentException("bad operator " + op);
        };
        System.out.println("  6 " + op + " 3 = " + result);
    }

    static void loopDemos() {
        System.out.println("\n=== LOOPS ===");
        // for: multiplication row
        StringBuilder row = new StringBuilder("  for     : 7 x table ->");
        for (int i = 1; i <= 5; i++) row.append(" ").append(7 * i);
        System.out.println(row);

        // enhanced for
        int total = 0;
        for (int price : new int[]{120, 80, 45}) total += price;
        System.out.println("  for-each: cart total = " + total);

        // while: how many years until savings double at 7%?
        double money = 1000;
        int years = 0;
        while (money < 2000) {
            money *= 1.07;
            years++;
        }
        System.out.printf("  while   : Rs 1000 at 7%% doubles in %d years (Rs %.2f)%n", years, money);

        // do-while: runs once even though the condition is false from the start
        int attempts = 0;
        do {
            attempts++;
        } while (attempts < 0);
        System.out.println("  do-while: body ran " + attempts + " time(s) although the condition was false");

        // digit sum with while: classic interview question
        int number = 98_765, sum = 0, tmp = number;
        while (tmp > 0) {
            sum += tmp % 10;   // last digit
            tmp /= 10;         // drop last digit
        }
        System.out.println("  digit sum of " + number + " = " + sum);
    }

    static void breakContinueLabels() {
        System.out.println("\n=== BREAK / CONTINUE / LABELS ===");
        StringBuilder odds = new StringBuilder("  continue: odd numbers ->");
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) continue;     // skip evens
            odds.append(' ').append(i);
        }
        System.out.println(odds);

        int firstPrimeOver100 = -1;
        for (int n = 101; ; n++) {        // infinite loop exited with break
            boolean prime = true;
            for (int d = 2; d * d <= n; d++) if (n % d == 0) { prime = false; break; }
            if (prime) { firstPrimeOver100 = n; break; }
        }
        System.out.println("  break   : first prime > 100 = " + firstPrimeOver100);

        int[][] grid = {{3, 8, 1}, {9, 42, 7}, {5, 2, 6}};
        String found = "not found";
        search:                            // label on the OUTER loop
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                if (grid[r][c] == 42) {
                    found = "(" + r + "," + c + ")";
                    break search;          // leaves BOTH loops
                }
            }
        }
        System.out.println("  label   : 42 found at " + found);
    }

    static void inputParsingDemo() {
        System.out.println("\n=== SCANNER + INPUT VALIDATION ===");
        // A Scanner can read any text source; here we simulate a user typing.
        Scanner in = new Scanner("abc\n-5\n200\n42\n");
        int age;
        while (true) {                     // keep asking until the input is valid
            String line = in.nextLine();
            System.out.print("  Enter age: " + line + " -> ");
            try {
                age = Integer.parseInt(line.trim());
                if (age < 0 || age > 150) {
                    System.out.println("out of range, try again");
                    continue;
                }
                System.out.println("accepted");
                break;
            } catch (NumberFormatException e) {
                System.out.println("not a number, try again");
            }
        }
        System.out.println("  Final age = " + age);
        System.out.println("  (For real keyboard input use: new Scanner(System.in))");
    }
}
