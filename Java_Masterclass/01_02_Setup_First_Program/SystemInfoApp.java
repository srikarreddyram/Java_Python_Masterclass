/*
 * ============================================================================
 *  SECTIONS 01-02 : REAL-WORLD APP - "Environment Doctor"
 * ============================================================================
 *
 *  Every team has a script that checks "is my machine set up correctly?"
 *  before a build. This app inspects the running JVM and prints a health
 *  report: Java version, vendor, OS, CPU cores, memory, and whether the JDK
 *  meets the course's minimum version (Java 17).
 *
 *  CONCEPTS USED
 *   - System.getProperty(...)       : read JVM/OS properties
 *   - System.getenv(...)            : read environment variables (JAVA_HOME, PATH)
 *   - Runtime.getRuntime()          : CPU cores and heap memory
 *   - Runtime.version().feature()   : the major Java version as an int
 *   - System.nanoTime()             : measure elapsed time
 *
 *  HOW TO RUN:  javac SystemInfoApp.java && java SystemInfoApp
 * ============================================================================
 */
public class SystemInfoApp {

    static final int REQUIRED_JAVA = 17;

    public static void main(String[] args) {
        long start = System.nanoTime();
        System.out.println("=== ENVIRONMENT DOCTOR ===");

        int feature = Runtime.version().feature();          // e.g. 17, 21
        row("Java version", System.getProperty("java.version"));
        row("Vendor", System.getProperty("java.vendor"));
        row("JVM", System.getProperty("java.vm.name"));
        row("OS", System.getProperty("os.name") + " " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")");
        row("User", System.getProperty("user.name"));
        row("Working dir", System.getProperty("user.dir"));

        Runtime rt = Runtime.getRuntime();
        row("CPU cores", String.valueOf(rt.availableProcessors()));
        row("Max heap", mb(rt.maxMemory()));
        row("Used heap", mb(rt.totalMemory() - rt.freeMemory()));

        String javaHome = System.getenv("JAVA_HOME");
        row("JAVA_HOME", javaHome == null ? "(not set - fine if java is on PATH)" : javaHome);

        System.out.println();
        check("Java " + REQUIRED_JAVA + "+ installed", feature >= REQUIRED_JAVA);
        check("Text blocks / records / sealed classes available", feature >= 17);
        check("At least 2 CPU cores", rt.availableProcessors() >= 2);
        check("At least 256 MB max heap", rt.maxMemory() >= 256L * 1024 * 1024);

        double ms = (System.nanoTime() - start) / 1_000_000.0;
        System.out.printf("%nReport generated in %.2f ms%n", ms);
    }

    static void row(String key, String value) {
        System.out.printf("  %-13s : %s%n", key, value);
    }

    static void check(String what, boolean ok) {
        System.out.println("  [" + (ok ? " OK " : "FAIL") + "] " + what);
    }

    static String mb(long bytes) {
        return String.format("%,d MB", bytes / (1024 * 1024));
    }
}
