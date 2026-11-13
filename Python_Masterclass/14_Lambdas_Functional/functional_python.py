"""
============================================================================
 SECTION 14 : LAMBDAS & FUNCTIONAL PYTHON - first-class functions, lambda,
              map/filter/reduce, comprehensions, functools, operator module
              (Python track - compare with LambdasFunctional.java)
============================================================================

 FUNCTIONS ARE ALREADY OBJECTS
   Java needs functional INTERFACES (Function, Predicate...) to pass behaviour.
   Python passes any function directly; type hints use Callable:
       | Java                  | Python hint                 | example             |
       |-----------------------|-----------------------------|---------------------|
       | Function<T,R>         | Callable[[T], R]            | len                 |
       | Predicate<T>          | Callable[[T], bool]         | str.isdigit         |
       | Consumer<T>           | Callable[[T], None]         | print               |
       | Supplier<T>           | Callable[[], T]             | list                |
       | BiFunction<T,U,R>     | Callable[[T, U], R]         | operator.add        |

 LAMBDA: a SINGLE EXPRESSION, no statements:  lambda a, b: a + b
   Anything longer -> write a def (it can be nested right where you need it).

 METHOD REFERENCES -> just name the function/method:
   Integer::parseInt -> int       String::toUpperCase -> str.upper
   greeting::concat  -> greeting.__add__ (bound method)   ArrayList::new -> list

 COMPREHENSIONS are the idiomatic map/filter:
   [f(x) for x in xs if cond(x)]    {k: v for ...}    {x for ...}    (x for ...)

 functools: partial (pre-fill args), reduce, lru_cache, cmp_to_key
 operator : itemgetter, attrgetter, methodcaller, add, mul (fast lambdas)

 HOW TO RUN:  python3 functional_python.py
============================================================================
"""
import operator
from collections.abc import Callable
from dataclasses import dataclass
from functools import cmp_to_key, partial, reduce


@dataclass(frozen=True)
class Product:
    name: str
    category: str
    price: float
    rating: float


def compose(*fns: Callable) -> Callable:
    """compose(f, g, h)(x) == h(g(f(x)))  - Java's andThen chain."""
    return lambda x: reduce(lambda acc, fn: fn(acc), fns, x)


def main() -> None:
    print("=== FUNCTIONS AS VALUES ===")
    words = ["banana", "kiwi", "apple", "fig", "cherry"]
    print(f"  sorted(key=len): {sorted(words, key=len)}")
    print(f"  sorted(reverse=True): {sorted(words, reverse=True)}")
    length: Callable[[str], int] = len
    long_word: Callable[[str], bool] = lambda s: len(s) > 4
    make_list: Callable[[], list] = list
    print(f"  'Function' len('masterclass') = {length('masterclass')}, 'Predicate' long_word('fig') = {long_word('fig')}, 'Supplier' list() = {make_list()}")

    print("\n=== map / filter / reduce vs comprehensions ===")
    nums = range(1, 11)
    print(f"  map(lambda)     : {list(map(lambda x: x * x, nums))}")
    print(f"  comprehension   : {[x * x for x in nums]}   <- preferred style")
    print(f"  filter(evens)   : {list(filter(lambda x: x % 2 == 0, nums))} == {[x for x in nums if x % 2 == 0]}")
    print(f"  reduce(product) : {reduce(operator.mul, nums)}  (sum/max/min/any/all cover most reduce uses)")
    print(f"  dict comp       : {{w: len(w) for w in words}} -> { {w: len(w) for w in words} }")
    print(f"  set comp        : { {len(w) for w in words} }")
    print(f"  any/all         : any >5 chars {any(len(w) > 5 for w in words)}, all lowercase {all(w.islower() for w in words)}")

    print("\n=== COMPOSING FUNCTIONS ===")
    plus2 = lambda x: x + 2
    times3 = lambda x: x * 3
    print(f"  compose(plus2, times3)(4) = {compose(plus2, times3)(4)}   (4+2)*3, like andThen")
    print(f"  compose(times3, plus2)(4) = {compose(times3, plus2)(4)}   4*3+2, like compose")
    festive, coupon, gst = (lambda p: p * 0.9), (lambda p: p - 100), (lambda p: p * 1.18)
    print(f"  Rs 2000 -> 10% off -> Rs 100 coupon -> +18% GST = Rs {compose(festive, coupon, gst)(2000):.2f}")
    starts_c = lambda s: s.startswith("c")
    both = lambda s: long_word(s) and starts_c(s)          # Predicate.and
    print(f"  long AND starts with c: {[w for w in words if both(w)]}")

    print("\n=== 'METHOD REFERENCES' ===")
    parse = int                                            # Integer::parseInt
    greeting = "Hello, "
    greet = greeting.__add__                               # bound method: greeting::concat
    upper = str.upper                                      # unbound: String::toUpperCase
    print(f"  int('2026') + 1 = {parse('2026') + 1}, greet('Srikar') = {greet('Srikar')}, str.upper('lambda') = {upper('lambda')}")
    print(f"  list(map(str.upper, words)) = {list(map(str.upper, words))}")

    print("\n=== partial, operator, cmp_to_key ===")
    to_binary = partial(int, base=2)                       # pre-fill an argument
    print(f"  partial(int, base=2)('1011') = {to_binary('1011')}")
    pow2 = partial(pow, exp=2)
    print(f"  partial(pow, exp=2)(12) = {pow2(12)}")
    catalog = [Product("Mouse", "electronics", 699, 4.2), Product("Desk Lamp", "home", 1299, 4.5),
               Product("Keyboard", "electronics", 2499, 4.6), Product("Mug", "home", 299, 3.9),
               Product("Headphones", "electronics", 3999, 4.4)]
    by_cat_then_price_desc = sorted(catalog, key=lambda p: (p.category, -p.price))   # Comparator chain in one key
    for p in by_cat_then_price_desc:
        print(f"    {p.category:<11} {p.name:<11} Rs {p.price:5.0f}  {p.rating}*")
    print(f"  attrgetter('name') -> {list(map(operator.attrgetter('name'), catalog))}")
    old_style = lambda a, b: (a.rating < b.rating) - (a.rating > b.rating)            # Java-style comparator
    print(f"  cmp_to_key(java-style comparator), best rated first: {[p.name for p in sorted(catalog, key=cmp_to_key(old_style))]}")

    print("\n=== GROUP & AGGREGATE (Java's merge(..., Double::sum)) ===")
    spend: dict[str, float] = {}
    for p in catalog:
        if p.rating >= 4.0:
            spend[p.category] = spend.get(p.category, 0) + p.price
    print(f"  spend by category (rating >= 4): {spend}")

    print("\n=== CAPTURING: no 'effectively final' rule ===")
    min_rating: float = 4
    good = lambda p: p.rating >= min_rating
    min_rating = 4.5                                       # legal in Python; the lambda sees the NEW value at call time
    print(f"  products passing after min_rating changed to 4.5: {[p.name for p in catalog if good(p)]}")


if __name__ == "__main__":
    main()
