"""
============================================================================
 SECTION 08 : REAL-WORLD APP - Ride-hailing fare engine (Python edition)
============================================================================

  Vehicle            fare(route) is polymorphic
    |- Bike
    |- AutoRickshaw  night surcharge
    |- Cab           has-a Driver, AC night rule
        |- PremiumCab  airport fee on top of Cab

  Ride  HAS-A Rider, Vehicle, Route  (composition, as a dataclass)
  FareEngine only knows "something with .fare(route)" -> duck typing means a
  brand-new class (e.g. a Metro feeder bus) plugs in WITHOUT inheriting Vehicle.

 Python bits to notice: dataclasses for Rider/Route/Driver, property with
 validation for surge, class attributes for per-type pricing (no constructor
 boilerplate), super().fare() to extend parent logic, min(..., key=...).

 HOW TO RUN:  python3 ride_hailing_app.py
============================================================================
"""
from dataclasses import dataclass
from typing import Protocol


@dataclass(frozen=True)
class Rider:
    name: str
    has_subscription: bool = False


@dataclass(frozen=True)
class Route:
    origin: str
    dest: str
    km: float
    minutes: int
    night: bool = False
    airport: bool = False


@dataclass(frozen=True)
class Driver:
    name: str
    rating: float


class Vehicle:
    label, base, per_km, per_min, seats = "Vehicle", 0.0, 0.0, 0.0, 4   # class attributes = per-type config

    def fare(self, r: Route) -> float:
        return self.base + self.per_km * r.km + self.per_min * r.minutes


class Bike(Vehicle):
    label, base, per_km, per_min, seats = "Bike", 20, 6, 0.5, 1


class AutoRickshaw(Vehicle):
    label, base, per_km, per_min, seats = "Auto", 30, 12, 1.0, 3

    def fare(self, r: Route) -> float:
        f = super().fare(r)
        return f * 1.5 if r.night else f


class Cab(Vehicle):
    label, base, per_km, per_min = "Cab", 50, 14, 1.5

    def __init__(self, driver: Driver):
        self.driver = driver                         # has-a

    def fare(self, r: Route) -> float:
        f = super().fare(r)
        return f * 1.2 if r.night else f


class PremiumCab(Cab):
    label, base, per_km, per_min = "Premier", 90, 20, 2.0

    def fare(self, r: Route) -> float:
        return super().fare(r) + (150 if r.airport else 0)


class FeederBus:                                     # NOT a Vehicle - duck typing still works
    label, seats = "Feeder bus", 40

    def fare(self, r: Route) -> float:
        return 10 if r.km <= 5 else 25


class Priced(Protocol):                              # "anything with .fare(route)" - checked by mypy
    def fare(self, r: Route) -> float: ...


@dataclass
class Ride:
    rider: Rider
    vehicle: Priced                                  # duck typing, but type-checked
    route: Route


class FareEngine:
    PLATFORM_FEE, GST = 8, 0.05

    def __init__(self):
        self._surge = 1.0

    @property
    def surge(self) -> float:
        return self._surge

    @surge.setter
    def surge(self, multiplier: float) -> None:      # validated assignment
        if not 1.0 <= multiplier <= 3.0:
            raise ValueError("surge must be 1.0 - 3.0")
        self._surge = multiplier

    def quote(self, ride: Ride) -> int:
        fare = ride.vehicle.fare(ride.route) * self._surge    # polymorphic / duck-typed call
        if ride.rider.has_subscription:
            fare *= 0.9
        return round((fare + self.PLATFORM_FEE) * (1 + self.GST))


def main() -> None:
    engine = FareEngine()
    srikar, meera = Rider("Srikar"), Rider("Meera", has_subscription=True)
    college = Route("SRM KTR", "Guindy", 24.5, 55)
    airport_night = Route("T Nagar", "Chennai Airport", 16, 40, night=True, airport=True)
    short_hop = Route("Metro station", "Office park", 4, 15)
    fleet = [Bike(), AutoRickshaw(), Cab(Driver("Kumar", 4.8)), PremiumCab(Driver("Anita", 4.95))]

    for r in (college, airport_night):
        tags = ", night" * r.night + ", airport" * r.airport
        print(f"=== {r.origin} -> {r.dest} ({r.km} km, {r.minutes} min{tags}) ===")
        quotes: dict[str, int] = {}
        for v in fleet:
            price = engine.quote(Ride(srikar, v, r))
            quotes[v.label] = price
            extra = f"  driver {v.driver.name} {v.driver.rating}*" if isinstance(v, Cab) else ""
            print(f"  {v.label:<8} seats {v.seats}  Rs {price:5d}{extra}")
        print(f"  cheapest: {min(quotes, key=lambda k: quotes[k])}")

    print("\n=== DUCK TYPING: a class with no common parent ===")
    print(f"  {FeederBus.label}: Rs {engine.quote(Ride(srikar, FeederBus(), short_hop))} for {short_hop.km} km")

    print("\n=== SURGE + MEMBERSHIP ===")
    engine.surge = 1.8
    cab = fleet[2]
    print(f"  Cab, surge 1.8x, regular rider : Rs {engine.quote(Ride(srikar, cab, college))}")
    print(f"  Cab, surge 1.8x, member (-10%) : Rs {engine.quote(Ride(meera, cab, college))}")
    try:
        engine.surge = 7.5
    except ValueError as e:
        print(f"  engine.surge = 7.5 rejected: {e}")


if __name__ == "__main__":
    main()
