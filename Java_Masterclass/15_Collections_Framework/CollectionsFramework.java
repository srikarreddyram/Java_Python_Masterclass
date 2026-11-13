/*
 * ============================================================================
 *  SECTION 15 : THE JAVA COLLECTIONS FRAMEWORK - Collection hierarchy, Set,
 *               Map, Queue/Deque, PriorityQueue, the Collections utility class,
 *               equals/hashCode contract, sorted & navigable collections
 * ============================================================================
 *
 *  THE HIERARCHY (interfaces on the left, main implementations on the right)
 *
 *   Iterable
 *     Collection
 *       |- List   ordered, duplicates, index      ArrayList, LinkedList
 *       |- Set    NO duplicates                   HashSet (no order)
 *       |    |                                    LinkedHashSet (insertion order)
 *       |    '- SortedSet/NavigableSet            TreeSet (sorted, red-black tree)
 *       '- Queue  FIFO / priority                 PriorityQueue (binary heap)
 *            '- Deque  both ends                  ArrayDeque (fast stack AND queue)
 *   Map (NOT a Collection) key -> value          HashMap, LinkedHashMap, TreeMap
 *
 *   | Implementation | get/contains/add | Order            | Nulls        |
 *   |----------------|------------------|------------------|--------------|
 *   | HashSet/Map    | O(1) avg         | none             | 1 null key   |
 *   | LinkedHash*    | O(1) avg         | insertion/access | 1 null key   |
 *   | TreeSet/Map    | O(log n)         | sorted           | no null keys |
 *   | ArrayDeque     | O(1) ends        | FIFO/LIFO        | no nulls     |
 *   | PriorityQueue  | O(log n) add/poll| by priority      | no nulls     |
 *
 *  equals/hashCode CONTRACT: if a.equals(b) then a.hashCode() == b.hashCode().
 *  Break it and HashSet/HashMap silently lose or duplicate your objects.
 *
 *  'Collections' (plural) is a UTILITY class: sort, shuffle, reverse, max, min,
 *  frequency, nCopies, unmodifiableList, synchronizedList, emptyList...
 *
 *  HOW TO RUN:  javac CollectionsFramework.java && java CollectionsFramework
 * ============================================================================
 */
import java.util.*;

public class CollectionsFramework {

    /** Correct equals/hashCode (a record would generate these for us). */
    static final class Contact {
        final String name, email;
        Contact(String name, String email) { this.name = name; this.email = email; }
        @Override public boolean equals(Object o) {
            return o instanceof Contact c && email.equalsIgnoreCase(c.email);   // identity = email
        }
        @Override public int hashCode() { return email.toLowerCase().hashCode(); }
        @Override public String toString() { return name + "<" + email + ">"; }
    }

    /** BROKEN: equals overridden, hashCode NOT -> violates the contract. */
    @SuppressWarnings("overrides")                          // deliberate bug for the demo
    static final class BadContact {
        final String email;
        BadContact(String email) { this.email = email; }
        @Override public boolean equals(Object o) { return o instanceof BadContact b && email.equals(b.email); }
    }

    public static void main(String[] args) {
        sets();
        hashContract();
        maps();
        queuesAndDeques();
        utilities();
    }

    static void sets() {
        System.out.println("=== SETS: uniqueness and ordering ===");
        List<String> visits = List.of("goa", "pune", "agra", "goa", "delhi", "pune", "chennai");
        System.out.println("  input         : " + visits);
        System.out.println("  HashSet       : " + new HashSet<>(visits) + "  (order not guaranteed)");
        System.out.println("  LinkedHashSet : " + new LinkedHashSet<>(visits) + "  (first-seen order)");
        TreeSet<String> sorted = new TreeSet<>(visits);
        System.out.println("  TreeSet       : " + sorted + "  (sorted)");
        System.out.println("  add(\"goa\") again returns " + sorted.add("goa") + " - duplicates rejected");

        System.out.println("  NavigableSet: first=" + sorted.first() + " last=" + sorted.last()
                + " ceiling(\"e\")=" + sorted.ceiling("e") + " floor(\"e\")=" + sorted.floor("e")
                + " headSet(\"delhi\")=" + sorted.headSet("delhi") + " descending=" + sorted.descendingSet());

        Set<String> a = new TreeSet<>(Set.of("java", "python", "sql", "aws"));
        Set<String> b = new TreeSet<>(Set.of("python", "ml", "sql", "spark"));
        Set<String> union = new TreeSet<>(a); union.addAll(b);
        Set<String> inter = new TreeSet<>(a); inter.retainAll(b);
        Set<String> diff = new TreeSet<>(a); diff.removeAll(b);
        System.out.println("  set algebra   : A u B=" + union + "  A n B=" + inter + "  A - B=" + diff);
    }

    static void hashContract() {
        System.out.println("\n=== equals / hashCode CONTRACT ===");
        Set<Contact> good = new HashSet<>();
        good.add(new Contact("Srikar", "srikar@x.com"));
        good.add(new Contact("Srikar R", "SRIKAR@X.COM"));        // same email -> same contact
        System.out.println("  correct class: " + good.size() + " contact(s) " + good);
        Set<BadContact> bad = new HashSet<>();
        bad.add(new BadContact("a@x.com"));
        bad.add(new BadContact("a@x.com"));
        System.out.println("  broken class : " + bad.size() + " entries for the SAME email (different hashCodes -> different buckets)");
        System.out.println("  contains(new BadContact(a@x.com)) = " + bad.contains(new BadContact("a@x.com")));
    }

    static void maps() {
        System.out.println("\n=== MAPS ===");
        String text = "to be or not to be that is the question to ask";
        Map<String, Integer> freq = new HashMap<>();
        for (String w : text.split(" ")) freq.merge(w, 1, Integer::sum);          // count with merge
        System.out.println("  word counts (HashMap)  : " + new TreeMap<>(freq));

        Map<String, List<String>> byLetter = new TreeMap<>();
        for (String w : text.split(" ")) byLetter.computeIfAbsent(w.substring(0, 1), k -> new ArrayList<>()).add(w);
        System.out.println("  grouping (computeIfAbsent): " + byLetter);

        Map<String, Integer> stock = new LinkedHashMap<>();
        stock.put("pen", 40); stock.put("notebook", 12); stock.put("eraser", 0);
        stock.putIfAbsent("pen", 999);                       // ignored: key exists
        stock.computeIfPresent("notebook", (k, v) -> v - 2);
        stock.compute("marker", (k, v) -> v == null ? 5 : v + 5);
        stock.replaceAll((k, v) -> v == 0 ? -1 : v);          // flag out-of-stock
        System.out.println("  LinkedHashMap ops      : " + stock + ", getOrDefault(glue)=" + stock.getOrDefault("glue", 0));
        for (Map.Entry<String, Integer> e : stock.entrySet()) if (e.getValue() < 0) System.out.println("  OUT OF STOCK: " + e.getKey());

        TreeMap<Integer, String> slabs = new TreeMap<>(Map.of(0, "Bronze", 1_000, "Silver", 5_000, "Gold", 20_000, "Platinum"));
        for (int points : new int[]{450, 1_000, 7_250, 50_000})
            System.out.println("  TreeMap.floorEntry(" + points + ") -> tier " + slabs.floorEntry(points).getValue());

        LinkedHashMap<String, String> lru = new LinkedHashMap<>(16, 0.75f, true) {   // access-order + eviction = LRU cache
            @Override protected boolean removeEldestEntry(Map.Entry<String, String> eldest) { return size() > 3; }
        };
        lru.put("a", "1"); lru.put("b", "2"); lru.put("c", "3");
        lru.get("a");                                         // 'a' becomes most recently used
        lru.put("d", "4");                                    // evicts least recently used: 'b'
        System.out.println("  LRU cache (LinkedHashMap access order): " + lru.keySet());
    }

    static void queuesAndDeques() {
        System.out.println("\n=== QUEUES & DEQUES ===");
        Deque<String> stack = new ArrayDeque<>();
        stack.push("page1"); stack.push("page2"); stack.push("page3");
        System.out.println("  ArrayDeque as stack: pop=" + stack.pop() + " peek=" + stack.peek());
        Queue<String> fifo = new ArrayDeque<>(List.of("cust1", "cust2"));
        fifo.offer("cust3");
        System.out.println("  ArrayDeque as queue: poll=" + fifo.poll() + " remaining=" + fifo);

        record Ticket(String id, int priority) { }
        PriorityQueue<Ticket> pq = new PriorityQueue<>(Comparator.comparingInt(Ticket::priority).thenComparing(Ticket::id));
        pq.add(new Ticket("T-104", 3)); pq.add(new Ticket("T-101", 1)); pq.add(new Ticket("T-103", 2)); pq.add(new Ticket("T-102", 1));
        StringBuilder order = new StringBuilder();
        while (!pq.isEmpty()) order.append(pq.poll().id()).append(' ');
        System.out.println("  PriorityQueue (P1 first): " + order.toString().trim());
    }

    static void utilities() {
        System.out.println("\n=== THE Collections UTILITY CLASS ===");
        List<Integer> nums = new ArrayList<>(List.of(5, 3, 9, 1, 9, 7));
        Collections.sort(nums);
        System.out.println("  sort          : " + nums + ", binarySearch(7)=" + Collections.binarySearch(nums, 7));
        Collections.reverse(nums);
        System.out.println("  reverse       : " + nums + ", max=" + Collections.max(nums) + " min=" + Collections.min(nums) + " frequency(9)=" + Collections.frequency(nums, 9));
        Collections.shuffle(nums, new Random(1));
        System.out.println("  shuffle(seed) : " + nums);
        Collections.swap(nums, 0, nums.size() - 1);
        System.out.println("  swap first/last: " + nums + ", nCopies: " + Collections.nCopies(3, "ha"));
        List<Integer> readOnly = Collections.unmodifiableList(nums);
        try { readOnly.add(1); } catch (UnsupportedOperationException e) { System.out.println("  unmodifiableList.add -> UnsupportedOperationException (a VIEW: changes to nums still show through)"); }
        List<Integer> copy = List.copyOf(nums);              // truly immutable snapshot
        nums.clear();
        System.out.println("  List.copyOf snapshot survives nums.clear(): " + copy + ", the view now: " + readOnly);
    }
}
