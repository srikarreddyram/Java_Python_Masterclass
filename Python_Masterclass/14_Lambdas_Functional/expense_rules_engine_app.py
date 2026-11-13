"""
============================================================================
 SECTION 14 : REAL-WORLD APP - Expense Tracker with a Rules Engine (Python)
============================================================================

 Same scenario as ExpenseRulesEngineApp.java. Rules are DATA: each rule is a
 (name, predicate, message) triple of plain functions/lambdas, so the product
 team adds a rule by adding one line.

 Python features doing the work
  - higher-order factories: merchant_has(...), amount_above(...) return predicates
  - all_of / any_of combinators build compound rules (Predicate.and / .or)
  - next(...) over a generator expression = "first matching category"
  - sorted(key=...) with a tuple key replaces Comparator chains
  - functools.reduce composes the cashback pipeline
  - a closure keeps the 'seen' set for duplicate detection

 HOW TO RUN:  python3 expense_rules_engine_app.py
============================================================================
"""
from collections import Counter, defaultdict
from collections.abc import Callable
from datetime import date
from functools import reduce
from typing import NamedTuple


class Txn(NamedTuple):
    day: date
    merchant: str
    amount: float
    city: str
    international: bool = False


Predicate = Callable[[Txn], bool]


class Rule(NamedTuple):
    name: str
    when: Predicate
    message: Callable[[Txn], str]


def merchant_has(*keywords: str) -> Predicate:
    return lambda t: any(k in t.merchant.lower() for k in keywords)


def amount_above(limit: float) -> Predicate:
    return lambda t: t.amount > limit


def all_of(*preds: Predicate) -> Predicate:
    return lambda t: all(p(t) for p in preds)


def is_duplicate() -> Predicate:
    seen: set[tuple] = set()                      # closure state

    def check(t: Txn) -> bool:
        key = (t.day, t.merchant, t.amount)
        if key in seen:
            return True
        seen.add(key)
        return False
    return check


TXNS = [
    Txn(date(2026, 11, 1), "Swiggy Instamart", 642, "Chennai"),
    Txn(date(2026, 11, 2), "Uber India", 318, "Chennai"),
    Txn(date(2026, 11, 3), "Amazon Electronics", 24_999, "Online"),
    Txn(date(2026, 11, 4), "Zomato", 486, "Chennai"),
    Txn(date(2026, 11, 5), "Netflix.com", 649, "Online", True),
    Txn(date(2026, 11, 6), "Indian Oil Petrol", 2_100, "Goa"),
    Txn(date(2026, 11, 7), "Zomato", 1_240, "Goa"),
    Txn(date(2026, 11, 8), "Steam Games", 3_200, "Online", True),
    Txn(date(2026, 11, 8), "Steam Games", 3_200, "Online", True),
    Txn(date(2026, 11, 9), "Apollo Pharmacy", 890, "Chennai"),
]

CATEGORIES: list[tuple[Predicate, str]] = [
    (merchant_has("swiggy", "zomato"), "Food"),
    (merchant_has("uber", "ola", "petrol", "oil"), "Transport"),
    (merchant_has("amazon", "flipkart"), "Shopping"),
    (merchant_has("netflix", "spotify", "steam"), "Entertainment"),
    (merchant_has("pharmacy", "hospital"), "Health"),
]

RULES = [
    Rule("BIG_SPEND", amount_above(10_000), lambda t: f"Large purchase Rs {t.amount:,.0f} at {t.merchant}"),
    Rule("FOREX", lambda t: t.international, lambda t: f"International txn - 3.5% forex markup applies to Rs {t.amount:,.0f}"),
    Rule("NEW_CITY", lambda t: t.city not in ("Chennai", "Online"), lambda t: f"Card used in {t.city} - was this you?"),
    Rule("DUPLICATE", is_duplicate(), lambda t: f"Possible duplicate charge at {t.merchant}"),
    Rule("LATE_NIGHT_FOOD", all_of(merchant_has("zomato"), amount_above(1_000)), lambda t: f"Rs {t.amount:,.0f} food order. Party?"),
]

BUDGETS = {"Food": 2_500, "Transport": 3_000, "Shopping": 15_000, "Entertainment": 2_000}


def categorise(t: Txn) -> str:
    return next((cat for pred, cat in CATEGORIES if pred(t)), "Other")


def main() -> None:
    print("=== TRANSACTIONS + ALERTS ===")
    by_category: defaultdict[str, float] = defaultdict(float)
    alerts: Counter[str] = Counter()
    for t in TXNS:
        cat = categorise(t)
        by_category[cat] += t.amount
        print(f"  {t.day}  {t.merchant:<20} Rs {t.amount:>8,.0f}  [{cat}]")
        for rule in RULES:
            if rule.when(t):
                print(f"        ! {rule.name}: {rule.message(t)}")
                alerts[rule.name] += 1

    print("\n=== BUDGET CHECK ===")
    def verdict(spent: float, limit: float | None) -> str:
        if limit is None:
            return "no budget set"
        return f"OVER by Rs {spent - limit:,.0f}" if spent > limit else f"{spent / limit:.0%} used"
    for cat, spent in sorted(by_category.items()):
        print(f"  {cat:<14} Rs {spent:>8,.0f}  {verdict(spent, BUDGETS.get(cat))}")

    print("\n=== TOP 3 EXPENSES (sorted with a tuple key) ===")
    for t in sorted(TXNS, key=lambda t: (-t.amount, t.day))[:3]:
        print(f"  Rs {t.amount:>8,.0f}  {t.merchant}")

    print("\n=== CASHBACK PIPELINE (reduce-composed functions) ===")
    pipeline = [lambda amt: amt * 0.05, lambda cb: min(cb, 500.0)]
    cashback = lambda amt: reduce(lambda acc, f: f(acc), pipeline, amt)
    total_cb = sum(cashback(t.amount) for t in TXNS if t.city == "Online")
    print(f"  5% on online spends, capped at Rs 500 per txn -> Rs {total_cb:,.2f}")

    print(f"\nAlert summary: {dict(alerts)}")


if __name__ == "__main__":
    main()
