"""
============================================================================
 SECTION 12 : GENERICS -> PYTHON TYPE HINTS: TypeVar, Generic, bounds,
              constrained types, Protocols as bounds, variance, erasure
              (Python track - compare with GenericsDeepDive.java)
============================================================================

 THE BIG DIFFERENCE
   Java generics are checked by the COMPILER and erased at runtime.
   Python generics are ONLY hints: the interpreter ignores them completely.
   A static checker (mypy, pyright - also built into VS Code / PyCharm) reads
   them and reports the same kinds of errors javac would.
       box: Box[int] = Box("text")   # runs fine; mypy: incompatible type

 SYNTAX (Python 3.12+ has a shorter form; this file uses the 3.9+ form)
   | Java                                  | Python                                    |
   |---------------------------------------|-------------------------------------------|
   | class Box<T>                          | T = TypeVar("T"); class Box(Generic[T])   |
   |                                       | 3.12+: class Box[T]:                      |
   | static <T> T first(List<T> xs)        | def first(xs: list[T]) -> T               |
   | <T extends Number>                    | TypeVar("N", bound=Number-like Protocol)  |
   | <T extends Comparable<T>>             | TypeVar("C", bound=SupportsLessThan)      |
   | only String or Integer                | TypeVar("S", str, int)  (constrained)     |
   | List<? extends Number> (read-only)    | Sequence[float]  (covariant, read-only)   |
   | List<? super Integer>  (write)        | callable params are contravariant         |
   | List<?>                               | list[Any] / Sequence[object]              |
   | Map<K, V>                             | dict[K, V]                                |
   | Optional<T>                           | T | None                                  |

 HOW TO RUN:  python3 generics_typing.py      (and try:  mypy generics_typing.py)
============================================================================
"""
from collections.abc import Callable, Iterable, Sequence
from dataclasses import dataclass
from typing import Any, Generic, Protocol, TypeVar, get_type_hints

T = TypeVar("T")
R = TypeVar("R")
K = TypeVar("K")
V = TypeVar("V")


class SupportsLessThan(Protocol):
    def __lt__(self, other: Any, /) -> bool: ...


C = TypeVar("C", bound=SupportsLessThan)          # Java: <C extends Comparable<C>>
S = TypeVar("S", str, bytes)                       # constrained: exactly str OR bytes


# ======================== generic class ========================
class Box(Generic[T]):
    def __init__(self, value: T):
        self.value = value

    def get(self) -> T:
        return self.value

    def map(self, fn: Callable[[T], R]) -> "Box[R]":
        return Box(fn(self.value))

    def __repr__(self) -> str:
        return f"Box[{self.value!r}]"


@dataclass(frozen=True)
class Pair(Generic[K, V]):
    key: K
    value: V

    def with_value(self, v: R) -> "Pair[K, R]":
        return Pair(self.key, v)


# ======================== bounded / constrained ========================
def maximum(items: Iterable[C]) -> C:
    it = iter(items)
    best = next(it)
    for x in it:
        if best < x:
            best = x
    return best


def concat(a: S, b: S) -> S:                       # str+str or bytes+bytes, never mixed
    return a + b


class Stats:
    @staticmethod
    def mean(values: Sequence[float]) -> float:    # Sequence is covariant: list[int] is accepted
        return sum(values) / len(values) if values else 0.0


def first_or(items: Sequence[T], default: T) -> T:
    return items[0] if items else default


def main() -> None:
    print("=== GENERIC CLASS ===")
    name: Box[str] = Box("Srikar")
    length: Box[int] = name.map(len)
    print(f"  {name} -> map(len) -> {length}, arithmetic needs no cast: {length.get() + 1}")
    wrong: Box[int] = Box("not an int")  # type: ignore[arg-type]  # deliberate: mypy error, runtime silence
    print(f"  Box[int] holding a str at runtime: {wrong}  <- hints are NOT enforced; run mypy to catch it")

    print("\n=== TWO TYPE PARAMETERS ===")
    score: Pair[str, int] = Pair("Java", 94)
    print(f"  {score} -> {score.with_value('A+')}")

    print("\n=== BOUNDED & CONSTRAINED TypeVars ===")
    print(f"  maximum([4, 19, 7]) = {maximum([4, 19, 7])}, maximum(strings) = {maximum(['pear', 'apple', 'zucchini'])}")
    print(f"  concat('ab', 'cd') = {concat('ab', 'cd')}, concat(b'x', b'y') = {concat(b'x', b'y')!r}")
    try:
        concat("ab", b"cd")  # type: ignore[type-var]  # deliberate: mypy flags it, runtime raises
    except TypeError as e:
        print(f"  concat('ab', b'cd') -> TypeError at runtime too: {e}")

    print("\n=== VARIANCE (Java wildcards) ===")
    ints: list[int] = [1, 2, 3]
    print(f"  Stats.mean(list[int]) = {Stats.mean(ints)}  (Sequence[float] is covariant & read-only, like List<? extends Number>)")
    print("  list[int] would NOT be accepted where list[float] is required - list is invariant, like Java's List<Integer> vs List<Number>")
    print(f"  first_or([], 'none') = {first_or([], 'none')!r}, first_or([3, 4], 0) = {first_or([3, 4], 0)}")

    print("\n=== 'ERASURE': hints exist only as metadata ===")
    print(f"  type(Box[int]('x')) is Box: {type(Box[str]('x')) is Box}")
    print(f"  but the hints are inspectable: {get_type_hints(Stats.mean)}")
    print("  This metadata powers dataclasses, pydantic and FastAPI validation - they READ the hints at runtime.")


if __name__ == "__main__":
    main()
