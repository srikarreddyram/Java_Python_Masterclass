/*
 * ============================================================================
 *  SECTION 16 : REAL-WORLD APP - Immutable, auditable Bank Ledger
 * ============================================================================
 *
 *  Banks never EDIT a past transaction: they append new entries and keep every
 *  version (an append-only / event-sourced ledger). Immutability makes this safe:
 *  once an entry exists, nothing can change it, so any balance can be replayed.
 *
 *  Design
 *    sealed interface Entry permits Deposit, Withdrawal, Transfer, Reversal
 *        - every entry is an immutable RECORD (implicitly final)
 *        - sealed: the ledger knows the COMPLETE list of entry kinds, so the
 *          balance calculation can't silently miss a new kind
 *    final class Ledger
 *        - private final List kept internally; only unmodifiable views escape
 *        - append() returns a NEW Ledger (persistent / copy-on-write style)
 *        - every entry gets an ID from a static counter set in a static block
 *    record AccountSnapshot - an immutable point-in-time balance view
 *
 *  HOW TO RUN:  javac LedgerApp.java && java LedgerApp
 * ============================================================================
 */
import java.time.LocalDateTime;
import java.util.*;

public class LedgerApp {

    // ======================== sealed entry hierarchy ========================
    sealed interface Entry permits Deposit, Withdrawal, Transfer, Reversal {
        long id();
        LocalDateTime at();
    }

    record Deposit(long id, LocalDateTime at, String account, long paise, String channel) implements Entry {
        Deposit {                                            // compact constructor = validation
            if (paise <= 0) throw new IllegalArgumentException("deposit must be positive");
        }
    }
    record Withdrawal(long id, LocalDateTime at, String account, long paise, String atm) implements Entry { }
    record Transfer(long id, LocalDateTime at, String from, String to, long paise, String ref) implements Entry { }
    record Reversal(long id, LocalDateTime at, long reversedId, String reason) implements Entry { }

    record AccountSnapshot(String account, long paise, int entries) {
        @Override public String toString() { return String.format("%-6s Rs %,10.2f  (%d entries)", account, paise / 100.0, entries); }
    }

    // ======================== immutable ledger ========================
    static final class Ledger {
        private static long nextId;
        static {                                             // static initializer: seed the ID sequence once
            nextId = 10_000;
        }

        private final List<Entry> entries;                   // never exposed directly

        private Ledger(List<Entry> entries) { this.entries = List.copyOf(entries); }
        static Ledger empty() { return new Ledger(List.of()); }

        static long newId() { return nextId++; }

        /** Returns a NEW ledger; this one is untouched (persistent data structure). */
        Ledger append(Entry e) {
            List<Entry> next = new ArrayList<>(entries);
            next.add(e);
            return new Ledger(next);
        }

        List<Entry> entries() { return entries; }            // List.copyOf is already unmodifiable

        Optional<Entry> find(long id) { return entries.stream().filter(e -> e.id() == id).findFirst(); }

        /** Balance by replaying history. Reversed entries are skipped. */
        AccountSnapshot balanceOf(String account) {
            Set<Long> reversed = new HashSet<>();
            for (Entry e : entries) if (e instanceof Reversal r) reversed.add(r.reversedId());
            long bal = 0;
            int count = 0;
            for (Entry e : entries) {
                if (reversed.contains(e.id())) continue;
                long delta = effect(e, account);             // every kind handled below
                if (delta != 0) { bal += delta; count++; }
            }
            return new AccountSnapshot(account, bal, count);
        }

        private static long effect(Entry e, String acc) {
            if (e instanceof Deposit d) return d.account().equals(acc) ? d.paise() : 0;
            if (e instanceof Withdrawal w) return w.account().equals(acc) ? -w.paise() : 0;
            if (e instanceof Transfer t) return t.to().equals(acc) ? t.paise() : t.from().equals(acc) ? -t.paise() : 0;
            if (e instanceof Reversal) return 0;
            throw new AssertionError("unknown entry kind");  // unreachable: Entry is sealed
            // Java 21: switch (e) { case Deposit d -> ...; case Withdrawal w -> ...; ... }  - the compiler
            // would REFUSE to compile if a new permitted type were added and not handled.
        }
    }

    static String describe(Entry e) {
        if (e instanceof Deposit d) return String.format("DEPOSIT    %-5s +Rs %,9.2f via %s", d.account(), d.paise() / 100.0, d.channel());
        if (e instanceof Withdrawal w) return String.format("WITHDRAW   %-5s -Rs %,9.2f at %s", w.account(), w.paise() / 100.0, w.atm());
        if (e instanceof Transfer t) return String.format("TRANSFER   %s -> %s Rs %,9.2f ref %s", t.from(), t.to(), t.paise() / 100.0, t.ref());
        Reversal r = (Reversal) e;                           // only remaining permitted type
        return String.format("REVERSAL   of #%d (%s)", r.reversedId(), r.reason());
    }

    public static void main(String[] args) {
        LocalDateTime t = LocalDateTime.of(2026, 11, 10, 9, 0);
        Ledger v0 = Ledger.empty();
        Ledger v1 = v0.append(new Deposit(Ledger.newId(), t, "SB-01", 50_000_00, "NEFT salary"));
        Ledger v2 = v1.append(new Deposit(Ledger.newId(), t.plusHours(1), "SB-02", 12_000_00, "cash"));
        Ledger v3 = v2.append(new Withdrawal(Ledger.newId(), t.plusHours(2), "SB-01", 5_000_00, "ATM Guindy"));
        Transfer rent = new Transfer(Ledger.newId(), t.plusHours(3), "SB-01", "SB-02", 18_000_00, "RENT-NOV");
        Ledger v4 = v3.append(rent);
        Transfer mistake = new Transfer(Ledger.newId(), t.plusHours(4), "SB-01", "SB-02", 18_000_00, "RENT-NOV");
        Ledger v5 = v4.append(mistake);                      // duplicate transfer by mistake
        Ledger v6 = v5.append(new Reversal(Ledger.newId(), t.plusHours(5), mistake.id(), "duplicate of #" + rent.id()));

        System.out.println("=== FULL LEDGER (latest version v6) ===");
        for (Entry e : v6.entries()) System.out.printf("  #%d %s  %s%n", e.id(), e.at().toLocalTime(), describe(e));

        System.out.println("\n=== BALANCES BY REPLAYING HISTORY ===");
        for (String acc : List.of("SB-01", "SB-02")) System.out.println("  " + v6.balanceOf(acc));

        System.out.println("\n=== TIME TRAVEL: older versions are untouched ===");
        System.out.println("  v3 (before rent): " + v3.balanceOf("SB-01"));
        System.out.println("  v5 (double rent): " + v5.balanceOf("SB-01") + "   <- the bug, still visible in history");
        System.out.println("  v6 (reversed)   : " + v6.balanceOf("SB-01"));
        System.out.println("  v0 has " + v0.entries().size() + " entries, v6 has " + v6.entries().size());

        System.out.println("\n=== TAMPER ATTEMPTS ===");
        try {
            v6.entries().remove(0);
        } catch (UnsupportedOperationException e) {
            System.out.println("  v6.entries().remove(0) -> UnsupportedOperationException");
        }
        try {
            new Deposit(Ledger.newId(), t, "SB-01", -1, "hack");
        } catch (IllegalArgumentException e) {
            System.out.println("  negative deposit -> " + e.getMessage());
        }
        System.out.println("  records have no setters: the compiler offers no way to edit #" + rent.id());
        System.out.println("  lookup #" + rent.id() + " -> " + v6.find(rent.id()).map(LedgerApp::describe).orElse("missing"));
    }
}
