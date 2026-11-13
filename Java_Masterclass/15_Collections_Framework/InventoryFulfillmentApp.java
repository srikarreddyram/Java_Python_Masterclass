/*
 * ============================================================================
 *  SECTION 15 : REAL-WORLD APP - Warehouse Inventory & Order Fulfilment
 * ============================================================================
 *
 *  A quick-commerce dark store (Blinkit / Zepto style). Every structure is
 *  chosen for a reason - which is the real skill this section teaches:
 *
 *   | Need                                   | Collection                       |
 *   |----------------------------------------|----------------------------------|
 *   | SKU -> stock, O(1) lookup              | HashMap<String, Item>            |
 *   | aisle -> SKUs, sorted aisle walk       | TreeMap<String, TreeSet<String>> |
 *   | orders served by priority then time    | PriorityQueue<Order>             |
 *   | unique categories in arrival order     | LinkedHashSet<String>            |
 *   | recently viewed items (bounded)        | ArrayDeque<String>               |
 *   | items expiring soonest                 | TreeMap<LocalDate, List<String>> |
 *   | restock alerts, one per SKU, sorted    | TreeMap<String, String>          |
 *
 *  HOW TO RUN:  javac InventoryFulfillmentApp.java && java InventoryFulfillmentApp
 * ============================================================================
 */
import java.time.LocalDate;
import java.util.*;

public class InventoryFulfillmentApp {

    static final class Item {
        final String sku, name, category, aisle;
        final double price;
        final LocalDate expiry;
        int qty;
        final int reorderLevel;
        Item(String sku, String name, String category, String aisle, double price, int qty, int reorderLevel, LocalDate expiry) {
            this.sku = sku; this.name = name; this.category = category; this.aisle = aisle;
            this.price = price; this.qty = qty; this.reorderLevel = reorderLevel; this.expiry = expiry;
        }
    }

    enum Tier { EXPRESS, PRIME, STANDARD }

    record Order(String id, String customer, Tier tier, int placedMinute, Map<String, Integer> lines) { }

    private final Map<String, Item> stock = new HashMap<>();
    private final Map<String, TreeSet<String>> aisleMap = new TreeMap<>();
    private final Set<String> categories = new LinkedHashSet<>();
    private final TreeMap<LocalDate, List<String>> expiryIndex = new TreeMap<>();
    private final Map<String, String> restockAlerts = new TreeMap<>();   // sku -> latest alert
    private final Deque<String> recentlyViewed = new ArrayDeque<>();

    void addItem(Item it) {
        stock.put(it.sku, it);
        aisleMap.computeIfAbsent(it.aisle, a -> new TreeSet<>()).add(it.sku);
        categories.add(it.category);
        expiryIndex.computeIfAbsent(it.expiry, d -> new ArrayList<>()).add(it.sku);
    }

    void view(String sku) {                                   // bounded "recently viewed" list
        recentlyViewed.remove(sku);                           // move to front if seen before
        recentlyViewed.addFirst(sku);
        if (recentlyViewed.size() > 3) recentlyViewed.removeLast();
    }

    /** Fulfil an order: all-or-nothing reservation, then a sorted pick path. */
    void fulfil(Order o) {
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, Integer> line : o.lines().entrySet()) {
            Item it = stock.get(line.getKey());
            if (it == null || it.qty < line.getValue()) missing.add(line.getKey());
        }
        if (!missing.isEmpty()) {
            System.out.printf("  %-5s %-8s %-8s REJECTED, short on %s%n", o.id(), o.customer(), o.tier(), missing);
            return;
        }
        double total = 0;
        TreeMap<String, List<String>> pickPath = new TreeMap<>();   // walk aisles in order A1, A2, B1...
        for (Map.Entry<String, Integer> line : o.lines().entrySet()) {
            Item it = stock.get(line.getKey());
            it.qty -= line.getValue();
            total += it.price * line.getValue();
            pickPath.computeIfAbsent(it.aisle, a -> new ArrayList<>()).add(line.getValue() + "x " + it.name);
            if (it.qty <= it.reorderLevel) restockAlerts.put(it.sku, it.name + " (" + it.qty + " left)");   // put replaces the older alert
        }
        System.out.printf("  %-5s %-8s %-8s Rs %,7.2f  pick path: %s%n", o.id(), o.customer(), o.tier(), total, pickPath);
    }

    public static void main(String[] args) {
        InventoryFulfillmentApp store = new InventoryFulfillmentApp();
        LocalDate today = LocalDate.of(2026, 11, 10);
        store.addItem(new Item("MLK1", "Toned Milk 1L", "Dairy", "A1", 56, 40, 10, today.plusDays(2)));
        store.addItem(new Item("CRD1", "Curd 400g", "Dairy", "A1", 45, 12, 5, today.plusDays(4)));
        store.addItem(new Item("BRD1", "Brown Bread", "Bakery", "A2", 50, 8, 4, today.plusDays(1)));
        store.addItem(new Item("EGG6", "Eggs x6", "Dairy", "A1", 54, 30, 8, today.plusDays(10)));
        store.addItem(new Item("RCE5", "Basmati Rice 5kg", "Staples", "C3", 649, 6, 2, today.plusDays(300)));
        store.addItem(new Item("ATA5", "Wheat Atta 5kg", "Staples", "C3", 289, 9, 3, today.plusDays(120)));
        store.addItem(new Item("CHP1", "Potato Chips", "Snacks", "B2", 20, 50, 15, today.plusDays(90)));
        store.addItem(new Item("COK2", "Cola 2L", "Beverages", "B1", 95, 14, 5, today.plusDays(150)));
        store.addItem(new Item("BAN6", "Bananas x6", "Fruits", "A2", 48, 10, 4, today.plusDays(3)));

        System.out.println("=== STORE LAYOUT (TreeMap of TreeSets) ===");
        store.aisleMap.forEach((aisle, skus) -> System.out.println("  " + aisle + " -> " + skus));
        System.out.println("  categories (LinkedHashSet, first-seen order): " + store.categories);

        // Orders arrive in time order; the queue serves EXPRESS first, then by arrival time.
        PriorityQueue<Order> queue = new PriorityQueue<>(Comparator.comparing(Order::tier).thenComparingInt(Order::placedMinute));
        queue.add(new Order("#501", "Srikar", Tier.STANDARD, 1, Map.of("MLK1", 2, "BRD1", 1, "CHP1", 4)));
        queue.add(new Order("#502", "Meera", Tier.EXPRESS, 2, Map.of("BRD1", 5, "EGG6", 2)));
        queue.add(new Order("#503", "Ravi", Tier.PRIME, 3, Map.of("RCE5", 5, "ATA5", 2, "COK2", 1)));
        queue.add(new Order("#504", "Asha", Tier.EXPRESS, 4, Map.of("BRD1", 3, "BAN6", 2)));
        queue.add(new Order("#505", "Karan", Tier.STANDARD, 5, Map.of("CRD1", 8, "MLK1", 30)));

        System.out.println("\n=== FULFILMENT (PriorityQueue: EXPRESS > PRIME > STANDARD, then FIFO) ===");
        while (!queue.isEmpty()) store.fulfil(queue.poll());

        System.out.println("\n=== RESTOCK ALERTS (TreeMap keyed by SKU: one alert per item) ===");
        store.restockAlerts.forEach((sku, msg) -> System.out.println("  " + sku + " " + msg));

        System.out.println("\n=== EXPIRING WITHIN 3 DAYS (TreeMap.headMap) ===");
        store.expiryIndex.headMap(today.plusDays(3), true).forEach((date, skus) -> {
            List<String> names = new ArrayList<>();
            for (String s : skus) names.add(store.stock.get(s).name + " (qty " + store.stock.get(s).qty + ")");
            System.out.println("  " + date + " -> " + names + "  => mark down 30%");
        });

        System.out.println("\n=== RECENTLY VIEWED (ArrayDeque, max 3, most recent first) ===");
        for (String sku : List.of("CHP1", "COK2", "MLK1", "CHP1", "BAN6")) {
            store.view(sku);
            System.out.println("  viewed " + sku + " -> " + store.recentlyViewed);
        }

        System.out.println("\n=== STOCK VALUE BY CATEGORY (TreeMap + merge) ===");
        Map<String, Double> value = new TreeMap<>();
        for (Item it : store.stock.values()) value.merge(it.category, it.price * it.qty, Double::sum);
        value.forEach((c, v) -> System.out.printf("  %-10s Rs %,9.2f%n", c, v));
    }
}
