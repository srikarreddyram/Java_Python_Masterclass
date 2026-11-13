"""
============================================================================
 SECTION 15 : PYTHON COLLECTIONS - dict, set, frozenset, the collections
              module (Counter, defaultdict, OrderedDict, deque, namedtuple,
              ChainMap), heapq, hashing rules
              (Python track - compare with CollectionsFramework.java)
============================================================================

 JAVA COLLECTIONS FRAMEWORK -> PYTHON
   | Java                         | Python                                     | notes                    |
   |------------------------------|--------------------------------------------|--------------------------|
   | HashMap / LinkedHashMap      | dict                                       | keeps insertion order    |
   | TreeMap (sorted keys)        | dict + sorted(), or sortedcontainers lib   | no built-in tree map     |
   | HashSet / LinkedHashSet      | set  (no order)  /  dict.fromkeys (order)  |                          |
   | TreeSet                      | sorted(set) / bisect on a sorted list      |                          |
   | Collections.unmodifiable*    | frozenset, tuple, types.MappingProxyType   |                          |
   | ArrayDeque / LinkedList      | collections.deque                          | O(1) both ends           |
   | PriorityQueue                | heapq on a list (min-heap)                 |                          |
   | map.merge(k, 1, Integer::sum)| Counter / defaultdict(int)                 |                          |
   | computeIfAbsent(k, ArrayList)| defaultdict(list) / dict.setdefault        |                          |
   | getOrDefault                 | d.get(k, default)                          |                          |
   | LinkedHashMap LRU            | OrderedDict.move_to_end / functools.lru_cache |                       |
   | equals + hashCode            | __eq__ + __hash__ (dataclass(frozen=True)) |                          |

 HASHING RULES: dict keys and set members must be HASHABLE (immutable):
 str, int, tuple of hashables, frozenset, frozen dataclasses. Lists, dicts and
 sets are NOT hashable. Same contract as Java: a == b  =>  hash(a) == hash(b).

 HOW TO RUN:  python3 collections_python.py
============================================================================
"""
import heapq
from collections import ChainMap, Counter, OrderedDict, defaultdict, deque, namedtuple
from dataclasses import dataclass
from types import MappingProxyType


@dataclass(frozen=True)
class Contact:                                    # frozen dataclass: correct __eq__ AND __hash__
    name: str
    email: str


class BadContact:                                 # __eq__ without __hash__ -> unhashable in Python
    def __init__(self, email: str):
        self.email = email

    def __eq__(self, other) -> bool:
        return isinstance(other, BadContact) and self.email == other.email


def sets() -> None:
    print("=== SETS ===")
    visits = ["goa", "pune", "agra", "goa", "delhi", "pune", "chennai"]
    print(f"  input           : {visits}")
    print(f"  set             : {set(visits)}  (no order guarantee)")
    print(f"  dict.fromkeys   : {list(dict.fromkeys(visits))}  (dedupe, first-seen order)")
    print(f"  sorted(set)     : {sorted(set(visits))}  (TreeSet view)")
    a, b = {"java", "python", "sql", "aws"}, {"python", "ml", "sql", "spark"}
    print(f"  A | B = {sorted(a | b)}  A & B = {sorted(a & b)}  A - B = {sorted(a - b)}  A ^ B = {sorted(a ^ b)}")
    print(f"  subset: {{'sql'}} <= A -> {{'sql'}} <= a = { {'sql'} <= a }")
    fs = frozenset({"read", "write"})
    perms = {fs: "editor"}                        # frozenset is hashable -> usable as a key
    print(f"  frozenset as a dict key: {perms[frozenset({'write', 'read'})]}")


def hashing() -> None:
    print("\n=== HASHING CONTRACT ===")
    good = {Contact("Srikar", "s@x.com"), Contact("Srikar", "s@x.com")}
    print(f"  frozen dataclass in a set: {len(good)} element(s)")
    try:
        {BadContact("a@x.com")}
    except TypeError as e:
        print(f"  class with __eq__ but no __hash__ -> TypeError: {e}  (Python refuses; Java silently misbehaves)")
    try:
        {[1, 2]: "list key"}
    except TypeError as e:
        print(f"  list as a dict key -> TypeError: {e}")
    print(f"  tuple as a key works: {({(13.08, 80.27): 'Chennai'})[(13.08, 80.27)]}")


def dicts() -> None:
    print("\n=== DICTS ===")
    text = "to be or not to be that is the question to ask"
    freq: dict[str, int] = {}
    for w in text.split():
        freq[w] = freq.get(w, 0) + 1             # Java: merge(w, 1, Integer::sum)
    print(f"  manual counting : {dict(sorted(freq.items()))}")
    print(f"  Counter         : {Counter(text.split()).most_common(3)}  <- top 3 in one call")
    groups = defaultdict(list)                    # computeIfAbsent(k, ArrayList::new)
    for w in text.split():
        groups[w[0]].append(w)
    print(f"  defaultdict(list): {dict(sorted(groups.items()))}")
    stock = {"pen": 40, "notebook": 12, "eraser": 0}
    stock.setdefault("pen", 999)                  # putIfAbsent: ignored
    stock["notebook"] -= 2
    stock["marker"] = stock.get("marker", 0) + 5
    stock = {k: (-1 if v == 0 else v) for k, v in stock.items()}   # replaceAll
    print(f"  dict ops        : {stock}, get('glue', 0) = {stock.get('glue', 0)}")
    merged = {"theme": "light", "lang": "en"} | {"theme": "dark"}  # 3.9+ merge operator
    print(f"  d1 | d2 merge   : {merged}")
    print(f"  iterate         : {[f'{k}={v}' for k, v in stock.items() if v > 0]}")

    tiers = [(0, "Bronze"), (1_000, "Silver"), (5_000, "Gold"), (20_000, "Platinum")]   # TreeMap.floorEntry
    import bisect
    thresholds = [t for t, _ in tiers]
    for pts in (450, 1_000, 7_250, 50_000):
        print(f"  floorEntry({pts}) via bisect -> {tiers[bisect.bisect_right(thresholds, pts) - 1][1]}")

    lru: OrderedDict[str, str] = OrderedDict()
    for k in "abc":
        lru[k] = k.upper()
    lru.move_to_end("a")                          # 'a' used recently
    lru["d"] = "D"
    if len(lru) > 3:
        lru.popitem(last=False)                   # evict least recently used
    print(f"  LRU with OrderedDict: {list(lru)}")

    defaults: dict[str, object] = {"region": "ap-south-1", "debug": False}
    env: dict[str, object] = {"debug": True}
    config = ChainMap(env, defaults)              # layered lookup, no copying
    print(f"  ChainMap(env, defaults): debug={config['debug']} region={config['region']}")
    read_only = MappingProxyType(defaults)
    try:
        read_only["debug"] = True  # type: ignore[index]  # deliberate: read-only view
    except TypeError:
        print("  MappingProxyType is a read-only VIEW (Collections.unmodifiableMap)")


def queues() -> None:
    print("\n=== QUEUES ===")
    stack = deque(["page1", "page2", "page3"])
    print(f"  deque as stack: pop={stack.pop()} peek={stack[-1]}")
    fifo = deque(["cust1", "cust2"])
    fifo.append("cust3")
    print(f"  deque as queue: popleft={fifo.popleft()} remaining={list(fifo)}")
    pq: list[tuple[int, str]] = []
    for pr, tid in [(3, "T-104"), (1, "T-101"), (2, "T-103"), (1, "T-102")]:
        heapq.heappush(pq, (pr, tid))             # tuples compare by priority, then id
    print(f"  heapq priority order: {[heapq.heappop(pq)[1] for _ in range(len(pq))]}")
    print(f"  heapq.nlargest(2, [5,1,9,7]) = {heapq.nlargest(2, [5, 1, 9, 7])}")


def records() -> None:
    print("\n=== namedtuple (lightweight immutable record) ===")
    Point = namedtuple("Point", "x y")
    p = Point(3, 4)
    print(f"  {p}, p.x={p.x}, unpack x,y = {tuple(p)}, _replace -> {p._replace(x=10)}")
    nums = [5, 3, 9, 1, 9, 7]
    print(f"  sorted {sorted(nums)}, reversed {list(reversed(nums))}, max {max(nums)}, count(9) {nums.count(9)}")


if __name__ == "__main__":
    sets()
    hashing()
    dicts()
    queues()
    records()
