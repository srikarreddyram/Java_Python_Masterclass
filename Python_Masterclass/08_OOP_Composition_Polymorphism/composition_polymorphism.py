"""
============================================================================
 SECTION 08 : OOP PART 2 - composition, encapsulation, polymorphism,
              duck typing, operator overloading
              (Python track - compare with CompositionPolymorphism.java)
============================================================================

 COMPOSITION ("has-a") - identical idea to Java: a Smartphone HAS-A Battery.

 ENCAPSULATION IN PYTHON
   Python has no private keyword. Instead:
     _name   : "internal" by convention (tools and readers respect it)
     __name  : NAME MANGLING -> stored as _ClassName__name, avoids clashes
               in subclasses (not real security)
     @property + @x.setter : keep attribute syntax (p.health) while running
               validation code - you can start with a plain attribute and add
               a property later WITHOUT changing callers (no getter/setter
               boilerplate up front, unlike Java)

 POLYMORPHISM
   - Overriding works exactly like Java (runtime dispatch).
   - DUCK TYPING: "if it walks like a duck..." - a function works with ANY
     object that has the needed methods; no common base class or interface
     required. (Protocols in Section 11 let a type checker verify this.)
   - Operator overloading via dunder methods: __add__, __lt__, __len__,
     __getitem__... so your objects work with +, <, len(), [], sorted().

 HOW TO RUN:  python3 composition_polymorphism.py
============================================================================
"""
import math
from functools import total_ordering


# ============================ COMPOSITION ============================
class Battery:
    def __init__(self, capacity_mah: int):
        self.capacity_mah = capacity_mah
        self.charge = 100

    def drain(self, percent: int) -> None:
        self.charge = max(0, self.charge - percent)


class Camera:
    def __init__(self, megapixels: int):
        self.megapixels = megapixels

    def shoot(self, subject: str) -> str:
        return f"{self.megapixels}MP photo of {subject}"


class Smartphone:
    def __init__(self, model: str, battery: Battery, camera: Camera):
        self.model, self.battery, self.camera = model, battery, camera   # HAS-A

    def take_photo(self, subject: str) -> str:                           # delegate to parts
        if self.battery.charge < 5:
            return f"{self.model}: battery too low"
        self.battery.drain(2)
        return f"{self.model} -> {self.camera.shoot(subject)}"


# ============================ ENCAPSULATION ============================
class Player:
    def __init__(self, name: str):
        self.name = name
        self._health = 100
        self.__lives = 3                     # name-mangled to _Player__lives

    @property
    def health(self) -> int:
        return self._health

    @health.setter
    def health(self, value: int) -> None:    # validation runs on  p.health = ...
        self._health = max(0, min(100, value))

    @property
    def lives(self) -> int:
        return self.__lives

    def take_damage(self, dmg: int) -> None:
        if dmg <= 0:
            return
        self._health -= dmg
        if self._health <= 0:
            self.__lives -= 1
            self._health = 100 if self.__lives > 0 else 0

    def __str__(self) -> str:
        return f"{self.name} [health {self._health}, lives {self.__lives}]"


# ============================ POLYMORPHISM ============================
class Shape:
    def name(self) -> str:
        return "shape"

    def area(self) -> float:
        return 0.0


class Circle(Shape):
    def __init__(self, r: float):
        self.r = r

    def name(self) -> str:
        return f"circle r={self.r}"

    def area(self) -> float:
        return math.pi * self.r ** 2


class Rectangle(Shape):
    def __init__(self, w: float, h: float):
        self.w, self.h = w, h

    def name(self) -> str:
        return f"rectangle {self.w}x{self.h}"

    def area(self) -> float:
        return self.w * self.h


class Square(Rectangle):
    def __init__(self, side: float):
        super().__init__(side, side)

    def name(self) -> str:
        return f"square {self.w}"

    def diagonal(self) -> float:
        return self.w * math.sqrt(2)


class Hexagon:                              # NOT a Shape subclass...
    def __init__(self, side: float):
        self.side = side

    def name(self) -> str:
        return f"hexagon s={self.side}"

    def area(self) -> float:                # ...but it has name() and area()
        return 3 * math.sqrt(3) / 2 * self.side ** 2


def total_area(things) -> float:            # duck typing: anything with .area() works
    return sum(t.area() for t in things)


# ============================ OPERATOR OVERLOADING ============================
@total_ordering                              # define __eq__ and __lt__, get <=, >, >= for free
class Money:
    def __init__(self, rupees: float):
        self.paise = round(rupees * 100)

    def __add__(self, other: "Money") -> "Money":
        return Money((self.paise + other.paise) / 100)

    def __mul__(self, factor: float) -> "Money":
        return Money(self.paise * factor / 100)

    __rmul__ = __mul__                       # so 3 * money works too

    def __eq__(self, other) -> bool:
        return isinstance(other, Money) and self.paise == other.paise

    def __lt__(self, other: "Money") -> bool:
        return self.paise < other.paise

    def __hash__(self) -> int:
        return hash(self.paise)

    def __bool__(self) -> bool:              # truthiness: zero money is falsy
        return self.paise != 0

    def __repr__(self) -> str:
        return f"Rs {self.paise / 100:,.2f}"


def main() -> None:
    print("=== COMPOSITION ===")
    phone = Smartphone("Pixel", Battery(4_700), Camera(50))
    print("  " + phone.take_photo("Marina Beach"))
    print(f"  battery now {phone.battery.charge}% of {phone.battery.capacity_mah:,} mAh")

    print("\n=== ENCAPSULATION ===")
    p = Player("Srikar")
    p.take_damage(70); print(f"  took 70 -> {p}")
    p.health = 500;    print(f"  p.health = 500 (setter clamps) -> {p}")
    p.take_damage(120); print(f"  took 120 (lost a life) -> {p}")
    try:
        print(p.__lives)
    except AttributeError:
        mangled = getattr(p, "_Player__lives")             # the name Python actually stored
        print(f"  p.__lives -> AttributeError; mangled name p._Player__lives = {mangled} (convention, not security)")

    print("\n=== POLYMORPHISM (dynamic dispatch, like Java) ===")
    shapes = [Circle(1.5), Rectangle(2, 5), Square(3), Shape()]
    for s in shapes:
        print(f"  {s.name():<16} area {s.area():6.2f}")

    print("\n=== isinstance checks / match on class patterns ===")
    for s in shapes:
        match s:
            case Square():
                print(f"  {s.name()} has diagonal {s.diagonal():.2f}")
            case Circle(r=r) if r > 1:
                print(f"  {s.name()} is a big circle")

    print("\n=== DUCK TYPING ===")
    mixed = [Circle(1), Square(2), Hexagon(1)]       # Hexagon shares NO base class
    print(f"  total_area of circle + square + hexagon = {total_area(mixed):.2f}")
    print(f"  isinstance(Hexagon(1), Shape) = {isinstance(Hexagon(1), Shape)} - it still worked")

    print("\n=== OPERATOR OVERLOADING ===")
    price, delivery = Money(1299.99), Money(49)
    total = price * 3 + delivery
    print(f"  {price} * 3 + {delivery} = {total};  3 * price = {3 * price}")
    print(f"  comparisons: {price} < {delivery}? {price < delivery};  max() = {max(price, delivery, total)}")
    print(f"  sorted: {sorted([total, delivery, price])};  bool(Money(0)) = {bool(Money(0))}")


if __name__ == "__main__":
    main()
