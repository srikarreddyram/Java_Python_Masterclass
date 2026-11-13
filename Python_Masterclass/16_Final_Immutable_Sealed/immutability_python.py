"""
============================================================================
 SECTION 16 : IMMUTABILITY, "final", OBJECT CREATION ORDER, SINGLETONS AND
              "SEALED" HIERARCHIES IN PYTHON
              (Python track - compare with FinalImmutableSealed.java)
============================================================================

 final -> PYTHON
   | Java                         | Python                                         |
   |------------------------------|------------------------------------------------|
   | final local / field          | typing.Final (checker-enforced), naming        |
   |                              | convention UPPER_CASE for constants            |
   | final method / final class   | @typing.final (checker-enforced);              |
   |                              | __init_subclass__ can block subclassing AT RUNTIME |
   | immutable class recipe       | @dataclass(frozen=True), NamedTuple, tuple,     |
   |                              | frozenset, __slots__ + read-only properties    |
   | defensive copy               | tuple(list_arg), MappingProxyType, copy.deepcopy|
   | static / instance initialiser| code in the CLASS BODY runs once at definition; |
   |                              | __new__ builds the object, __init__ fills it   |
   | enum singleton               | Enum member / module-level object              |
   | sealed + pattern matching    | a Union type + match/case with assert_never    |
   |                              | (mypy/pyright prove exhaustiveness)            |

 IMMUTABLE vs MUTABLE built-ins
   immutable: int, float, str, bytes, tuple, frozenset, None, bool
   mutable  : list, dict, set, bytearray, most user classes
 A tuple is shallowly immutable: (1, [2, 3]) still holds a mutable list.

 OBJECT CREATION ORDER (class Child(Parent))
   1. class bodies run when the 'class' statement executes (once, at import)
   2. Parent.__init_subclass__(Child) runs when Child is defined
   3. Child() -> type.__call__ -> __new__ (allocate) -> __init__ (initialise)

 HOW TO RUN:  python3 immutability_python.py
============================================================================
"""
import copy
from dataclasses import FrozenInstanceError, dataclass, field, replace
from decimal import Decimal
from enum import Enum
from types import MappingProxyType
from typing import Final, NamedTuple, final

GST_RATE: Final = Decimal("0.18")               # mypy reports any reassignment


# ======================== frozen value object ========================
@dataclass(frozen=True)
class Money:
    paise: int

    @classmethod
    def of(cls, rupees: str) -> "Money":
        return cls(int(Decimal(rupees) * 100))

    def __add__(self, other: "Money") -> "Money":
        return Money(self.paise + other.paise)

    def times(self, factor: str) -> "Money":
        return Money(int((Decimal(self.paise) * Decimal(factor)).to_integral_value()))

    def __str__(self) -> str:
        return f"Rs {self.paise / 100:,.2f}"


# ======================== defensive copies ========================
@dataclass(frozen=True)
class Invoice:
    number: str
    lines: tuple[str, ...]                       # tuple, not list
    meta: MappingProxyType = field(default_factory=lambda: MappingProxyType({}))

    @classmethod
    def create(cls, number: str, lines: list[str], meta: dict) -> "Invoice":
        return cls(number, tuple(lines), MappingProxyType(dict(meta)))   # copy IN

    def with_line(self, line: str) -> "Invoice":
        return replace(self, lines=self.lines + (line,))                  # new object


class LeakyInvoice:
    def __init__(self, lines: list[str]):
        self.lines = lines                       # keeps the caller's list


# ======================== __slots__: fixed attribute set, less memory ========================
class Sensor:
    __slots__ = ("_id", "_unit")                 # no __dict__ -> can't add random attributes

    def __init__(self, sensor_id: str, unit: str):
        self._id, self._unit = sensor_id, unit

    @property
    def id(self) -> str:                         # read-only
        return self._id


# ======================== blocking subclassing at runtime ========================
class NoSubclass:
    def __init_subclass__(cls, **kwargs):
        raise TypeError(f"{cls.__name__} cannot extend a final class")


@final
class Leaf:                                       # @final: type checkers reject subclasses
    pass


# ======================== creation order ========================
trace: list[str] = []


class Parent:
    trace.append("1:Parent class body")           # runs ONCE, when the class statement executes

    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        trace.append(f"2:Parent.__init_subclass__({cls.__name__})")

    def __new__(cls, *args, **kwargs):
        trace.append("4:Parent.__new__")
        return super().__new__(cls)

    def __init__(self):
        trace.append("6:Parent.__init__")


class Child(Parent):
    trace.append("1b:Child class body")
    CONFIG: Final = MappingProxyType({"db": "prod", "retries": 3})    # 'static final' map

    def __new__(cls):
        trace.append("3:Child.__new__")
        return super().__new__(cls)

    def __init__(self):
        trace.append("5:Child.__init__ (before super)")
        super().__init__()
        trace.append("7:Child.__init__ (after super)")


# ======================== singleton via Enum ========================
class AppConfig(Enum):
    INSTANCE = MappingProxyType({"env": "prod", "region": "ap-south-1"})

    def get(self, key: str) -> str:
        return self.value.get(key, "?")


def main() -> None:
    print("=== CONSTANTS & Final ===")
    print(f"  GST_RATE: Final = {GST_RATE}  (rebinding it runs, but mypy/pyright report an error)")
    names: Final = ["Srikar"]
    names.append("Meera")
    print(f"  Final list can still be mutated: {names}  (same as Java's final reference)")

    print("\n=== FROZEN VALUE OBJECTS ===")
    price = Money.of("1299.99")
    total = price.times("3") + Money.of("49")
    print(f"  price {price} x3 + delivery = {total}  (price unchanged: {price})")
    print(f"  usable as a dict key: {({Money.of('499'): 'free delivery threshold'})[Money.of('499')]}")
    try:
        price.paise = 1  # type: ignore[misc]  # deliberate: frozen dataclass
    except FrozenInstanceError:
        print("  price.paise = 1 -> FrozenInstanceError")
    print(f"  Decimal-based: Money.of('0.1') + Money.of('0.2') = {Money.of('0.1') + Money.of('0.2')}")

    print("\n=== DEFENSIVE COPIES ===")
    source, meta = ["Laptop x1"], {"gstin": "33ABCDE1234F1Z5"}
    inv = Invoice.create("INV-7", source, meta)
    leaky = LeakyInvoice(source)
    source.append("HACKED x999")
    meta["gstin"] = "tampered"
    print(f"  immutable invoice: {inv.lines}, meta {dict(inv.meta)}")
    print(f"  leaky invoice    : {leaky.lines}  <- changed behind its back")
    try:
        inv.meta["gstin"] = "x"  # type: ignore[index]  # deliberate: read-only mapping
    except TypeError:
        print("  inv.meta is a read-only MappingProxyType")
    print(f"  with_line -> new object {inv.with_line('Mouse x1').lines}, original {inv.lines}")
    shallow = (1, [2, 3])
    shallow[1].append(4)
    print(f"  a tuple holding a list is only SHALLOWLY immutable: {shallow}; deepcopy breaks the link: {copy.deepcopy(shallow) is not shallow}")

    print("\n=== __slots__ ===")
    s = Sensor("T-01", "C")
    try:
        s.color = "red"  # type: ignore[attr-defined]  # deliberate: __slots__ blocks new attributes
    except AttributeError as e:
        print(f"  s.color = 'red' -> AttributeError: {e}")

    print("\n=== FINAL CLASSES ===")
    try:
        class Sub(NoSubclass):                     # noqa: F841 - deliberately rejected
            pass
    except TypeError as e:
        print(f"  runtime-enforced: {e}")
    print(f"  @typing.final on Leaf: class X(Leaf) runs, but type checkers reject it ({Leaf.__name__} is marked final)")

    print("\n=== CREATION ORDER ===")
    print(f"  at class definition: {trace}")
    trace.clear()
    Child()
    print(f"  Child():             {trace}")
    print(f"  Child.CONFIG (read-only): {dict(Child.CONFIG)}")

    print("\n=== ENUM SINGLETON ===")
    print(f"  AppConfig.INSTANCE.get('region') = {AppConfig.INSTANCE.get('region')}, "
          f"same object: {AppConfig.INSTANCE is AppConfig['INSTANCE']}")

    print("\n=== NAMEDTUPLE ===")
    class Point(NamedTuple):
        x: float
        y: float
    p = Point(3, 4)
    print(f"  {p}, hashable {hash(p) == hash(Point(3, 4))}, _replace -> {p._replace(x=0)}")


if __name__ == "__main__":
    main()
