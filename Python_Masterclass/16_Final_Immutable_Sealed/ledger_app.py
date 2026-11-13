"""
============================================================================
 SECTION 16 : REAL-WORLD APP - Immutable, auditable Bank Ledger (Python)
============================================================================

 Same append-only ledger as LedgerApp.java. Python's version of a SEALED
 hierarchy is a closed UNION type plus match/case:

     Entry = Deposit | Withdrawal | Transfer | Reversal

 and a final  case _: assert_never(e)  branch. If someone adds a new entry
 type to the union but forgets to handle it, mypy/pyright report an error at
 that line - the same safety Java 21 gets from sealed + exhaustive switch.

 Immutability tools: frozen dataclasses (slots=True for less memory),
 tuple-based storage, append() returning a NEW Ledger, itertools.count for IDs.

 HOW TO RUN:  python3 ledger_app.py
============================================================================
"""
from dataclasses import dataclass
from datetime import datetime, timedelta
from itertools import count
from typing import assert_never

_ids = count(10_000)


def new_id() -> int:
    return next(_ids)


@dataclass(frozen=True, slots=True)
class Deposit:
    id: int
    at: datetime
    account: str
    paise: int
    channel: str

    def __post_init__(self):
        if self.paise <= 0:
            raise ValueError("deposit must be positive")


@dataclass(frozen=True, slots=True)
class Withdrawal:
    id: int
    at: datetime
    account: str
    paise: int
    atm: str


@dataclass(frozen=True, slots=True)
class Transfer:
    id: int
    at: datetime
    src: str
    dst: str
    paise: int
    ref: str


@dataclass(frozen=True, slots=True)
class Reversal:
    id: int
    at: datetime
    reversed_id: int
    reason: str


Entry = Deposit | Withdrawal | Transfer | Reversal      # the closed ("sealed") set of entry kinds


def effect(e: Entry, acc: str) -> int:
    match e:                                            # exhaustive match over the union
        case Deposit(account=a, paise=p):
            return p if a == acc else 0
        case Withdrawal(account=a, paise=p):
            return -p if a == acc else 0
        case Transfer(src=s, dst=d, paise=p):
            return p if d == acc else -p if s == acc else 0
        case Reversal():
            return 0
        case _:
            assert_never(e)                             # mypy proves this line is unreachable


def describe(e: Entry) -> str:
    match e:
        case Deposit(account=a, paise=p, channel=c):
            return f"DEPOSIT    {a:<5} +Rs {p / 100:>9,.2f} via {c}"
        case Withdrawal(account=a, paise=p, atm=atm):
            return f"WITHDRAW   {a:<5} -Rs {p / 100:>9,.2f} at {atm}"
        case Transfer(src=s, dst=d, paise=p, ref=r):
            return f"TRANSFER   {s} -> {d} Rs {p / 100:>9,.2f} ref {r}"
        case Reversal(reversed_id=rid, reason=why):
            return f"REVERSAL   of #{rid} ({why})"
        case _:
            assert_never(e)


@dataclass(frozen=True)
class Snapshot:
    account: str
    paise: int
    entries: int

    def __str__(self) -> str:
        return f"{self.account:<6} Rs {self.paise / 100:>10,.2f}  ({self.entries} entries)"


@dataclass(frozen=True)
class Ledger:
    entries: tuple[Entry, ...] = ()

    def append(self, e: Entry) -> "Ledger":
        return Ledger(self.entries + (e,))              # new ledger; self untouched

    def find(self, entry_id: int) -> Entry | None:
        return next((e for e in self.entries if e.id == entry_id), None)

    def balance_of(self, acc: str) -> Snapshot:
        reversed_ids = {e.reversed_id for e in self.entries if isinstance(e, Reversal)}
        deltas = [effect(e, acc) for e in self.entries if e.id not in reversed_ids]
        return Snapshot(acc, sum(deltas), sum(1 for d in deltas if d))


def main() -> None:
    t = datetime(2026, 11, 10, 9, 0)
    h = lambda n: t + timedelta(hours=n)
    v0 = Ledger()
    v1 = v0.append(Deposit(new_id(), h(0), "SB-01", 50_000_00, "NEFT salary"))
    v2 = v1.append(Deposit(new_id(), h(1), "SB-02", 12_000_00, "cash"))
    v3 = v2.append(Withdrawal(new_id(), h(2), "SB-01", 5_000_00, "ATM Guindy"))
    rent = Transfer(new_id(), h(3), "SB-01", "SB-02", 18_000_00, "RENT-NOV")
    v4 = v3.append(rent)
    mistake = Transfer(new_id(), h(4), "SB-01", "SB-02", 18_000_00, "RENT-NOV")
    v5 = v4.append(mistake)
    v6 = v5.append(Reversal(new_id(), h(5), mistake.id, f"duplicate of #{rent.id}"))

    print("=== FULL LEDGER (v6) ===")
    for e in v6.entries:
        print(f"  #{e.id} {e.at:%H:%M}  {describe(e)}")

    print("\n=== BALANCES BY REPLAYING HISTORY ===")
    for acc in ("SB-01", "SB-02"):
        print(f"  {v6.balance_of(acc)}")

    print("\n=== TIME TRAVEL ===")
    print(f"  v3 (before rent): {v3.balance_of('SB-01')}")
    print(f"  v5 (double rent): {v5.balance_of('SB-01')}   <- the bug, still in history")
    print(f"  v6 (reversed)   : {v6.balance_of('SB-01')}")
    print(f"  v0 has {len(v0.entries)} entries, v6 has {len(v6.entries)}")

    print("\n=== TAMPER ATTEMPTS ===")
    entries_any: object = v6.entries                       # hide the type so the demo can try forbidden calls
    for attempt, action in [("v6.entries[0] = None", lambda: getattr(entries_any, "__setitem__")(0, None)),
                            ("rent.paise = 1", lambda: setattr(rent, "paise", 1)),
                            ("negative deposit", lambda: Deposit(new_id(), t, "SB-01", -1, "hack"))]:
        try:
            action()
        except (TypeError, AttributeError, ValueError) as e:
            print(f"  {attempt:<22} -> {type(e).__name__}: {e}")
    found = v6.find(rent.id)
    print(f"  lookup #{rent.id} -> {describe(found) if found else 'missing'}")


if __name__ == "__main__":
    main()
