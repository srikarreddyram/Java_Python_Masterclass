"""
============================================================================
 SECTION 10 : REAL-WORLD APP - Road-trip Itinerary Planner (Python edition)
============================================================================

 Same trip as TravelItineraryApp.java. Python has no ListIterator with add(),
 so the idiomatic tools differ:
  - bisect.insort(stops, detour, key=...) keeps the list sorted by km on insert
    (Python 3.10+ key= argument)
  - filtering closed stops = a comprehension (never remove while iterating)
  - navigation = a small Navigator class with an index cursor
  - a GENERATOR yields driving legs between consecutive stops lazily
  - Enum members carry label + typical hours; a dict keyed by the enum
    replaces EnumMap

 HOW TO RUN:  python3 travel_itinerary_app.py
============================================================================
"""
import bisect
from dataclasses import dataclass
from enum import Enum
from itertools import pairwise


class Category(Enum):
    FOOD = ("Food stop", 1.0)
    SIGHTSEEING = ("Sightseeing", 2.5)
    FUEL = ("Fuel/charging", 0.5)
    STAY = ("Overnight", 10.0)

    def __init__(self, label: str, typical_hours: float):
        self.label, self.typical_hours = label, typical_hours


@dataclass(frozen=True)
class Stop:
    place: str
    km: int
    category: Category
    cost: int
    open_today: bool = True

    def __str__(self) -> str:
        return f"{self.place} (km {self.km})"


def show(title: str, stops: list[Stop]) -> None:
    print(f"=== {title} ===")
    for i, s in enumerate(stops, 1):
        flag = "" if s.open_today else "  [CLOSED TODAY]"
        print(f"  {i:2d}. {str(s):<34} {s.category.label:<13} Rs {s.cost:>5,}{flag}")


class Navigator:
    """Forward/back cursor over the itinerary (what ListIterator gave us in Java)."""
    def __init__(self, stops: list[Stop]):
        self.stops, self.i = stops, -1

    def next(self) -> Stop:
        self.i = min(self.i + 1, len(self.stops) - 1)
        return self.stops[self.i]

    def back(self) -> Stop:
        self.i = max(self.i - 1, 0)
        return self.stops[self.i]


def legs(stops: list[Stop]):
    """Generator: produce each driving leg only when asked."""
    for a, b in pairwise(stops):
        yield a.place, b.place, b.km - a.km


def main() -> None:
    trip = [
        Stop("Chennai (start)", 0, Category.FUEL, 0),
        Stop("Mahabalipuram", 58, Category.SIGHTSEEING, 600),
        Stop("Pondicherry", 150, Category.STAY, 3_500),
        Stop("Chidambaram Temple", 215, Category.SIGHTSEEING, 0, open_today=False),
        Stop("Tanjore", 330, Category.SIGHTSEEING, 200),
        Stop("Madurai", 480, Category.STAY, 2_800),
    ]
    show("ORIGINAL PLAN", trip)

    for detour in (Stop("Murugan Idli Shop", 470, Category.FOOD, 350),
                   Stop("EV charger, Kumbakonam", 280, Category.FUEL, 450),
                   Stop("Auroville", 160, Category.SIGHTSEEING, 0)):
        bisect.insort(trip, detour, key=lambda s: s.km)      # O(log n) search + O(n) insert
    show("WITH DETOURS (bisect.insort keeps km order)", trip)

    closed = [s for s in trip if not s.open_today]
    trip = [s for s in trip if s.open_today]                 # filter by rebuilding
    for s in closed:
        print(f"  removed closed stop: {s.place}")

    print("\n=== NAVIGATION ===")
    nav = Navigator(trip)
    for cmd in ["next", "next", "next", "back", "next", "next", "next"]:
        here = nav.next() if cmd == "next" else nav.back()
        print(f"  {cmd:<4} -> now at {here}")

    print("\n=== DRIVING LEGS (generator) ===")
    longest = max(legs(trip), key=lambda leg: leg[2])
    for a, b, km in legs(trip):
        print(f"  {a:<24} -> {b:<24} {km:4d} km")
    print(f"  longest leg: {longest[0]} -> {longest[1]} ({longest[2]} km)")

    print("\n=== BUDGET & TIME BY CATEGORY ===")
    summary = {c: [0, 0] for c in Category}                 # {category: [count, cost]}
    for s in trip:
        summary[s.category][0] += 1
        summary[s.category][1] += s.cost
    hours = 0.0
    for cat, (n, cost) in summary.items():
        if n:
            print(f"  {cat.label:<13} x{n}  Rs {cost:>6,}  ~{n * cat.typical_hours:.1f} h")
            hours += n * cat.typical_hours
    km = trip[-1].km
    print(f"  Total: {len(trip)} stops, {km} km, Rs {sum(s.cost for s in trip):,}, ~{hours:.1f} h at stops + {km / 50:.1f} h driving")

    highlights = [s for s in trip if s.category is Category.SIGHTSEEING]
    print(f"\n  Highlights: {[s.place for s in highlights]} -> slide 2 = {highlights[1].place}")


if __name__ == "__main__":
    main()
