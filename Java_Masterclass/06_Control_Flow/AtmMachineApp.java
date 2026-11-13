/*
 * ============================================================================
 *  SECTION 06 : REAL-WORLD APP - ATM Machine (menu-driven console program)
 * ============================================================================
 *
 *  A classic menu loop:
 *      authenticate (max 3 PIN attempts)
 *      do {
 *          show menu -> read choice -> switch(choice) -> perform action
 *      } while (choice != EXIT);
 *
 *  Business rules
 *   - Withdrawals must be multiples of Rs 100, max Rs 20,000 per transaction,
 *     Rs 40,000 per day, and cannot exceed the balance.
 *   - Cash is dispensed using the fewest notes (Rs 500, 200, 100).
 *   - Card is blocked after 3 wrong PINs.
 *
 *  CONCEPTS USED: do-while menu, switch expression/statement, while + retry,
 *  break/continue, Scanner, try/catch on NumberFormatException, for loops.
 *
 *  The program reads from a SCRIPT so it runs unattended. Run it interactively by
 *  passing "live":   java AtmMachineApp live
 *
 *  HOW TO RUN:  javac AtmMachineApp.java && java AtmMachineApp
 * ============================================================================
 */
import java.util.Scanner;

public class AtmMachineApp {

    static final int CORRECT_PIN = 4321, PER_TXN_LIMIT = 20_000, DAILY_LIMIT = 40_000;
    static int balance = 52_750, withdrawnToday = 0;
    static final StringBuilder statement = new StringBuilder();
    static Scanner in;
    static boolean echo;   // echo scripted input so the transcript reads naturally

    public static void main(String[] args) {
        boolean live = args.length > 0 && args[0].equals("live");
        String script = String.join("\n",
                "1111", "4321",        // wrong PIN, then correct
                "1",                   // balance
                "2", "2500",           // withdraw 2500
                "2", "1250",           // not a multiple of 100
                "2", "25000",          // above per-transaction limit
                "3", "abc", "5000",    // deposit: bad input then 5000
                "2", "18000", "2", "20000",   // 2,500 + 18,000 + 20,000 > 40,000 daily limit
                "4",                   // mini statement
                "9", "5");             // invalid option, then exit
        in = live ? new Scanner(System.in) : new Scanner(script);
        echo = !live;

        System.out.println("===== WELCOME TO DSA BANK ATM =====");
        if (!authenticate()) {
            System.out.println("Card BLOCKED after 3 wrong PINs. Please contact your branch.");
            return;
        }

        int choice;
        do {
            System.out.println("\n[1] Balance  [2] Withdraw  [3] Deposit  [4] Mini statement  [5] Exit");
            choice = readInt("Choose option");
            switch (choice) {
                case 1 -> System.out.printf("  Available balance: Rs %,d%n", balance);
                case 2 -> withdraw(readInt("Amount to withdraw"));
                case 3 -> deposit(readInt("Amount to deposit"));
                case 4 -> System.out.print(statement.length() == 0 ? "  (no transactions)\n" : statement);
                case 5 -> System.out.println("  Thank you for banking with us. Please collect your card.");
                default -> System.out.println("  Invalid option " + choice + ", try again.");
            }
        } while (choice != 5);
    }

    static boolean authenticate() {
        for (int attempt = 1; attempt <= 3; attempt++) {
            int pin = readInt("Enter PIN (attempt " + attempt + "/3)");
            if (pin == CORRECT_PIN) {
                System.out.println("  PIN verified.");
                return true;
            }
            System.out.println("  Wrong PIN.");
        }
        return false;
    }

    static void withdraw(int amount) {
        // Guard clauses: each rule rejects early, the happy path stays unindented.
        if (amount <= 0) { System.out.println("  Amount must be positive."); return; }
        if (amount % 100 != 0) { System.out.println("  Please enter a multiple of Rs 100."); return; }
        if (amount > PER_TXN_LIMIT) { System.out.printf("  Max Rs %,d per transaction.%n", PER_TXN_LIMIT); return; }
        if (withdrawnToday + amount > DAILY_LIMIT) {
            System.out.printf("  Daily limit reached: you can withdraw Rs %,d more today.%n", DAILY_LIMIT - withdrawnToday);
            return;
        }
        if (amount > balance) { System.out.println("  Insufficient funds."); return; }

        balance -= amount;
        withdrawnToday += amount;
        System.out.println("  Dispensing: " + notesFor(amount));
        System.out.printf("  Please collect Rs %,d. Balance Rs %,d%n", amount, balance);
        statement.append(String.format("  WITHDRAW  -Rs %,7d   balance Rs %,9d%n", amount, balance));
    }

    static void deposit(int amount) {
        if (amount <= 0 || amount % 100 != 0) { System.out.println("  Deposit a positive multiple of Rs 100."); return; }
        balance += amount;
        System.out.printf("  Rs %,d deposited. Balance Rs %,d%n", amount, balance);
        statement.append(String.format("  DEPOSIT   +Rs %,7d   balance Rs %,9d%n", amount, balance));
    }

    /** Greedy note breakdown: largest notes first. */
    static String notesFor(int amount) {
        StringBuilder sb = new StringBuilder();
        for (int note : new int[]{500, 200, 100}) {
            int count = amount / note;
            if (count > 0) {
                sb.append(count).append(" x Rs ").append(note).append("  ");
                amount %= note;
            }
        }
        return sb.toString().trim();
    }

    /** Keeps asking until the user types a valid integer. */
    static int readInt(String prompt) {
        while (true) {
            System.out.print("  " + prompt + ": ");
            String line = in.nextLine().trim();
            if (echo) System.out.println(prompt.startsWith("Enter PIN") ? "****" : line);
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("  '" + line + "' is not a number.");
            }
        }
    }
}
