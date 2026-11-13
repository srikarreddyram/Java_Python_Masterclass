"""
============================================================================
 SECTION 13 : NESTED TYPES -> PYTHON: nested classes, nested functions,
              CLOSURES, nonlocal, DECORATORS
              (Python track - compare with NestedClasses.java)
============================================================================

 WHERE JAVA USES NESTED CLASSES, PYTHON USUALLY USES FUNCTIONS
   | Java                                 | Python                                   |
   |--------------------------------------|------------------------------------------|
   | static nested class (Builder, Node)  | nested class - always "static": it does  |
   |                                      | NOT capture the outer instance           |
   | inner class (needs Outer.this)       | pass the outer object explicitly, or use |
   |                                      | a closure / generator                    |
   | local class inside a method          | class or def inside a function           |
   | anonymous class (one-off Comparator) | lambda or a nested def                   |
   | captured effectively-final locals    | CLOSURES capture variables; 'nonlocal'   |
   |                                      | even lets you REASSIGN them              |

 CLOSURE = a function plus the variables it captured from the enclosing scope.
 Captured variables live on (in fn.__closure__) after the outer function returns.

 DECORATOR = a function that takes a function and returns a new function,
 applied with @name. Used for logging, timing, caching, auth checks, retries.
   @timed          is just       f = timed(f)

 LATE BINDING GOTCHA: closures look up the variable when CALLED, not when
 created, so  [lambda: i for i in range(3)]  all return 2.

 HOW TO RUN:  python3 nested_closures_decorators.py
============================================================================
"""
import functools
import time


# ======================== nested class: Builder ========================
class Pizza:
    def __init__(self, size: str, toppings: list[str], extra_cheese: bool):
        self.size, self.toppings, self.extra_cheese = size, toppings, extra_cheese

    def __str__(self) -> str:
        return f"{self.size} pizza with {self.toppings}" + (" + extra cheese" if self.extra_cheese else "")

    class Builder:                                   # nested class: grouped under Pizza, no outer instance
        def __init__(self):
            self._size, self._toppings, self._cheese = "medium", [], False

        def size(self, s: str) -> "Pizza.Builder":
            self._size = s
            return self

        def topping(self, t: str) -> "Pizza.Builder":
            self._toppings.append(t)
            return self

        def extra_cheese(self) -> "Pizza.Builder":
            self._cheese = True
            return self

        def build(self) -> "Pizza":
            return Pizza(self._size, list(self._toppings), self._cheese)


# ======================== closures ========================
def make_counter(start: int = 0):
    count = start                                    # captured by the inner function

    def increment(step: int = 1) -> int:
        nonlocal count                               # allow REASSIGNING the captured variable
        count += step
        return count

    return increment


def make_multiplier(factor: float):
    return lambda x: x * factor                      # closure over 'factor'


# ======================== decorators ========================
def timed(fn):
    @functools.wraps(fn)                             # keep fn's name/docstring
    def wrapper(*args, **kwargs):
        t0 = time.perf_counter()
        result = fn(*args, **kwargs)
        print(f"    [timed] {fn.__name__} took {(time.perf_counter() - t0) * 1000:.2f} ms")
        return result
    return wrapper


def retry(times: int):                               # decorator WITH arguments = a decorator factory
    def decorator(fn):
        @functools.wraps(fn)
        def wrapper(*args, **kwargs):
            for attempt in range(1, times + 1):
                try:
                    return fn(*args, **kwargs)
                except ConnectionError as e:
                    print(f"    [retry] attempt {attempt}/{times} failed: {e}")
            raise ConnectionError(f"{fn.__name__} failed after {times} attempts")
        return wrapper
    return decorator


def count_calls(fn):
    @functools.wraps(fn)
    def wrapper(*args, **kwargs):
        wrapper.calls += 1                           # functions are objects: attach state
        return fn(*args, **kwargs)
    wrapper.calls = 0
    return wrapper


@count_calls
def fib(n: int) -> int:
    return n if n < 2 else fib(n - 1) + fib(n - 2)


@functools.lru_cache(maxsize=None)                   # built-in memoising decorator
def fib_fast(n: int) -> int:
    return n if n < 2 else fib_fast(n - 1) + fib_fast(n - 2)


_flaky_state = {"calls": 0}


@retry(times=3)
def fetch_exchange_rate() -> float:
    _flaky_state["calls"] += 1
    if _flaky_state["calls"] < 3:
        raise ConnectionError("timeout")
    return 83.42


@timed
def sort_employees(rows: list[tuple[str, str, int]], mode: str) -> list[str]:
    # local "record" and local comparator - Java's local class / anonymous class
    keys = {
        "salary": lambda e: -e[2],
        "dept": lambda e: (e[1], e[0]),
        "name": lambda e: e[0],
    }
    return [f"{n:<7} {d:<6} {s:>7,}" for n, d, s in sorted(rows, key=keys[mode])]


def main() -> None:
    print("=== NESTED CLASS: Builder ===")
    print("  " + str(Pizza.Builder().size("large").topping("paneer").topping("capsicum").extra_cheese().build()))
    print("  " + str(Pizza.Builder().topping("corn").build()))

    print("\n=== CLOSURES ===")
    clicks = make_counter()
    other = make_counter(100)
    print(f"  clicks: {clicks()}, {clicks()}, {clicks(5)}   independent counter: {other()}")
    print(f"  captured cell contents: {[c.cell_contents for c in clicks.__closure__]}")
    double, gst = make_multiplier(2), make_multiplier(1.18)
    print(f"  double(21) = {double(21)}, gst(1000) = {gst(1000):.2f}")

    print("\n=== LATE-BINDING GOTCHA ===")
    wrong = [lambda: i for i in range(3)]
    right = [lambda i=i: i for i in range(3)]       # default arg freezes the value now
    print(f"  wrong: {[f() for f in wrong]}   right: {[f() for f in right]}")

    print("\n=== DECORATORS ===")
    rows = [("Asha", "ENG", 95000), ("Ravi", "SALES", 60000), ("Meera", "ENG", 120000),
            ("Karan", "HR", 55000), ("Divya", "SALES", 91000)]
    for mode in ("salary", "dept"):
        print(f"  sorted by {mode}:")
        for line in sort_employees(rows, mode):
            print(f"    {line}")
    print(f"  decorated function keeps its name: {sort_employees.__name__}")

    print(f"  fib(20) = {fib(20)} took {fib.calls:,} calls (count_calls decorator)")
    t0 = time.perf_counter()
    print(f"  fib_fast(200) = {fib_fast(200)} via @lru_cache in {(time.perf_counter() - t0) * 1000:.2f} ms, cache {fib_fast.cache_info()}")
    print(f"  retry decorator -> rate = {fetch_exchange_rate()}")


if __name__ == "__main__":
    main()
