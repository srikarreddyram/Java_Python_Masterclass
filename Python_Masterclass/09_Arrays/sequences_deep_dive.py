"""
============================================================================
 SECTION 09 : ARRAYS -> PYTHON SEQUENCES: list, tuple, array.array, slicing,
              2D lists, copying (shallow vs deep), sorting, bisect
              (Python track - compare with ArraysDeepDive.java)
============================================================================

 WHAT REPLACES int[] ?
   | Java                    | Python                                        |
   |-------------------------|-----------------------------------------------|
   | int[] a = new int[5]    | a = [0] * 5          (list: dynamic, any type)|
   | {2, 3, 5}               | [2, 3, 5]  or tuple (2, 3, 5) (immutable)     |
   | typed primitive array   | array.array('i', ...) or numpy.ndarray        |
   | a.length                | len(a)                                        |
   | a[a.length - 1]         | a[-1]       (negative indexes count from end) |
   | Arrays.copyOfRange      | a[2:5]      (slicing always makes a copy)     |
   | Arrays.toString(a)      | print(a)    (lists print nicely)              |
   | Arrays.sort / binarySearch | a.sort(), sorted(a) / bisect module        |
   | Arrays.fill             | [x] * n                                       |
   | Arrays.equals           | a == b      (compares contents)               |
   | int[][] m = new int[3][4] | [[0] * 4 for _ in range(3)]  (NOT [[0]*4]*3)|

 SLICING: seq[start:stop:step]  stop is exclusive, any part optional
   a[::-1] reverses, a[::2] every second element, a[-3:] last three.

 HOW TO RUN:  python3 sequences_deep_dive.py
============================================================================
"""
import bisect
import copy
import random
import sys
from array import array


def basics() -> None:
    print("=== CREATE, INDEX, SLICE ===")
    marks = [0] * 5
    marks[0], marks[-1] = 91, 77
    prices = [49.5, 120.0, 15.25]
    print(f"  marks = {marks}, prices = {prices}, len = {len(prices)}")
    try:
        marks[5] = 10
    except IndexError as e:
        print(f"  marks[5] -> IndexError: {e}")
    a: list[object] = list(range(10))
    print(f"  a = {a}")
    print(f"  a[2:5] = {a[2:5]}, a[:3] = {a[:3]}, a[-3:] = {a[-3:]}, a[::2] = {a[::2]}, a[::-1] = {a[::-1]}")
    a[1:4] = ["x", "y"]                  # slice ASSIGNMENT can even change the length
    print(f"  a[1:4] = ['x','y'] -> {a}")
    point = (3, 4)                       # tuple: fixed, immutable record-like sequence
    x, y = point
    print(f"  tuple {point} unpacked to x={x}, y={y}; tuples can be dict keys, lists can't")


def typed_arrays() -> None:
    print("\n=== TYPED ARRAYS: list vs array.array ===")
    nums_list = list(range(100_000))
    nums_arr = array("i", range(100_000))   # C ints packed contiguously, like Java int[]
    list_bytes = sys.getsizeof(nums_list) + sum(sys.getsizeof(n) for n in nums_list)   # pointer array + int objects
    print(f"  list of 100k ints : ~{list_bytes // 1024:,} KB (8-byte pointers + a 28-byte int object each)")
    print(f"  array('i') 100k   : ~{sys.getsizeof(nums_arr) // 1024:,} KB (raw 4-byte ints)")
    try:
        nums_arr.append(3.5)  # type: ignore[arg-type]  # deliberate: typed array rejects floats
    except TypeError as e:
        print(f"  array('i').append(3.5) -> TypeError ({e}) - typed like Java")


def references_and_copies() -> None:
    print("\n=== REFERENCES vs SHALLOW vs DEEP COPY ===")
    original = [1, 2, 3]
    same = original                      # another name for the SAME list
    shallow = original[:]                # or list(original) / original.copy()
    same[0] = 100
    print(f"  original after same[0]=100: {original};  shallow copy unaffected: {shallow}")
    nested = [[1, 2], [3, 4]]
    shallow_n, deep_n = copy.copy(nested), copy.deepcopy(nested)
    nested[0].append(99)
    print(f"  nested after nested[0].append(99): {nested}")
    print(f"  shallow copy SHARES inner lists:  {shallow_n}")
    print(f"  deep copy is fully independent:   {deep_n}")
    print(f"  == compares contents: {[1, 2] == [1, 2]};  is compares identity: {[1, 2] is [1, 2]}")


def sorting_and_searching() -> None:
    print("\n=== SORT / SEARCH ===")
    rnd = random.Random(7)
    data = [rnd.randint(1, 99) for _ in range(10)]
    print(f"  random      : {data}")
    print(f"  sorted(data): {sorted(data)}  (returns NEW list)")
    data.sort(reverse=True)
    print(f"  data.sort(reverse=True): {data}  (in place, returns None)")
    data.sort()
    key = data[6]
    i = bisect.bisect_left(data, key)
    print(f"  bisect_left({key}) -> {i}, found: {i < len(data) and data[i] == key}")
    print(f"  insertion point for 1000 -> {bisect.bisect_left(data, 1000)}")
    bisect.insort(data, 50)
    print(f"  insort(50) keeps it sorted: {data}")
    cities = ["Pune", "Chennai", "Goa", "Agra"]
    print(f"  sorted strings: {sorted(cities)}, by length: {sorted(cities, key=len)}, reverse alpha: {sorted(cities, reverse=True)}")
    print(f"  sum {sum(data)}, max {max(data)}, min {min(data)}, index of max {data.index(max(data))}, count(50) {data.count(50)}")


def two_dimensional() -> None:
    print("\n=== 2D LISTS ===")
    wrong = [[0] * 3] * 3
    wrong[0][0] = 1
    print(f"  [[0]*3]*3 then wrong[0][0]=1 -> {wrong}   <- 3 references to ONE row!")
    matrix = [[r * 4 + c for c in range(4)] for r in range(3)]
    print(f"  matrix (comprehension): {matrix}")
    transposed = [list(col) for col in zip(*matrix)]
    print(f"  transposed via zip(*m) : {transposed}")
    pascal = [[1]]
    for _ in range(5):
        prev = pascal[-1]
        pascal.append([1] + [a + b for a, b in zip(prev, prev[1:])] + [1])
    print("  Pascal's triangle (jagged):")
    for row in pascal:
        print(f"    {row}")


def classic_algorithms() -> None:
    print("\n=== CLASSIC ALGORITHMS, PYTHONICALLY ===")
    a = [5, 3, 9, 1, 7]
    print(f"  reverse   : {a[::-1]}")
    print(f"  rotate r2 : {a[-2:] + a[:-2]}")
    prefix, run = [], 0
    for v in a:
        run += v
        prefix.append(run)
    print(f"  prefix sums: {prefix} (itertools.accumulate does this too)")
    print(f"  pairs that sum to 10: {[(x, y) for i, x in enumerate(a) for y in a[i + 1:] if x + y == 10]}")


if __name__ == "__main__":
    basics()
    typed_arrays()
    references_and_copies()
    sorting_and_searching()
    two_dimensional()
    classic_algorithms()
