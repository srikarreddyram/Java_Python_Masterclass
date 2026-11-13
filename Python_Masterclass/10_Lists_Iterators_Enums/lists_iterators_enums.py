"""
============================================================================
 SECTION 10 : LISTS, deque, THE ITERATOR PROTOCOL, GENERATORS, ENUMS
              (Python track - compare with ListsIteratorsEnums.java)
============================================================================

 LIST vs Java ArrayList - the same structure (dynamic array of references)
   | op                    | Java ArrayList    | Python list          | cost     |
   |-----------------------|-------------------|----------------------|----------|
   | append                | add(x)            | append(x)            | O(1) am. |
   | insert at i           | add(i, x)         | insert(i, x)         | O(n)     |
   | get / set             | get(i) / set(i,x) | a[i] / a[i] = x      | O(1)     |
   | remove by value       | remove(Object)    | remove(x)            | O(n)     |
   | remove by index       | remove(int)       | pop(i) / del a[i]    | O(n)     |
   | remove last           | remove(size-1)    | pop()                | O(1)     |
   | contains / indexOf    | contains / indexOf| x in a / a.index(x)  | O(n)     |
   | LinkedList / Deque    | LinkedList        | collections.deque    | O(1) ends|

 ITERATOR PROTOCOL (Java's Iterable/Iterator)
   iter(obj) calls obj.__iter__() -> an iterator
   next(it)  calls it.__next__()  -> next value, or raises StopIteration
   A for-loop does exactly this behind the scenes.

 GENERATORS (no direct Java equivalent): a function with 'yield' returns a
 lazy iterator. Values are produced on demand, one at a time - you can loop
 over a billion items with constant memory.

 AUTOBOXING: doesn't exist - every Python value is already an object.
 But the same IDENTITY trap exists: small ints are cached, so 'is' "works"
 for 256 and not for 1000. Always compare values with ==.

 ENUMS: class Size(Enum) - members are singletons, can have values/methods.

 HOW TO RUN:  python3 lists_iterators_enums.py
============================================================================
"""
import itertools
from collections import deque
from enum import Enum, IntEnum, auto


class Size(Enum):
    SMALL = ("S", 250)
    MEDIUM = ("M", 350)
    LARGE = ("L", 500)

    def __init__(self, code: str, ml: int):         # each member's value tuple is unpacked here
        self.code, self.ml = code, ml

    def upgrade(self) -> "Size":
        members = list(Size)
        i = members.index(self)
        return members[min(i + 1, len(members) - 1)]

    def __str__(self) -> str:
        return f"{self.code}({self.ml}ml)"


class Priority(IntEnum):                            # comparable like ints
    LOW = auto()
    MEDIUM = auto()
    HIGH = auto()


# ---------------- custom iterator (class-based) ----------------
class Countdown:
    """Iterable class: for x in Countdown(3) -> 3, 2, 1"""
    def __init__(self, start: int):
        self.start = start

    def __iter__(self):
        return CountdownIterator(self.start)


class CountdownIterator:
    def __init__(self, current: int):
        self.current = current

    def __iter__(self):
        return self

    def __next__(self) -> int:
        if self.current <= 0:
            raise StopIteration                     # signals the end to the for-loop
        self.current -= 1
        return self.current + 1


# ---------------- the same thing as a generator ----------------
def countdown(start: int):
    while start > 0:
        yield start                                 # pause here, resume on next()
        start -= 1


def fibonacci():                                    # INFINITE generator - fine, it's lazy
    a, b = 0, 1
    while True:
        yield a
        a, b = b, a + b


def list_demo() -> None:
    print("=== LIST ===")
    groceries = ["milk", "eggs", "bread"]
    groceries.append("apples")
    groceries.insert(1, "butter")
    groceries[0] = "oat milk"
    groceries.extend(["tea", "eggs"])
    print(f"  list: {groceries}, len={len(groceries)}")
    print(f"  index('eggs')={groceries.index('eggs')}, count('eggs')={groceries.count('eggs')}, 'tea' in list: {'tea' in groceries}")
    groceries.remove("eggs")                          # first match, by value
    last = groceries.pop()                            # by index (default: last)
    del groceries[0]
    print(f"  after remove/pop('{last}')/del: {groceries}")
    groceries.sort()
    print(f"  sorted: {groceries}")
    groceries = [g for g in groceries if not g.startswith("b")]    # Java removeIf
    print(f"  filtered (no b*): {groceries}")
    frozen = tuple(groceries)
    print(f"  tuple(list) is the immutable version: {frozen}")


def deque_demo() -> None:
    print("\n=== deque (Java LinkedList / ArrayDeque) ===")
    tasks = deque(["write code"])
    tasks.appendleft("plan")
    tasks.append("deploy")
    tasks.insert(2, "test")
    print(f"  tasks: {list(tasks)}  first={tasks[0]} last={tasks[-1]}")
    print(f"  queue popleft() -> {tasks.popleft()}, now {list(tasks)}")
    tasks.append("hotfix")
    print(f"  stack pop() -> {tasks.pop()}")
    recent: deque[str] = deque(maxlen=3)                          # bounded: old items fall off automatically
    for page in ["home", "cart", "pay", "done"]:
        recent.append(page)
    print(f"  deque(maxlen=3) after 4 appends: {list(recent)}")


def iterator_demo() -> None:
    print("\n=== ITERATORS & GENERATORS ===")
    nums = [4, 6, 7, 10, 12, 13]
    for x in nums:
        if x % 2 == 0:
            nums.remove(x)                            # WRONG: no exception in Python, just wrong results
    print(f"  remove evens while iterating -> {nums}  <- 6 and 12 survived (the list shifted under the loop)")
    print("  (Java throws ConcurrentModificationException here; Python silently skips elements)")
    nums = [4, 6, 7, 10, 12, 13]
    nums = [x for x in nums if x % 2]                 # RIGHT: build a new list
    print(f"  comprehension filter: {nums}")

    it = iter(["a", "b"])
    print(f"  manual iteration: next={next(it)}, next={next(it)}, next(it, 'END')={next(it, 'END')}")
    print(f"  class-based Countdown(3): {list(Countdown(3))}")
    print(f"  generator countdown(3)  : {list(countdown(3))}")
    gen = fibonacci()
    print(f"  first 10 of an INFINITE generator: {list(itertools.islice(gen, 10))}")
    squares = (x * x for x in range(1_000_000))       # generator expression: nothing computed yet
    print(f"  generator expression sum of 1M squares = {sum(squares):,} (constant memory)")
    print(f"  itertools: chain={list(itertools.chain([1, 2], [3]))} pairwise={list(itertools.pairwise('abc'))} "
          f"product={list(itertools.product('ab', [1, 2]))}")


def identity_demo() -> None:
    print("\n=== 'AUTOBOXING' TRAP: identity vs equality ===")
    a, b = 256, 256
    c, d = int("1000"), int("1000")
    print(f"  256 is 256: {a is b} (small-int cache);  1000 is 1000: {c is d} <- use ==: {c == d}")


def enum_demo() -> None:
    print("\n=== ENUMS ===")
    s = Size["MEDIUM"]                                # by name (Java valueOf)
    print(f"  members: {[str(x) for x in Size]}, name={s.name}, code={s.code}")
    print(f"  upgrade: {s} -> {s.upgrade()} -> {s.upgrade().upgrade()}")
    match s:
        case Size.SMALL:
            price = "Rs 120"
        case Size.MEDIUM:
            price = "Rs 160"
        case Size.LARGE:
            price = "Rs 210"
    print(f"  match on enum: {s} costs {price}")
    print(f"  IntEnum compares like ints: HIGH > LOW = {Priority.HIGH > Priority.LOW}, sorted = {sorted([Priority.HIGH, Priority.LOW, Priority.MEDIUM])}")
    counts = {sz: 0 for sz in Size}                   # dict keyed by enum (Java EnumMap)
    for order in [Size.LARGE, Size.SMALL, Size.LARGE]:
        counts[order] += 1
    print(f"  order counts: {{{', '.join(f'{k.name}: {v}' for k, v in counts.items())}}}")


if __name__ == "__main__":
    list_demo()
    deque_demo()
    iterator_demo()
    identity_demo()
    enum_demo()
