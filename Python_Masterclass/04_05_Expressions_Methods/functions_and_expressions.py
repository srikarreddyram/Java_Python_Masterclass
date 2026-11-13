"""
============================================================================
 SECTIONS 04-05 : EXPRESSIONS, STATEMENTS, BLOCKS, if/elif/else, FUNCTIONS
                  (Python track - compare with MethodsAndExpressions.java)
============================================================================

 BLOCKS = INDENTATION
   Java uses { } ; Python uses a colon + consistent indentation (4 spaces).
   if, for, while, def, class, with, try all open an indented block.

 SCOPE (LEGB rule) - Python looks names up in this order:
   Local -> Enclosing function -> Global (module) -> Built-ins
   IMPORTANT: if/for/while blocks do NOT create a new scope (unlike Java's { }).
   A variable assigned inside an 'if' is visible after it.

 FUNCTIONS vs Java METHODS
   | Java                              | Python                                      |
   |-----------------------------------|---------------------------------------------|
   | static int add(int a, int b)      | def add(a: int, b: int) -> int:             |
   | overloading by parameter types    | NO overloading - use defaults, *args, or    |
   |                                   | functools.singledispatch                    |
   | int... varargs                    | *args (tuple)  and  **kwargs (dict)         |
   | -                                 | default values, keyword arguments,          |
   |                                   | keyword-only (after *) / positional-only (/)|
   | one return value                  | return a, b  -> returns a tuple             |
   | pass-by-value (copy of reference) | pass-by-object-reference: SAME idea         |

 THE MUTABLE DEFAULT TRAP: def f(x, items=[]) creates ONE list, shared by all
 calls. Use items=None and create the list inside.

 HOW TO RUN:  python3 functions_and_expressions.py
============================================================================
"""
import math
from functools import singledispatch


def grade(marks: int) -> str:
    """Early returns keep code flat (same pattern as Java)."""
    if not 0 <= marks <= 100:
        return "invalid"
    if marks >= 90:
        return "A+"
    if marks >= 75:
        return "A"
    if marks >= 60:
        return "B"
    if marks >= 40:
        return "C"
    return "F"


# ---------------- "overloading" the Python ways ----------------
def area(a: float, b: float | None = None, *, circle: bool = False) -> float:
    """One function, optional + keyword-only parameters, instead of 3 overloads."""
    if circle:
        return math.pi * a * a
    return a * a if b is None else a * b


@singledispatch
def describe(value) -> str:                      # dispatch on the TYPE of the first argument
    return f"something: {value!r}"


@describe.register
def _(value: int) -> str:
    return f"an int with {len(str(abs(value)))} digits"


@describe.register
def _(value: list) -> str:
    return f"a list of {len(value)} items"


# ---------------- *args and **kwargs ----------------
def total(*numbers: float) -> float:             # numbers is a TUPLE
    return sum(numbers)


def make_profile(name: str, /, age: int, *, city: str = "Chennai", **extra) -> dict:
    """name is positional-only (/), city is keyword-only (*), extra collects the rest."""
    return {"name": name, "age": age, "city": city, **extra}


def min_max_avg(values: list[float]) -> tuple[float, float, float]:
    return min(values), max(values), sum(values) / len(values)   # multiple return values (a tuple)


# ---------------- pass-by-object-reference ----------------
def try_to_change(number: int, items: list, text: str) -> None:
    number = 999                 # re-binds the LOCAL name only
    items.append(999)            # mutates the SHARED list object
    text += " (modified)"        # str is immutable -> creates a new local object


# ---------------- mutable default trap ----------------
def add_item_buggy(item, basket=[]):             # the [] is created ONCE, at def time
    basket.append(item)
    return basket


def add_item_fixed(item, basket=None):
    if basket is None:
        basket = []
    basket.append(item)
    return basket


def factorial(n: int) -> int:
    return 1 if n <= 1 else n * factorial(n - 1)


def main() -> None:
    print("=== EXPRESSIONS, STATEMENTS & SCOPE ===")
    score = 87
    if score >= 40:
        status = "pass"                 # created inside the if-block...
    print(f"  score={score} status={status} grade={grade(score)}  (...but visible here: no block scope)")

    print("\n=== if / elif / else ===")
    for temp in (-2, 18, 29, 41):
        if temp < 0:
            advice = "freezing - stay in"
        elif temp < 20:
            advice = "cool - take a jacket"
        elif temp < 35:
            advice = "pleasant"
        else:
            advice = "heatwave - stay hydrated"
        print(f"  {temp:3d} C -> {advice}")

    print("\n=== 'OVERLOADING' WITHOUT OVERLOADING ===")
    print(f"  area(4) square = {area(4)}, area(4, 6) rectangle = {area(4, 6)}, area(4, circle=True) = {area(4, circle=True):.2f}")
    print(f"  singledispatch: {describe(12345)} | {describe([1, 2, 3])} | {describe(3.5)}")

    print("\n=== *args, **kwargs, keyword-only, positional-only ===")
    print(f"  total() = {total()}, total(5) = {total(5)}, total(1, 2, 3, 4) = {total(1, 2, 3, 4)}")
    nums = [10, 20, 30]
    print(f"  total(*nums) unpacks a list into args = {total(*nums)}")
    print(f"  {make_profile('Srikar', 21, city='Goa', github='srikarreddyram', role='intern')}")
    try:
        make_profile(name="X", age=1)  # type: ignore[call-arg]  # deliberate: name is positional-only
    except TypeError as e:
        print(f"  positional-only enforced: TypeError: {e}")
    lo, hi, avg = min_max_avg([4.0, 9.5, 7.0])
    print(f"  multiple returns via tuple unpacking: min={lo} max={hi} avg={avg:.2f}")

    print("\n=== PASS-BY-OBJECT-REFERENCE ===")
    n, items, text = 1, [1, 2, 3], "original"
    try_to_change(n, items, text)
    print(f"  int  after call: {n}           (re-binding a parameter never affects the caller)")
    print(f"  list after call: {items}  (mutation of a shared object is visible)")
    print(f"  str  after call: {text!r}   (strings are immutable, += made a new object)")

    print("\n=== THE MUTABLE DEFAULT TRAP ===")
    print(f"  buggy: {add_item_buggy('a')} then {add_item_buggy('b')}  <- 'a' leaked into the 2nd call!")
    print(f"  fixed: {add_item_fixed('a')} then {add_item_fixed('b')}")

    print("\n=== FUNCTIONS ARE OBJECTS + DOCSTRINGS ===")
    ops = {"square": lambda x: x * x, "fact": factorial, "sqrt": math.sqrt}
    print("  " + ", ".join(f"{name}(5)={fn(5):g}" for name, fn in ops.items()))
    print(f"  area.__doc__ = {area.__doc__!r}")
    print(f"  area.__annotations__ = {area.__annotations__}")

    print("\n=== RECURSION ===")
    print("  " + ", ".join(f"{i}! = {factorial(i)}" for i in (0, 5, 10, 25)))
    print("  (25! needs no special type - Python ints grow as needed; Java long overflows after 20!)")


if __name__ == "__main__":
    main()
