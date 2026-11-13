/*
 * ============================================================================
 *  SECTION 03 : REAL-WORLD APP - Loan EMI Calculator with amortisation table
 * ============================================================================
 *
 *  Banks compute the Equated Monthly Instalment (EMI) with:
 *
 *        EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
 *
 *    P = principal, r = monthly interest rate (annual% / 12 / 100), n = months
 *
 *  Each month the interest is charged on the OUTSTANDING balance, and the rest
 *  of the EMI reduces the principal. Early EMIs are mostly interest; late ones
 *  are mostly principal.
 *
 *  CONCEPTS USED
 *   - double arithmetic and Math.pow
 *   - int vs long for money in paise (avoid float rounding drift)
 *   - casting, compound assignment, the ternary operator
 *   - printf formatting (%,.2f -> 1,23,456.78 style grouping)
 *   - BigDecimal for exact rounding of the final figure
 *
 *  HOW TO RUN:  javac LoanEmiCalculator.java && java LoanEmiCalculator
 * ============================================================================
 */
import java.math.BigDecimal;
import java.math.RoundingMode;

public class LoanEmiCalculator {

    static double emi(double principal, double annualRatePercent, int months) {
        double r = annualRatePercent / 12 / 100;        // monthly rate as a fraction
        if (r == 0) return principal / months;           // 0% loan edge case
        double growth = Math.pow(1 + r, months);         // (1 + r)^n
        return principal * r * growth / (growth - 1);
    }

    static void schedule(String title, double principal, double ratePercent, int months, int showRows) {
        double emi = emi(principal, ratePercent, months);
        BigDecimal emiRounded = BigDecimal.valueOf(emi).setScale(2, RoundingMode.HALF_UP);
        System.out.println("=== " + title + " ===");
        System.out.printf("  Principal Rs %,.2f | Rate %.2f%% p.a. | Tenure %d months (%d yrs)%n",
                principal, ratePercent, months, months / 12);
        System.out.println("  EMI = Rs " + emiRounded);

        double balance = principal, totalInterest = 0;
        System.out.println("  Month |        EMI |  Interest | Principal |       Balance");
        for (int m = 1; m <= months; m++) {
            double interest = balance * ratePercent / 12 / 100;
            double principalPart = emi - interest;
            balance -= principalPart;                        // compound assignment
            totalInterest += interest;
            boolean show = m <= showRows || m > months - 2;  // first few + last two rows
            if (show) {
                System.out.printf("  %5d | %,10.2f | %,9.2f | %,9.2f | %,13.2f%n",
                        m, emi, interest, principalPart, Math.max(balance, 0));
            } else if (m == showRows + 1) {
                System.out.println("    ... |        ... |       ... |       ... |           ...");
            }
        }
        double totalPaid = emi * months;
        System.out.printf("  Total paid Rs %,.2f  (interest Rs %,.2f = %.1f%% of the loan)%n%n",
                totalPaid, totalInterest, totalInterest / principal * 100);
    }

    public static void main(String[] args) {
        schedule("Home loan", 50_00_000, 8.5, 20 * 12, 3);
        schedule("Car loan", 8_00_000, 9.25, 5 * 12, 3);
        schedule("Education loan", 15_00_000, 10.0, 7 * 12, 2);

        // Compare tenures: shorter tenure = higher EMI but far less interest
        System.out.println("=== Same Rs 50 lakh at 8.5%: tenure vs total interest ===");
        for (int years : new int[]{10, 15, 20, 25, 30}) {
            double e = emi(50_00_000, 8.5, years * 12);
            double interest = e * years * 12 - 50_00_000;
            String verdict = interest > 50_00_000 ? "interest > principal!" : "";
            System.out.printf("  %2d yrs: EMI Rs %,10.2f  total interest Rs %,13.2f  %s%n", years, e, interest, verdict);
        }
    }
}
