"""
============================================================================
 SECTION 11 : ABSTRACTION - abstract base classes (ABC), Protocols,
              mixins, multiple inheritance and the MRO
              (Python track - compare with AbstractionInterfaces.java)
============================================================================

 JAVA -> PYTHON
   | Java                         | Python                                      |
   |------------------------------|---------------------------------------------|
   | abstract class Animal        | class Animal(ABC):                          |
   | abstract String sound();     | @abstractmethod  def sound(self): ...       |
   | interface Flyer (nominal:    | class Flyer(ABC) - nominal, must inherit    |
   |   must say 'implements')     | class Flyer(Protocol) - STRUCTURAL: any     |
   |                              |   class with matching methods qualifies,    |
   |                              |   checked by mypy/pyright (duck typing,     |
   |                              |   but verified)                             |
   | default methods              | ordinary methods in the ABC / mixin         |
   | implements A, B, C           | class X(Base, A, B, C) - real multiple      |
   |                              |   inheritance, resolved by the MRO (C3)     |
   | constants in interfaces      | class attributes                            |

 ABC: instantiating a class with unimplemented @abstractmethods raises
 TypeError - at object-creation time, not compile time.

 MIXIN: a small class that adds ONE capability (e.g. JsonMixin.to_json) and
 is meant to be combined with others, never used alone.

 MRO (method resolution order): ClassName.__mro__ lists the order Python
 searches for a method. super() follows the MRO, which is what makes
 cooperative multiple inheritance (the "diamond") work.

 HOW TO RUN:  python3 abstraction_protocols.py
============================================================================
"""
import json
from abc import ABC, abstractmethod
from typing import Protocol, runtime_checkable


# ======================== abstract base class ========================
class Animal(ABC):
    def __init__(self, name: str):
        self.name = name

    @abstractmethod
    def sound(self) -> str: ...

    @abstractmethod
    def daily_food_kg(self) -> float: ...

    def feeding_note(self) -> str:              # concrete, shared by all subclasses
        return f"{self.name} says {self.sound()} and eats {self.daily_food_kg():.1f} kg/day"


class Dog(Animal):
    def sound(self) -> str:
        return "Woof"

    def daily_food_kg(self) -> float:
        return 0.6


class Elephant(Animal):
    def sound(self) -> str:
        return "Trumpet"

    def daily_food_kg(self) -> float:
        return 150


class HalfDone(Animal):                         # forgot daily_food_kg
    def sound(self) -> str:
        return "?"


# ======================== nominal "interface" via ABC ========================
class Flyer(ABC):
    CRUISE_ALTITUDE_M = 1_000                   # constant

    @abstractmethod
    def take_off(self) -> str: ...

    def land(self) -> str:                      # 'default method'
        return f"{self} lands using the default routine"


class Bird(Animal, Flyer):                      # extends one class AND 'implements' Flyer
    def sound(self) -> str:
        return "Tweet"

    def daily_food_kg(self) -> float:
        return 0.05

    def take_off(self) -> str:
        return f"{self.name} flaps wings and takes off"

    def __str__(self) -> str:
        return self.name


# ======================== structural typing: Protocol ========================
@runtime_checkable
class Trackable(Protocol):
    def location(self) -> str: ...


class Drone:                                    # does NOT inherit Trackable...
    def __init__(self, serial: str, battery: int):
        self.serial, self.battery = serial, battery

    def location(self) -> str:                  # ...but has the right method
        return "13.0827N, 80.2707E @ 1000 m"


class Parcel:
    def location(self) -> str:
        return "Chennai sorting hub"


def track(item: Trackable) -> str:              # a type checker accepts Drone and Parcel
    return f"tracking {type(item).__name__} -> {item.location()}"


# ======================== mixins + MRO ========================
class JsonMixin:
    def to_json(self) -> str:
        return json.dumps({k: v for k, v in vars(self).items() if not k.startswith("_")})


class ComparableByMixin:
    sort_key = "id"

    def __lt__(self, other) -> bool:
        return getattr(self, self.sort_key) < getattr(other, self.sort_key)


class Product(JsonMixin, ComparableByMixin):
    sort_key = "price"

    def __init__(self, id: int, name: str, price: float):
        self.id, self.name, self.price = id, name, price


# The diamond: both B and C extend A; D extends B and C
class A:
    def hello(self) -> str:
        return "A"


class B(A):
    def hello(self) -> str:
        return "B>" + super().hello()


class C(A):
    def hello(self) -> str:
        return "C>" + super().hello()


class D(B, C):
    def hello(self) -> str:
        return "D>" + super().hello()


def main() -> None:
    print("=== ABSTRACT BASE CLASSES ===")
    for a in (Dog("Bruno"), Elephant("Ganesh"), Bird("Tweety")):
        print("  " + a.feeding_note())
    for cls in (Animal, HalfDone):
        try:
            cls("x")  # type: ignore[abstract]  # deliberate: abstract classes cannot be instantiated
        except TypeError as e:
            print(f"  {cls.__name__}('x') -> TypeError: {e}")

    print("\n=== ABC AS AN INTERFACE ===")
    rio = Bird("Rio")
    print(f"  {rio.take_off()}; {rio.land()}; isinstance(rio, Flyer) = {isinstance(rio, Flyer)}")

    print("\n=== PROTOCOLS: structural typing ===")
    for thing in (Drone("DX-9", 18), Parcel()):
        print(f"  {track(thing)}   isinstance(..., Trackable) = {isinstance(thing, Trackable)}")
    print("  neither class mentions Trackable - matching methods are enough")

    print("\n=== MIXINS ===")
    items = [Product(3, "Keyboard", 2499), Product(1, "Mouse", 699), Product(2, "Monitor", 10999)]
    print(f"  JsonMixin.to_json(): {items[0].to_json()}")
    print(f"  sorted() via ComparableByMixin (by price): {[p.name for p in sorted(items)]}")
    print(f"  Product MRO: {[c.__name__ for c in Product.__mro__]}")

    print("\n=== THE DIAMOND & super() ===")
    print(f"  D().hello() = {D().hello()}   (A runs ONCE)")
    print(f"  D.__mro__   = {[c.__name__ for c in D.__mro__]}")
    print("  super() means 'next class in the MRO', not 'my parent' - that's how C gets called from B")


if __name__ == "__main__":
    main()
