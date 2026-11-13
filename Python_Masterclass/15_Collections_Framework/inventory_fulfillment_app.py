"""
============================================================================
 SECTION 15 : REAL-WORLD APP - Warehouse Inventory & Fulfilment (Python)
============================================================================

 Same dark-store scenario as InventoryFulfillmentApp.java. Choosing the
 right structure is the skill:

   | Need                                  | Python structure                        |
   |---------------------------------------|-----------------------------------------|
   | SKU -> item, O(1) lookup              | dict[str, Item]                         |
   | aisle -> SKUs (walked in aisle order) | defaultdict(set), sorted() when printing|
   | orders by tier then arrival           | heapq with (tier, minute, order) tuples |
   | unique categories in arrival order    | dict.fromkeys / dict as ordered set     |
   | recently viewed (bounded, MRU first)  | deque(maxlen=3)                         |
   | items expiring soon                   | sorted list of (date, sku) + bisect     |
   | one restock alert per SKU             | dict (latest assignment wins)           |
   | stock value per category              | defaultdict(float)                      |

 HOW TO RUN:  python3 inventory_fulfillment_app.py
============================================================================
"""
import bisect
import heapq
from collections import defaultdict, deque
from dataclasses import dataclass, field
from datetime import date, timedelta
from enum import IntEnum


class Tier(IntEnum):            # IntEnum sorts EXPRESS(1) < PRIME(2) < STANDARD(3)
    EXPRESS = 1
    PRIME = 2
    STANDARD = 3


@dataclass
class Item:
    sku: str
    name: str
    category: str
    aisle: str
    price: float
    qty: int
    reorder_level: int
    expiry: date


@dataclass(order=True)
class Order:                    # order=True generates <, > using the fields in order
    tier: Tier
    minute: int
    id: str = field(compare=False)
    customer: str = field(compare=False)
    lines: dict[str, int] = field(compare=False, default_factory=dict)


class Store:
    def __init__(self):
        self.stock: dict[str, Item] = {}
        self.aisles: defaultdict[str, set[str]] = defaultdict(set)
        self.categories: dict[str, None] = {}
        self.expiry_index: list[tuple[date, str]] = []
        self.restock: dict[str, str] = {}
        self.recent: deque[str] = deque(maxlen=3)

    def add(self, it: Item) -> None:
        self.stock[it.sku] = it
        self.aisles[it.aisle].add(it.sku)
        self.categories.setdefault(it.category)
        bisect.insort(self.expiry_index, (it.expiry, it.sku))

    def view(self, sku: str) -> None:
        if sku in self.recent:
            self.recent.remove(sku)
        self.recent.appendleft(sku)              # maxlen drops the oldest from the right

    def fulfil(self, o: Order) -> None:
        missing = [s for s, q in o.lines.items() if s not in self.stock or self.stock[s].qty < q]
        if missing:
            print(f"  {o.id:<5} {o.customer:<8} {o.tier.name:<8} REJECTED, short on {missing}")
            return
        total, path = 0.0, defaultdict(list)
        for sku, q in o.lines.items():
            it = self.stock[sku]
            it.qty -= q
            total += it.price * q
            path[it.aisle].append(f"{q}x {it.name}")
            if it.qty <= it.reorder_level:
                self.restock[sku] = f"{it.name} ({it.qty} left)"
        route = {a: path[a] for a in sorted(path)}
        print(f"  {o.id:<5} {o.customer:<8} {o.tier.name:<8} Rs {total:>8,.2f}  pick path: {route}")


def main() -> None:
    today = date(2026, 11, 10)
    store = Store()
    d = lambda n: today + timedelta(days=n)
    for args in [("MLK1", "Toned Milk 1L", "Dairy", "A1", 56, 40, 10, d(2)), ("CRD1", "Curd 400g", "Dairy", "A1", 45, 12, 5, d(4)),
                 ("BRD1", "Brown Bread", "Bakery", "A2", 50, 8, 4, d(1)), ("EGG6", "Eggs x6", "Dairy", "A1", 54, 30, 8, d(10)),
                 ("RCE5", "Basmati Rice 5kg", "Staples", "C3", 649, 6, 2, d(300)), ("ATA5", "Wheat Atta 5kg", "Staples", "C3", 289, 9, 3, d(120)),
                 ("CHP1", "Potato Chips", "Snacks", "B2", 20, 50, 15, d(90)), ("COK2", "Cola 2L", "Beverages", "B1", 95, 14, 5, d(150)),
                 ("BAN6", "Bananas x6", "Fruits", "A2", 48, 10, 4, d(3))]:
        store.add(Item(*args))

    print("=== STORE LAYOUT ===")
    for aisle in sorted(store.aisles):
        print(f"  {aisle} -> {sorted(store.aisles[aisle])}")
    print(f"  categories (first-seen order): {list(store.categories)}")

    queue: list[Order] = []
    for o in [Order(Tier.STANDARD, 1, "#501", "Srikar", {"MLK1": 2, "BRD1": 1, "CHP1": 4}),
              Order(Tier.EXPRESS, 2, "#502", "Meera", {"BRD1": 5, "EGG6": 2}),
              Order(Tier.PRIME, 3, "#503", "Ravi", {"RCE5": 5, "ATA5": 2, "COK2": 1}),
              Order(Tier.EXPRESS, 4, "#504", "Asha", {"BRD1": 3, "BAN6": 2}),
              Order(Tier.STANDARD, 5, "#505", "Karan", {"CRD1": 8, "MLK1": 30})]:
        heapq.heappush(queue, o)                 # dataclass(order=True) makes Orders comparable

    print("\n=== FULFILMENT (heapq: EXPRESS > PRIME > STANDARD, then FIFO) ===")
    while queue:
        store.fulfil(heapq.heappop(queue))

    print("\n=== RESTOCK ALERTS (one per SKU) ===")
    for sku in sorted(store.restock):
        print(f"  {sku} {store.restock[sku]}")

    print("\n=== EXPIRING WITHIN 3 DAYS (bisect on a sorted list) ===")
    cut = bisect.bisect_right(store.expiry_index, (d(3), "￿"))
    for when, sku in store.expiry_index[:cut]:
        print(f"  {when} -> {store.stock[sku].name} (qty {store.stock[sku].qty})  => mark down 30%")

    print("\n=== RECENTLY VIEWED (deque maxlen=3) ===")
    for sku in ["CHP1", "COK2", "MLK1", "CHP1", "BAN6"]:
        store.view(sku)
        print(f"  viewed {sku} -> {list(store.recent)}")

    print("\n=== STOCK VALUE BY CATEGORY (Counter) ===")
    value: defaultdict[str, float] = defaultdict(float)
    for it in store.stock.values():
        value[it.category] += it.price * it.qty
    for cat in sorted(value):
        print(f"  {cat:<10} Rs {value[cat]:>9,.2f}")
    print(f"  most valuable: {max(value, key=lambda c: value[c])}")


if __name__ == "__main__":
    main()
