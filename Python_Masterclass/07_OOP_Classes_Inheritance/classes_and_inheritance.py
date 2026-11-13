"""
============================================================================
 SECTION 07 : OOP PART 1 - classes, __init__, self, class vs instance
              attributes, properties, @classmethod/@staticmethod, dataclasses,
              inheritance, super(), dunder methods, str methods
              (Python track - compare with ClassesAndInheritance.java)
============================================================================

 JAVA -> PYTHON CHEAT SHEET
   | Java                               | Python                                  |
   |------------------------------------|-----------------------------------------|
   | constructor Car(...)               | def __init__(self, ...)                 |
   | this                               | self (explicit first parameter)         |
   | fields declared in the class       | attributes created by assigning self.x  |
   | static field                       | class attribute (in the class body)     |
   | static method                      | @staticmethod                           |
   | static factory / extra constructor | @classmethod  (receives cls)            |
   | private / getters / setters        | _convention / @property                 |
   | toString()                         | __str__ (for users) / __repr__ (for devs)|
   | equals() / hashCode()              | __eq__ / __hash__                       |
   | record                             | @dataclass  (frozen=True for immutable) |
   | class B extends A                  | class B(A):   (multiple bases allowed)  |
   | super(...) / super.m()             | super().__init__(...) / super().m()     |
   | everything extends Object          | everything inherits from object         |
   | StringBuilder                      | "".join(parts)  or  io.StringIO         |

 HOW TO RUN:  python3 classes_and_inheritance.py
============================================================================
"""
import math
import time
from dataclasses import dataclass, field


# ============================ a plain class ============================
class Car:
    cars_built = 0                                   # CLASS attribute: shared (Java static)

    def __init__(self, make: str, model: str = "Generic"):   # defaults replace constructor overloads
        self.make = make                             # INSTANCE attributes
        self.model = model
        self._speed = 0                              # leading _ = "internal, please don't touch"
        Car.cars_built += 1

    @classmethod
    def from_string(cls, text: str) -> "Car":        # alternative constructor (Java: static factory)
        make, model = text.split("-")
        return cls(make, model)

    @staticmethod
    def kmph_to_mph(kmph: float) -> float:           # utility, needs neither self nor cls
        return kmph * 0.621371

    def accelerate(self, delta: int) -> None:
        self._speed = max(0, min(180, self._speed + delta))

    @property
    def speed(self) -> int:                          # read-only attribute (getter, no setter)
        return self._speed

    def __str__(self) -> str:                        # print(car)
        return f"{self.make} {self.model} @ {self._speed} km/h"

    def __repr__(self) -> str:                       # debugging / REPL view
        return f"Car({self.make!r}, {self.model!r})"


# ============================ inheritance ============================
class Employee:
    def __init__(self, name: str, base_salary: float):
        self.name = name
        self.base_salary = base_salary

    def monthly_pay(self) -> float:
        return self.base_salary / 12

    def describe(self) -> str:
        return f"{type(self).__name__:<8} {self.name:<9} pays Rs {self.monthly_pay():>12,.2f}/month"


class Manager(Employee):
    def __init__(self, name: str, base_salary: float, bonus_percent: float):
        super().__init__(name, base_salary)          # parent initialiser
        self.bonus_percent = bonus_percent

    def monthly_pay(self) -> float:                  # override (no @Override annotation needed)
        return super().monthly_pay() * (1 + self.bonus_percent / 100)


class Intern(Employee):
    def __init__(self, name: str, stipend_per_month: float):
        super().__init__(name, stipend_per_month * 12)

    def describe(self) -> str:
        return super().describe() + "  (stipend)"


# ============================ equality by hand ============================
class Point:
    def __init__(self, x: int, y: int):
        self.x, self.y = x, y

    def __eq__(self, other: object) -> bool:
        if not isinstance(other, Point):
            return NotImplemented                    # let Python try the other side
        return (self.x, self.y) == (other.x, other.y)

    def __hash__(self) -> int:                       # equal objects MUST hash equal
        return hash((self.x, self.y))


# ============================ dataclass = Java record ============================
@dataclass(frozen=True)                              # frozen -> immutable, hashable
class GeoPoint:
    lat: float
    lon: float

    def __post_init__(self):                         # like a compact constructor
        if not -90 <= self.lat <= 90:
            raise ValueError(f"bad latitude {self.lat}")

    def distance_to(self, o: "GeoPoint") -> float:
        r = 6371
        d_lat, d_lon = math.radians(o.lat - self.lat), math.radians(o.lon - self.lon)
        a = math.sin(d_lat / 2) ** 2 + math.cos(math.radians(self.lat)) * math.cos(math.radians(o.lat)) * math.sin(d_lon / 2) ** 2
        return 2 * r * math.asin(math.sqrt(a))


@dataclass
class Course:                                        # mutable dataclass with a default list
    title: str
    hours: float
    tags: list[str] = field(default_factory=list)   # never use tags: list = [] (shared default!)


def main() -> None:
    print("=== CLASSES, OBJECTS, REFERENCES ===")
    a = Car("Tata", "Nexon")
    b = Car.from_string("Mahindra-Thar")
    alias = a
    alias.accelerate(60)
    print(f"  a = {a}   (changed through 'alias')")
    print(f"  b = {b!r}  <- __repr__")
    print(f"  a is alias: {a is alias}, cars built (class attribute): {Car.cars_built}")
    print(f"  staticmethod: 100 km/h = {Car.kmph_to_mph(100):.1f} mph")
    try:
        a.speed = 999  # type: ignore[misc]  # deliberate: property without a setter
    except AttributeError as e:
        print(f"  a.speed = 999 -> AttributeError: {e}")
    print(f"  attributes live in a dict: {vars(a)}")

    print("\n=== INHERITANCE & OVERRIDING ===")
    for emp in (Employee("Asha", 9_00_000), Manager("Ravi", 18_00_000, 20), Intern("Srikar", 25_000)):
        print("  " + emp.describe())
    print(f"  Manager.__mro__ = {[c.__name__ for c in Manager.__mro__]}  (method lookup order)")
    print(f"  isinstance(Manager(...), Employee) = {isinstance(Manager('x', 1, 1), Employee)}, issubclass(Intern, object) = {issubclass(Intern, object)}")

    print("\n=== __eq__ / __hash__ ===")
    p1, p2 = Point(3, 4), Point(3, 4)
    print(f"  p1 is p2: {p1 is p2}   p1 == p2: {p1 == p2}   same hash: {hash(p1) == hash(p2)}   set size: {len({p1, p2})}")

    print("\n=== DATACLASSES (Java records) ===")
    chennai, goa = GeoPoint(13.0827, 80.2707), GeoPoint(15.4909, 73.8278)
    print(f"  {chennai}  (auto __repr__)")
    print(f"  Chennai -> Goa straight-line distance: {chennai.distance_to(goa):.0f} km")
    print(f"  equality by value: {chennai == GeoPoint(13.0827, 80.2707)}")
    try:
        GeoPoint(95, 0)
    except ValueError as e:
        print(f"  __post_init__ rejected: {e}")
    try:
        chennai.lat = 0  # type: ignore[misc]  # deliberate: frozen dataclass
    except Exception as e:
        print(f"  frozen dataclass: {type(e).__name__}")
    c1, c2 = Course("Java", 135.5), Course("Python", 40)
    c1.tags.append("oop")
    print(f"  default_factory gives each object its own list: {c1.tags} vs {c2.tags}")

    print("\n=== STRING METHODS (str is immutable) ===")
    s = "  Java Masterclass 2025  "
    print(f"  strip(): {s.strip()!r}  upper(): {s.strip().upper()}  title(): {'hello world'.title()}")
    print(f"  find('Master') = {s.strip().find('Master')}, slicing [5:11] = {s.strip()[5:11]!r}")
    print(f"  replace: {s.strip().replace('2025', '2026')}, 'Java' in s: {'Java' in s}, startswith: {s.strip().startswith('Java')}")
    print(f"  split/join: {' | '.join('a,b,c'.split(','))}, partition: {'key=value'.partition('=')}")
    print(f"  formatting: {'id':<6}|{9.87:5.1f}|{7:03d}|{0.256:.1%}|{1234567:,}")

    print("\n=== BUILDING STRINGS: += vs join (Java String vs StringBuilder) ===")
    t0 = time.perf_counter()
    slow = ""
    for i in range(20_000):
        slow += str(i)
    t1 = time.perf_counter()
    fast = "".join(str(i) for i in range(20_000))
    t2 = time.perf_counter()
    print(f"  += : {(t1 - t0) * 1000:.1f} ms, join: {(t2 - t1) * 1000:.1f} ms, same: {slow == fast}")
    print("  (CPython optimises += in simple loops, but join is the portable, idiomatic choice)")


if __name__ == "__main__":
    main()
