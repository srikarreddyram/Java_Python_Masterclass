/*
 * ============================================================================
 *  SECTION 13 : REAL-WORLD APP - Notification Center (event listeners)
 * ============================================================================
 *
 *  Apps like Swiggy or a bank app fire EVENTS (order placed, payment failed,
 *  OTP requested...) and different CHANNELS (SMS, email, push) react to them.
 *  This is the Observer/listener pattern - historically the #1 use of nested
 *  and anonymous classes in Java (Swing, Android click listeners).
 *
 *  Where each nested-class kind appears
 *   - Event          : static nested RECORD (pure data, no outer instance)
 *   - Listener       : nested interface
 *   - Subscription   : INNER class - it needs NotificationCenter.this to
 *                      unsubscribe itself from the outer object's list
 *   - QuietHoursFilter: LOCAL class inside configure()
 *   - SMS/Email/Audit: ANONYMOUS classes passed straight to subscribe()
 *
 *  HOW TO RUN:  javac NotificationCenterApp.java && java NotificationCenterApp
 * ============================================================================
 */
import java.util.*;

public class NotificationCenterApp {

    enum Type { ORDER_PLACED, ORDER_DELIVERED, PAYMENT_FAILED, OTP, PROMO }

    // static nested record: immutable event data
    record Event(Type type, String user, String message, int hour) { }

    // nested interface
    interface Listener {
        boolean wants(Event e);
        void onEvent(Event e);
    }

    private final Map<Integer, Listener> listeners = new LinkedHashMap<>();
    private final List<String> deliveryLog = new ArrayList<>();
    private int nextId = 1;

    // ======================== INNER class ========================
    class Subscription {
        private final int id;
        private final String name;
        Subscription(int id, String name) { this.id = id; this.name = name; }

        void cancel() {
            listeners.remove(id);                          // touches the OUTER object's private map
            System.out.println("  (" + name + " unsubscribed; " + listeners.size() + " listeners left)");
        }
    }

    Subscription subscribe(String name, Listener l) {
        int id = nextId++;
        listeners.put(id, l);
        return new Subscription(id, name);                 // inner instance bound to THIS center
    }

    void publish(Event e) {
        System.out.printf("%n>> %02d:00 %s for %s: %s%n", e.hour(), e.type(), e.user(), e.message());
        int delivered = 0;
        for (Listener l : listeners.values()) {
            if (l.wants(e)) { l.onEvent(e); delivered++; }
        }
        if (delivered == 0) System.out.println("   (no channel accepted this event)");
    }

    void log(String channel, Event e) { deliveryLog.add(channel + ":" + e.type()); }

    /** Builds the push channel with a LOCAL class that applies quiet hours. */
    Listener configurePush(int quietFrom, int quietTo) {
        class QuietHoursFilter {                           // LOCAL class, captures quietFrom/quietTo
            boolean isQuiet(int hour) { return quietFrom > quietTo ? hour >= quietFrom || hour < quietTo : hour >= quietFrom && hour < quietTo; }
        }
        QuietHoursFilter filter = new QuietHoursFilter();

        return new Listener() {                            // ANONYMOUS class using the local class
            @Override public boolean wants(Event e) {
                boolean urgent = e.type() == Type.OTP || e.type() == Type.PAYMENT_FAILED;
                return urgent || !filter.isQuiet(e.hour());  // urgent alerts ignore quiet hours
            }
            @Override public void onEvent(Event e) {
                System.out.println("   PUSH  -> " + e.user() + "'s phone: " + e.message());
                log("PUSH", e);
            }
        };
    }

    public static void main(String[] args) {
        NotificationCenterApp center = new NotificationCenterApp();

        Subscription sms = center.subscribe("SMS", new Listener() {
            @Override public boolean wants(Event e) { return e.type() == Type.OTP || e.type() == Type.PAYMENT_FAILED; }
            @Override public void onEvent(Event e) {
                String text = e.type() == Type.OTP ? "Your OTP is " + e.message() + ". Do not share it." : e.message();
                System.out.println("   SMS   -> " + text);
                center.log("SMS", e);
            }
        });

        center.subscribe("Email", new Listener() {
            private int sent;                              // anonymous classes may keep state
            @Override public boolean wants(Event e) { return e.type() != Type.OTP; }
            @Override public void onEvent(Event e) {
                sent++;
                System.out.println("   EMAIL -> to " + e.user().toLowerCase() + "@example.com (#" + sent + "): " + e.message());
                center.log("EMAIL", e);
            }
        });

        center.subscribe("Push", center.configurePush(22, 7));   // quiet hours 10 pm - 7 am

        Subscription audit = center.subscribe("Audit", new Listener() {
            @Override public boolean wants(Event e) { return true; }
            @Override public void onEvent(Event e) { System.out.println("   AUDIT -> stored " + e.type()); }
        });

        center.publish(new Event(Type.ORDER_PLACED, "Srikar", "Order #5531 confirmed, Rs 486", 13));
        center.publish(new Event(Type.OTP, "Srikar", "482913", 23));                 // night, but OTP is urgent
        center.publish(new Event(Type.PROMO, "Meera", "50% off on biryani tonight", 23));   // push muted at night

        audit.cancel();                                    // inner object removes itself from its outer center
        sms.cancel();
        center.publish(new Event(Type.PAYMENT_FAILED, "Ravi", "Payment of Rs 1,250 failed. Retry?", 2));
        center.publish(new Event(Type.ORDER_DELIVERED, "Srikar", "Order #5531 delivered", 14));

        Map<String, Integer> perChannel = new TreeMap<>();
        for (String entry : center.deliveryLog) perChannel.merge(entry.split(":")[0], 1, Integer::sum);
        System.out.println("\nDeliveries per channel: " + perChannel);
    }
}
