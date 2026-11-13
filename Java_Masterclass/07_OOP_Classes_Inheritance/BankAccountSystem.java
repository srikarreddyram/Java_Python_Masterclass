/*
 * ============================================================================
 *  SECTION 07 : REAL-WORLD APP - Bank Account System
 * ============================================================================
 *
 *  Class design
 *
 *                       BankAccount  (base class)
 *                 - accountNumber, holder, balance
 *                 - deposit(), withdraw(), monthEnd()
 *                     /                     \
 *          SavingsAccount              CurrentAccount
 *       + interest rate              + overdraft limit
 *       + min balance rule           + monthly fee
 *       overrides canWithdraw()      overrides canWithdraw()
 *       overrides monthEnd()         overrides monthEnd()
 *
 *   Customer  (a record: immutable customer details)
 *   Transaction (a record: one line of the passbook)
 *
 *  CONCEPTS USED
 *   - constructors + constructor chaining, this/super
 *   - static counter that generates account numbers
 *   - encapsulated state: balance is private, changed only via methods
 *   - overriding hooks (canWithdraw, monthEnd) - the parent calls them
 *   - records, toString overriding, StringBuilder for the statement
 *
 *  HOW TO RUN:  javac BankAccountSystem.java && java BankAccountSystem
 * ============================================================================
 */
import java.util.ArrayList;
import java.util.List;

public class BankAccountSystem {

    record Customer(String name, String email, String phone) { }

    record Transaction(String type, double amount, double balanceAfter, String note) {
        @Override
        public String toString() {
            return String.format("    %-9s %12s  bal %,12.2f  %s", type, (amount >= 0 ? "+" : "") + String.format("%,.2f", amount), balanceAfter, note);
        }
    }

    // ================================ base class ================================
    static class BankAccount {
        private static int nextNumber = 1001;          // shared across every account

        private final String accountNumber;
        private final Customer holder;
        private double balance;
        private final List<Transaction> history = new ArrayList<>();

        BankAccount(Customer holder, double openingDeposit) {
            this.accountNumber = prefix() + (nextNumber++);
            this.holder = holder;
            record("OPEN", openingDeposit, "opening deposit");
        }

        // Subclasses override this. CAUTION: it is called from the constructor, before the
        // subclass's own fields are set. That's safe here only because the overrides return
        // constants. Calling overridable methods from constructors is a known trap.
        String prefix() { return "AC"; }

        double getBalance() { return balance; }
        String getAccountNumber() { return accountNumber; }
        Customer getHolder() { return holder; }

        void deposit(double amount) {
            if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
            record("DEPOSIT", amount, "");
        }

        /** Template: the parent controls the flow, subclasses decide the rule. */
        boolean withdraw(double amount, String note) {
            if (amount <= 0 || !canWithdraw(amount)) {
                System.out.printf("  [%s] DECLINED withdrawal of Rs %,.2f (%s)%n", accountNumber, amount, declineReason(amount));
                return false;
            }
            record("WITHDRAW", -amount, note);
            return true;
        }

        boolean canWithdraw(double amount) { return amount <= balance; }
        String declineReason(double amount) { return "insufficient funds"; }

        void monthEnd() { }                            // hook: nothing by default

        /** Only this class touches 'balance' directly. */
        protected void record(String type, double signedAmount, String note) {
            balance += signedAmount;
            history.add(new Transaction(type, signedAmount, balance, note));
        }

        String statement() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("  %s  %s  (%s)%n", accountNumber, getClass().getSimpleName(), holder.name()));
            for (Transaction t : history) sb.append(t).append('\n');
            return sb.toString();
        }

        @Override
        public String toString() {
            return String.format("%s %-15s %-8s Rs %,12.2f", accountNumber, getClass().getSimpleName(), holder.name(), balance);
        }
    }

    // ================================ subclasses ================================
    static class SavingsAccount extends BankAccount {
        static final double MIN_BALANCE = 1_000;
        private final double annualRatePercent;

        SavingsAccount(Customer holder, double opening, double annualRatePercent) {
            super(holder, opening);                    // MUST be first
            this.annualRatePercent = annualRatePercent;
        }

        SavingsAccount(Customer holder, double opening) {
            this(holder, opening, 3.5);                // default rate via chaining
        }

        @Override String prefix() { return "SB"; }

        @Override
        boolean canWithdraw(double amount) { return getBalance() - amount >= MIN_BALANCE; }

        @Override
        String declineReason(double amount) { return "would go below minimum balance Rs " + (int) MIN_BALANCE; }

        @Override
        void monthEnd() {
            double interest = getBalance() * annualRatePercent / 12 / 100;
            record("INTEREST", interest, annualRatePercent + "% p.a.");
        }
    }

    static class CurrentAccount extends BankAccount {
        private final double overdraftLimit;
        static final double MONTHLY_FEE = 250;

        CurrentAccount(Customer holder, double opening, double overdraftLimit) {
            super(holder, opening);
            this.overdraftLimit = overdraftLimit;
        }

        @Override String prefix() { return "CA"; }

        @Override
        boolean canWithdraw(double amount) { return getBalance() - amount >= -overdraftLimit; }

        @Override
        String declineReason(double amount) { return "exceeds overdraft limit Rs " + (int) overdraftLimit; }

        @Override
        void monthEnd() {
            record("FEE", -MONTHLY_FEE, "account maintenance");
            if (getBalance() < 0) {                    // overdraft interest at 15% p.a.
                record("OD-INT", getBalance() * 0.15 / 12, "overdraft interest");
            }
        }
    }

    /** Transfer works on ANY BankAccount - it doesn't care which subclass. */
    static boolean transfer(BankAccount from, BankAccount to, double amount) {
        if (from.withdraw(amount, "transfer to " + to.getAccountNumber())) {
            to.record("TRANSFER", amount, "from " + from.getAccountNumber());
            System.out.printf("  Transferred Rs %,.2f %s -> %s%n", amount, from.getAccountNumber(), to.getAccountNumber());
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        Customer srikar = new Customer("Srikar", "srikar@example.com", "98xxxxxx01");
        Customer meera = new Customer("Meera", "meera@example.com", "98xxxxxx02");

        SavingsAccount s1 = new SavingsAccount(srikar, 25_000);
        SavingsAccount s2 = new SavingsAccount(meera, 1_50_000, 4.0);
        CurrentAccount c1 = new CurrentAccount(meera, 40_000, 50_000);
        List<BankAccount> accounts = List.of(s1, s2, c1);

        System.out.println("=== ACCOUNTS OPENED ===");
        accounts.forEach(a -> System.out.println("  " + a));

        System.out.println("\n=== DAILY ACTIVITY ===");
        s1.deposit(12_000);
        s1.withdraw(35_500, "laptop");              // leaves exactly 1,500 -> allowed
        s1.withdraw(1_000, "dinner");               // would leave 500 -> declined
        c1.withdraw(75_000, "supplier payment");    // goes into overdraft -35,000
        c1.withdraw(20_000, "rent");                // -55,000 > limit -> declined
        transfer(s2, s1, 10_000);
        transfer(s1, c1, 50_000);                   // declined

        System.out.println("\n=== MONTH END (each subclass runs its own rule) ===");
        for (BankAccount a : accounts) a.monthEnd();
        accounts.forEach(a -> System.out.println("  " + a));

        System.out.println("\n=== PASSBOOK: " + s1.getAccountNumber() + " ===");
        System.out.print(s1.statement());
        System.out.println("=== PASSBOOK: " + c1.getAccountNumber() + " ===");
        System.out.print(c1.statement());

        double totalDeposits = 0;
        for (BankAccount a : accounts) totalDeposits += a.getBalance();
        System.out.printf("%nBank's net balance across accounts: Rs %,.2f%n", totalDeposits);
    }
}
