/*
 * ============================================================================
 *  SECTION 14 : REAL-WORLD APP - Expense Tracker with a pluggable Rules Engine
 * ============================================================================
 *
 *  Fintech apps (CRED, Jupiter, bank apps) categorise card transactions and
 *  raise alerts using RULES that the product team keeps adding. If every rule is
 *  a lambda, new rules are one line of data - no new classes, no if-else chains.
 *
 *   Rule = record(name, Predicate<Txn> when, Function<Txn,String> message)
 *   Categoriser = list of (Predicate<Txn>, category) pairs, first match wins
 *   Reports     = Comparator chains, Collectors-free grouping via Map.merge,
 *                 BiFunction for budget checks, Supplier for default values,
 *                 Consumer chains for output
 *
 *  HOW TO RUN:  javac ExpenseRulesEngineApp.java && java ExpenseRulesEngineApp
 * ============================================================================
 */
import java.time.LocalDate;
import java.util.*;
import java.util.function.*;

public class ExpenseRulesEngineApp {

    record Txn(LocalDate date, String merchant, double amount, String city, boolean international) { }

    record Rule(String name, Predicate<Txn> when, Function<Txn, String> message) { }

    record CategoryRule(Predicate<Txn> matches, String category) { }

    // ---------------- reusable predicate "building blocks" ----------------
    static Predicate<Txn> merchantHas(String... keywords) {
        return t -> {
            String m = t.merchant().toLowerCase();
            for (String k : keywords) if (m.contains(k)) return true;
            return false;
        };
    }

    static Predicate<Txn> amountAbove(double limit) { return t -> t.amount() > limit; }

    public static void main(String[] args) {
        List<Txn> txns = List.of(
                new Txn(LocalDate.of(2026, 11, 1), "Swiggy Instamart", 642, "Chennai", false),
                new Txn(LocalDate.of(2026, 11, 2), "Uber India", 318, "Chennai", false),
                new Txn(LocalDate.of(2026, 11, 3), "Amazon Electronics", 24_999, "Online", false),
                new Txn(LocalDate.of(2026, 11, 4), "Zomato", 486, "Chennai", false),
                new Txn(LocalDate.of(2026, 11, 5), "Netflix.com", 649, "Online", true),
                new Txn(LocalDate.of(2026, 11, 6), "Indian Oil Petrol", 2_100, "Goa", false),
                new Txn(LocalDate.of(2026, 11, 7), "Zomato", 1_240, "Goa", false),
                new Txn(LocalDate.of(2026, 11, 8), "Steam Games", 3_200, "Online", true),
                new Txn(LocalDate.of(2026, 11, 8), "Steam Games", 3_200, "Online", true),   // duplicate charge
                new Txn(LocalDate.of(2026, 11, 9), "Apollo Pharmacy", 890, "Chennai", false));

        // ================== categoriser: first matching rule wins ==================
        List<CategoryRule> categoriser = List.of(
                new CategoryRule(merchantHas("swiggy", "zomato"), "Food"),
                new CategoryRule(merchantHas("uber", "ola", "petrol", "oil"), "Transport"),
                new CategoryRule(merchantHas("amazon", "flipkart"), "Shopping"),
                new CategoryRule(merchantHas("netflix", "spotify", "steam"), "Entertainment"),
                new CategoryRule(merchantHas("pharmacy", "hospital"), "Health"));
        Function<Txn, String> categorise = t -> categoriser.stream()
                .filter(r -> r.matches().test(t)).map(CategoryRule::category).findFirst().orElse("Other");

        // ================== alert rules: just data ==================
        Set<String> seen = new HashSet<>();
        List<Rule> rules = List.of(
                new Rule("BIG_SPEND", amountAbove(10_000), t -> "Large purchase Rs " + (int) t.amount() + " at " + t.merchant()),
                new Rule("FOREX", Txn::international, t -> "International txn - 3.5% forex markup applies to Rs " + (int) t.amount()),
                new Rule("NEW_CITY", t -> !t.city().equals("Chennai") && !t.city().equals("Online"), t -> "Card used in " + t.city() + " - was this you?"),
                new Rule("DUPLICATE", t -> !seen.add(t.date() + t.merchant() + t.amount()), t -> "Possible duplicate charge at " + t.merchant()),
                new Rule("LATE_NIGHT_FOOD", merchantHas("zomato").and(amountAbove(1_000)), t -> "Rs " + (int) t.amount() + " food order. Party?"));

        System.out.println("=== TRANSACTIONS + ALERTS ===");
        Map<String, Double> byCategory = new TreeMap<>();
        Map<String, Integer> alertCounts = new LinkedHashMap<>();
        for (Txn t : txns) {
            String cat = categorise.apply(t);
            byCategory.merge(cat, t.amount(), Double::sum);
            System.out.printf("  %s  %-20s Rs %,8.0f  [%s]%n", t.date(), t.merchant(), t.amount(), cat);
            for (Rule r : rules) {
                if (r.when().test(t)) {
                    System.out.println("        ! " + r.name() + ": " + r.message().apply(t));
                    alertCounts.merge(r.name(), 1, Integer::sum);
                }
            }
        }

        // ================== budgets with BiFunction + Supplier ==================
        System.out.println("\n=== BUDGET CHECK ===");
        Map<String, Double> budgets = Map.of("Food", 2_500.0, "Transport", 3_000.0, "Shopping", 15_000.0, "Entertainment", 2_000.0);
        Supplier<Double> noBudget = () -> Double.POSITIVE_INFINITY;
        BiFunction<Double, Double, String> verdict = (spent, limit) ->
                limit.isInfinite() ? "no budget set" : spent > limit ? String.format("OVER by Rs %,.0f", spent - limit) : String.format("%.0f%% used", spent / limit * 100);
        byCategory.forEach((cat, spent) ->
                System.out.printf("  %-14s Rs %,8.0f  %s%n", cat, spent, verdict.apply(spent, budgets.getOrDefault(cat, noBudget.get()))));

        // ================== reports with Comparator chains ==================
        System.out.println("\n=== TOP 3 EXPENSES (Comparator.comparingDouble().reversed()) ===");
        List<Txn> sorted = new ArrayList<>(txns);
        sorted.sort(Comparator.comparingDouble(Txn::amount).reversed().thenComparing(Txn::date));
        sorted.subList(0, 3).forEach(t -> System.out.printf("  Rs %,8.0f  %s%n", t.amount(), t.merchant()));

        System.out.println("\n=== CASHBACK SIMULATION (UnaryOperator pipeline) ===");
        UnaryOperator<Double> onlineCashback = amt -> amt * 0.05, cap = cb -> Math.min(cb, 500.0);
        Function<Double, Double> cashback = onlineCashback.andThen(cap);
        double totalCb = 0;
        for (Txn t : txns) if (t.city().equals("Online")) totalCb += cashback.apply(t.amount());
        System.out.printf("  5%% on online spends, capped at Rs 500 per txn -> Rs %,.2f%n", totalCb);

        Consumer<String> header = s -> System.out.print("\nAlert summary: ");
        Consumer<String> body = s -> System.out.println(s);
        header.andThen(body).accept(alertCounts.toString());
    }
}
