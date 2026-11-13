/*
 * ============================================================================
 *  SECTIONS 04-05 : REAL-WORLD APP - Electricity Bill Generator
 * ============================================================================
 *
 *  Utility companies bill by SLABS: the first block of units is cheap, later
 *  blocks cost more. Each method below does ONE job, which is the main lesson
 *  of this section: break a problem into small, named, testable methods.
 *
 *  Tariff used here (illustrative, not any specific state's tariff):
 *    Domestic:   0-100 units Rs 0 (subsidised) | 101-200 Rs 2.25 | 201-400 Rs 4.50
 *                401-500 Rs 6.00 | 500+ Rs 8.00
 *    Commercial: flat Rs 8.50 per unit
 *    Fixed charge: Rs 50 (domestic), Rs 150 (commercial); electricity duty 5% on energy charge
 *
 *  CONCEPTS USED
 *   - methods with parameters/return values, early return for validation
 *   - OVERLOADING: generateBill(units) vs generateBill(units, commercial)
 *     vs generateBill(previousReading, currentReading, commercial)
 *   - if / else-if chains, Math.min/Math.max, static constants
 *
 *  HOW TO RUN:  javac ElectricityBillApp.java && java ElectricityBillApp
 * ============================================================================
 */
public class ElectricityBillApp {

    static final double FIXED_DOMESTIC = 50, FIXED_COMMERCIAL = 150, DUTY_RATE = 0.05;

    /** Units falling inside the slab [from, to] when total usage is 'units'. */
    static int unitsInSlab(int units, int from, int to) {
        if (units < from) return 0;
        return Math.min(units, to) - from + 1;
    }

    static double domesticEnergyCharge(int units) {
        double charge = 0;
        charge += unitsInSlab(units, 101, 200) * 2.25;
        charge += unitsInSlab(units, 201, 400) * 4.50;
        charge += unitsInSlab(units, 401, 500) * 6.00;
        charge += unitsInSlab(units, 501, Integer.MAX_VALUE) * 8.00;
        return charge;                       // first 100 units are free (subsidised)
    }

    static double commercialEnergyCharge(int units) { return units * 8.50; }

    // ---------------- OVERLOADED entry points ----------------
    static double generateBill(int units) {                     // domestic by default
        return generateBill(units, false);
    }

    static double generateBill(int units, boolean commercial) {
        if (units < 0) {
            System.out.println("  ERROR: units cannot be negative");
            return 0;
        }
        double energy = commercial ? commercialEnergyCharge(units) : domesticEnergyCharge(units);
        double fixed = commercial ? FIXED_COMMERCIAL : FIXED_DOMESTIC;
        double duty = energy * DUTY_RATE;
        double total = energy + fixed + duty;
        printBill(units, commercial, energy, fixed, duty, total);
        return total;
    }

    static double generateBill(int previousReading, int currentReading, boolean commercial) {
        if (currentReading < previousReading) {
            System.out.println("  ERROR: meter reading went backwards (" + previousReading + " -> " + currentReading + ")");
            return 0;
        }
        System.out.println("  Meter: " + previousReading + " -> " + currentReading);
        return generateBill(currentReading - previousReading, commercial);
    }

    static void printBill(int units, boolean commercial, double energy, double fixed, double duty, double total) {
        System.out.printf("  %-10s %4d units | energy Rs %8.2f + fixed Rs %6.2f + duty Rs %6.2f = Rs %9.2f%n",
                commercial ? "COMMERCIAL" : "DOMESTIC", units, energy, fixed, duty, total);
    }

    static String usageBand(int units) {
        if (units <= 100) return "LOW (fully subsidised)";
        else if (units <= 300) return "MODERATE";
        else if (units <= 500) return "HIGH";
        return "VERY HIGH - consider a solar rooftop";
    }

    public static void main(String[] args) {
        System.out.println("=== MONTHLY BILLS ===");
        for (int units : new int[]{80, 150, 320, 480, 750}) {
            double bill = generateBill(units);
            System.out.printf("             usage band: %s, average Rs %.2f/unit%n", usageBand(units), units == 0 ? 0 : bill / units);
        }

        System.out.println("\n=== SAME USAGE, DIFFERENT CONNECTION TYPE ===");
        generateBill(320, false);
        generateBill(320, true);

        System.out.println("\n=== FROM METER READINGS ===");
        generateBill(10_450, 10_712, false);
        generateBill(5_000, 4_900, false);      // validation catches the bad reading
        generateBill(-5);

        System.out.println("\n=== WHAT IF? Saving 50 units a month ===");
        double before = generateBill(430), after = generateBill(380);
        System.out.printf("  Monthly saving Rs %.2f, yearly Rs %.2f%n", before - after, (before - after) * 12);
    }
}
