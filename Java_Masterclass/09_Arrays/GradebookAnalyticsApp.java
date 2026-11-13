/*
 * ============================================================================
 *  SECTION 09 : REAL-WORLD APP - Class Gradebook Analytics
 * ============================================================================
 *
 *  A teacher's marks register is naturally a 2D array:
 *
 *                   DSA   Java   DBMS   OS   Maths
 *      Srikar    [  88,    94,    76,   81,   90 ]
 *      Himesh    [  72,    65,    80,   58,   70 ]
 *      ...
 *
 *  The app computes per-student totals/percentages/grades, per-subject
 *  averages/toppers/pass rates, overall rank list, a grade histogram, and
 *  flags students who failed any subject - all with plain arrays and loops.
 *
 *  CONCEPTS USED
 *   - parallel arrays (names[] lines up with marks[][] rows)
 *   - 2D traversal row-wise (per student) and column-wise (per subject)
 *   - sorting an index array to rank without disturbing the original data
 *   - Arrays.toString, Arrays.fill, counting into an int[] (histogram)
 *
 *  HOW TO RUN:  javac GradebookAnalyticsApp.java && java GradebookAnalyticsApp
 * ============================================================================
 */
import java.util.Arrays;

public class GradebookAnalyticsApp {

    static final String[] SUBJECTS = {"DSA", "Java", "DBMS", "OS", "Maths"};
    static final String[] STUDENTS = {"Srikar", "Himesh", "Krrish", "Sathvik", "Praveen", "Ananya", "Meera"};
    static final int[][] MARKS = {
            {88, 94, 76, 81, 90},
            {72, 65, 80, 58, 70},
            {91, 85, 88, 92, 79},
            {45, 38, 62, 55, 49},   // failed Java
            {67, 74, 59, 61, 83},
            {95, 89, 93, 87, 98},
            {58, 71, 35, 66, 60}};  // failed DBMS
    static final int PASS_MARK = 40;
    static final char[] GRADES = {'O', 'A', 'B', 'C', 'D', 'F'};

    static char grade(double percent) {
        if (percent >= 90) return 'O';
        if (percent >= 80) return 'A';
        if (percent >= 70) return 'B';
        if (percent >= 60) return 'C';
        if (percent >= 50) return 'D';
        return 'F';
    }

    public static void main(String[] args) {
        int n = STUDENTS.length, m = SUBJECTS.length;
        int[] totals = new int[n];
        double[] percents = new double[n];

        // ---------------- row-wise: per student ----------------
        System.out.println("=== STUDENT REPORT ===");
        System.out.printf("  %-8s %s  Total   %%    Grade%n", "Name", Arrays.toString(SUBJECTS));
        for (int s = 0; s < n; s++) {
            for (int sub = 0; sub < m; sub++) totals[s] += MARKS[s][sub];
            percents[s] = totals[s] * 100.0 / (m * 100);
            System.out.printf("  %-8s %-27s %4d  %5.1f   %c%n", STUDENTS[s], Arrays.toString(MARKS[s]), totals[s], percents[s], grade(percents[s]));
        }

        // ---------------- column-wise: per subject ----------------
        System.out.println("\n=== SUBJECT ANALYTICS ===");
        for (int sub = 0; sub < m; sub++) {
            int sum = 0, best = 0, passed = 0;
            for (int s = 0; s < n; s++) {
                sum += MARKS[s][sub];
                if (MARKS[s][sub] > MARKS[best][sub]) best = s;
                if (MARKS[s][sub] >= PASS_MARK) passed++;
            }
            System.out.printf("  %-6s avg %5.1f | topper %-7s (%d) | pass rate %3.0f%%%n",
                    SUBJECTS[sub], sum / (double) n, STUDENTS[best], MARKS[best][sub], passed * 100.0 / n);
        }

        // ---------------- ranking via an index array ----------------
        System.out.println("\n=== RANK LIST ===");
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++) order[i] = i;
        Arrays.sort(order, (a, b) -> totals[b] - totals[a]);       // sort indexes by total, descending
        for (int r = 0; r < n; r++) {
            int s = order[r];
            System.out.printf("  #%d %-8s %d (%.1f%%)%n", r + 1, STUDENTS[s], totals[s], percents[s]);
        }

        // ---------------- histogram of grades ----------------
        System.out.println("\n=== GRADE DISTRIBUTION ===");
        int[] counts = new int[GRADES.length];
        for (double p : percents) counts[new String(GRADES).indexOf(grade(p))]++;
        for (int g = 0; g < GRADES.length; g++) {
            char[] bar = new char[counts[g] * 3];
            Arrays.fill(bar, '#');
            System.out.printf("  %c | %-12s %d%n", GRADES[g], new String(bar), counts[g]);
        }

        // ---------------- alerts ----------------
        System.out.println("\n=== SUPPLEMENTARY EXAM ALERTS (any subject < " + PASS_MARK + ") ===");
        boolean anyone = false;
        for (int s = 0; s < n; s++) {
            StringBuilder failed = new StringBuilder();
            for (int sub = 0; sub < m; sub++) if (MARKS[s][sub] < PASS_MARK) failed.append(SUBJECTS[sub]).append(' ');
            if (failed.length() > 0) {
                anyone = true;
                System.out.println("  " + STUDENTS[s] + " -> re-exam in: " + failed.toString().trim());
            }
        }
        if (!anyone) System.out.println("  none");

        double classAvg = Arrays.stream(percents).average().orElse(0);
        System.out.printf("%nClass average %.1f%% | above average: %d of %d students%n",
                classAvg, Arrays.stream(percents).filter(p -> p > classAvg).count(), n);
    }
}
