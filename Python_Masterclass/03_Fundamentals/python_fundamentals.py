"""
============================================================================
 SECTION 03 : PYTHON FUNDAMENTALS - variables & dynamic typing, numeric
              types, conversion ("casting"), operators, bool/None, strings
              (Python track - compare with JavaFundamentals.java)
============================================================================

 VARIABLES ARE NAMES, NOT BOXES
 -------------------------------
   Java : int age = 21;      the VARIABLE has a type, fixed at compile time
   Python: age = 21          the OBJECT has a type; the name can be re-bound
           age = "twenty"    legal (dynamic typing) - but type hints + a checker
                             (mypy/pyright) give you Java-like safety:  age: int = 21

 NUMERIC TYPES
   | Python  | Java equivalent       | Notes                                      |
   |---------|-----------------------|--------------------------------------------|
   | int     | byte/short/int/long   | ARBITRARY precision - never overflows      |
   | float   | double                | 64-bit IEEE 754, same 0.1+0.2 problem      |
   | Decimal | BigDecimal            | exact decimal arithmetic (money)           |
   | Fraction| -                     | exact rationals 1/3                        |
   | complex | -                     | 3+4j                                       |
   | bool    | boolean               | subclass of int! True == 1                 |
   | None    | null                  | the single "no value" object               |
   | str     | String                | immutable, Unicode; no separate char type  |

 CONVERSIONS are function calls, not casts: int("42"), float("3.5"), str(7),
 int(9.99) -> 9 (truncates), round(9.99) -> 10, bool(""), bool([]) -> False.

 OPERATORS that differ from Java
   /  true division (7/2 == 3.5)    //  floor division (7//2 == 3, -7//2 == -4!)
   ** power                          and / or / not  instead of && || !
   == value equality                 is  identity (Java's == on references)
   no ++ / --  (use x += 1)          chained comparisons: 0 < x < 10
   x if cond else y  (ternary)       := walrus assigns inside an expression

 HOW TO RUN:  python3 python_fundamentals.py
============================================================================
"""
import math
import sys
from decimal import ROUND_HALF_UP, Decimal
from fractions import Fraction


def dynamic_typing() -> None:
    print("=== NAMES, OBJECTS, DYNAMIC TYPING ===")
    x = 42
    print(f"  x = 42      -> type {type(x).__name__}, id {id(x) % 10_000}")
    x = "forty-two"  # type: ignore[assignment]  # deliberate: dynamic re-binding demo
    print(f"  x = '...'   -> type {type(x).__name__}  (the NAME was re-bound to a new object)")
    count: int = 10                       # type hint: documentation + static checking, NOT enforced at runtime
    count = "ten"  # type: ignore[assignment]  # deliberate: runs fine, mypy flags it
    print(f"  hints aren't enforced at runtime: count = {count!r}")
    a, b, *rest = [1, 2, 3, 4, 5]         # unpacking (no Java equivalent)
    a, b = b, a                           # swap without a temp variable
    print(f"  unpacking: a={a} b={b} rest={rest}")


def numbers() -> None:
    print("\n=== NUMBERS ===")
    big = 2 ** 100
    print(f"  2**100 = {big}  (Python ints never overflow; Java long would wrap)")
    print(f"  sys.maxsize = {sys.maxsize:,} is only the max list SIZE, not the max int")
    print(f"  0.1 + 0.2 = {0.1 + 0.2}  (same IEEE 754 floats as Java double)")
    print(f"  math.isclose(0.1 + 0.2, 0.3) = {math.isclose(0.1 + 0.2, 0.3)}  <- compare floats this way")
    price = Decimal("19.99") * 3
    print(f"  Decimal('19.99') * 3 = {price}, rounded = {price.quantize(Decimal('0.1'), ROUND_HALF_UP)}")
    print(f"  Decimal(0.1) = {Decimal(0.1)}  <- build Decimals from STRINGS, not floats")
    print(f"  Fraction(1, 3) * 3 = {Fraction(1, 3) * 3}, complex (3+4j) magnitude = {abs(3 + 4j)}")
    print(f"  readable literals: {1_000_000:,}  hex {0xFF}  binary {0b1010}  sci {1.5e3}")


def conversions() -> None:
    print("\n=== CONVERSIONS (Java casting) ===")
    print(f"  int('42') + 1 = {int('42') + 1},  float('3.5') = {float('3.5')},  str(7) + '7' = {str(7) + '7'}")
    print(f"  int(9.99) = {int(9.99)} (truncates),  round(9.99) = {round(9.99)},  round(2.5) = {round(2.5)} (banker's rounding!)")
    print(f"  int('ff', 16) = {int('ff', 16)},  bin(10) = {bin(10)},  ord('A') = {ord('A')},  chr(66) = {chr(66)}")
    try:
        int("12abc")
    except ValueError as e:
        print(f"  int('12abc') -> ValueError: {e}")
    print(f"  '10' + 5 -> TypeError in Python (Java would give '105'); you must write '10' + str(5) = {'10' + str(5)}")


def operators() -> None:
    print("\n=== OPERATORS ===")
    a, b = 17, 5
    print(f"  {a} / {b} = {a / b}, {a} // {b} = {a // b}, {a} % {b} = {a % b}, {a} ** 2 = {a ** 2}")
    print(f"  -7 // 2 = {-7 // 2} (floors toward -inf; Java -7/2 = -3),  -7 % 2 = {-7 % 2} (Java gives -1)")
    print(f"  divmod(17, 5) = {divmod(17, 5)}")
    x = 5
    x += 1                                  # no x++ in Python
    print(f"  x += 1 -> {x}; chained 0 < x < 10 -> {0 < x < 10}")
    adult, has_id = True, False
    print(f"  and/or/not: {adult and has_id}, {adult or has_id}, {not has_id}")
    print(f"  'or' returns an OPERAND: '' or 'default' -> {'' or 'default'!r}, 0 or 99 -> {0 or 99}")
    label = "a is bigger" if a > b else "b is bigger"
    print(f"  conditional expression: {label}")
    data = [3, 8, 1]
    if (n := len(data)) > 2:                # walrus: assign AND test
        print(f"  walrus: list has {n} items")


def truthiness_and_none() -> None:
    print("\n=== TRUTHINESS, bool, None ===")
    samples: tuple[object, ...] = (0, 0.0, "", [], {}, None, "0", [0], -1)
    for value in samples:
        print(f"  bool({value!r:>5}) = {bool(value)}")
    print(f"  True + True = {True + True}  (bool is a subclass of int)")
    result = None
    print(f"  result is None -> {result is None}  (always test None with 'is', not ==)")


def strings() -> None:
    print("\n=== STRINGS (immutable, Unicode, no char type) ===")
    s = "Srikar"
    print(f"  s[0] = {s[0]!r} (a 1-char str), s[-1] = {s[-1]!r}, s[1:4] = {s[1:4]!r}, len = {len(s)}")
    print(f"  'na' * 4 = {'na' * 4}, 'kar' in s = {'kar' in s}")
    a = "hello"
    b = "".join(["hel", "lo"])               # built at runtime -> a different object
    print(f"  a == b: {a == b}   a is b: {a is b}  <- use == for strings (like Java's equals)")
    namaste, smile = "नमस्ते", "\U0001F642"   # Devanagari word, emoji
    print(f"  Unicode: len(namaste) = {len(namaste)} code points, len(emoji) = {len(smile)} "
          f"(Java's String.length() would say 2 for this emoji); ascii() view: {ascii(smile)}")


if __name__ == "__main__":
    dynamic_typing()
    numbers()
    conversions()
    operators()
    truthiness_and_none()
    strings()
