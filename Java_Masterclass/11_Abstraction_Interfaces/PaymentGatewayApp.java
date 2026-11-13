/*
 * ============================================================================
 *  SECTION 11 : REAL-WORLD APP - Payment Gateway (think Razorpay / Stripe)
 * ============================================================================
 *
 *  Design
 *
 *   interface PaymentMethod         <- the CONTRACT every method must honour
 *       authorize(amount) / capture(amount) / refund(amount) / fee(amount)
 *
 *   abstract class BaseProcessor implements PaymentMethod
 *       pay(order)  = TEMPLATE METHOD: validate -> fraud check -> authorize
 *                     -> capture -> receipt. The ORDER of steps is fixed here.
 *       authorize() / fee() left ABSTRACT for each method to define.
 *
 *     CardProcessor     UpiProcessor     WalletProcessor
 *     (+ 3-D Secure)    (+ VPA check)    (+ Rechargeable balance)
 *
 *   interface Refundable / Rechargeable  <- optional capabilities
 *
 *  Why both?  The interface lets the checkout code depend only on
 *  "something that can pay"; the abstract class removes duplicated workflow
 *  code from every concrete processor.
 *
 *  HOW TO RUN:  javac PaymentGatewayApp.java && java PaymentGatewayApp
 * ============================================================================
 */
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PaymentGatewayApp {

    record Order(String id, String customer, double amount) { }
    record Receipt(String orderId, String method, double amount, double fee, String status) {
        @Override public String toString() {
            return String.format("  %-6s %-14s Rs %,9.2f  fee Rs %6.2f  %s", orderId, method, amount, fee, status);
        }
    }

    // ============================ contracts ============================
    interface PaymentMethod {
        String name();
        boolean authorize(Order o);
        double fee(double amount);
        Receipt pay(Order o);
    }

    interface Refundable {
        double refund(Order o, double amount);
    }

    interface Rechargeable {
        void topUp(double amount);
        double balance();
    }

    // ============================ shared workflow ============================
    abstract static class BaseProcessor implements PaymentMethod {
        private static final double FRAUD_LIMIT = 2_00_000;
        protected final List<String> log = new ArrayList<>();

        /** TEMPLATE METHOD: final so subclasses can't reorder the steps. */
        @Override
        public final Receipt pay(Order o) {
            if (o.amount() <= 0) return fail(o, "invalid amount");
            if (o.amount() > FRAUD_LIMIT) return fail(o, "flagged by fraud check");
            if (!authorize(o)) return fail(o, "authorisation declined");
            double fee = Math.round(fee(o.amount()) * 100) / 100.0;
            log.add("captured " + o.id());
            return new Receipt(o.id(), name(), o.amount(), fee, "SUCCESS");
        }

        private Receipt fail(Order o, String reason) {
            log.add("failed " + o.id() + ": " + reason);
            return new Receipt(o.id(), name(), o.amount(), 0, "FAILED (" + reason + ")");
        }
    }

    // ============================ concrete processors ============================
    static class CardProcessor extends BaseProcessor implements Refundable {
        private final String last4;
        private final double creditLimit;
        private double used;
        CardProcessor(String last4, double creditLimit) { this.last4 = last4; this.creditLimit = creditLimit; }

        @Override public String name() { return "Card **" + last4; }
        @Override public boolean authorize(Order o) {
            if (o.amount() > 5_000) log.add("3-D Secure OTP verified for " + o.id());   // card-specific rule
            if (used + o.amount() > creditLimit) return false;
            used += o.amount();
            return true;
        }
        @Override public double fee(double amount) { return amount * 0.02; }   // 2% MDR
        @Override public double refund(Order o, double amount) {
            used -= amount;
            log.add("refunded Rs " + amount + " for " + o.id());
            return amount;
        }
    }

    static class UpiProcessor extends BaseProcessor implements Refundable {
        private final String vpa;
        UpiProcessor(String vpa) { this.vpa = vpa; }
        @Override public String name() { return "UPI " + vpa; }
        @Override public boolean authorize(Order o) { return vpa.matches("[\\w.]+@\\w+") && o.amount() <= 1_00_000; }
        @Override public double fee(double amount) { return 0; }                 // zero MDR on UPI
        @Override public double refund(Order o, double amount) { log.add("UPI reversal Rs " + amount); return amount; }
    }

    static class WalletProcessor extends BaseProcessor implements Rechargeable {
        private double balance;
        WalletProcessor(double opening) { balance = opening; }
        @Override public String name() { return "Wallet"; }
        @Override public boolean authorize(Order o) {
            if (o.amount() > balance) return false;
            balance -= o.amount();
            return true;
        }
        @Override public double fee(double amount) { return Math.min(amount * 0.01, 25); }   // 1% capped at Rs 25
        @Override public void topUp(double amount) { balance += amount; }
        @Override public double balance() { return balance; }
    }

    // ============================ checkout depends ONLY on the interface ============================
    static Receipt checkout(Order order, PaymentMethod method) {
        return method.pay(order);
    }

    public static void main(String[] args) {
        CardProcessor card = new CardProcessor("4242", 60_000);
        UpiProcessor upi = new UpiProcessor("srikar@okbank");
        UpiProcessor badUpi = new UpiProcessor("not-a-vpa");
        WalletProcessor wallet = new WalletProcessor(1_500);

        Map<Order, PaymentMethod> cart = new LinkedHashMap<>();
        cart.put(new Order("#1001", "Srikar", 3_499), card);
        cart.put(new Order("#1002", "Srikar", 54_999), card);     // > Rs 5,000 -> 3-D Secure OTP step
        cart.put(new Order("#1003", "Meera", 12_750), upi);
        cart.put(new Order("#1004", "Meera", 899), badUpi);
        cart.put(new Order("#1005", "Ravi", 2_400), wallet);      // insufficient wallet balance
        cart.put(new Order("#1006", "Ravi", 3_50_000), upi);      // fraud limit

        System.out.println("=== CHECKOUT ===");
        double revenue = 0, fees = 0;
        for (Map.Entry<Order, PaymentMethod> e : cart.entrySet()) {
            Receipt r = checkout(e.getKey(), e.getValue());
            System.out.println(r);
            if (r.status().equals("SUCCESS")) { revenue += r.amount(); fees += r.fee(); }
        }

        System.out.println("\n=== CAPABILITY CHECKS (instanceof on interfaces) ===");
        wallet.topUp(2_000);
        System.out.println("  wallet topped up -> balance Rs " + wallet.balance());
        Order retry = new Order("#1005", "Ravi", 2_400);
        System.out.println(checkout(retry, wallet));
        revenue += 2_400; fees += wallet.fee(2_400);

        for (PaymentMethod m : List.of(card, upi, wallet)) {
            System.out.printf("  %-14s refunds: %-3s  top-ups: %s%n", m.name(),
                    m instanceof Refundable ? "yes" : "no", m instanceof Rechargeable ? "yes" : "no");
        }

        System.out.println("\n=== CUSTOMER RETURNS ORDER #1001 (paid by card) ===");
        Order returned = new Order("#1001", "Srikar", 3_499);
        PaymentMethod usedMethod = card;                           // checkout only knows the interface type
        if (usedMethod instanceof Refundable r) {                  // capability check at runtime
            revenue -= r.refund(returned, returned.amount());      // gateway fee is not returned
            System.out.printf("  refunded Rs %,.2f to %s%n", returned.amount(), card.name());
        }

        System.out.printf("%nSettled revenue Rs %,.2f | gateway fees Rs %,.2f | merchant receives Rs %,.2f%n", revenue, fees, revenue - fees);
        System.out.println("Card processor audit log: " + card.log);
    }
}
