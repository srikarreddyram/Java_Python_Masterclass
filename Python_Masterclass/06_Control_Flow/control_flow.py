"""
============================================================================
 SECTION 06 : CONTROL FLOW - match-case, for/while (+ else), break/continue,
              range/enumerate/zip, input parsing with try/except
              (Python track - compare with ControlFlow.java)
============================================================================

 SWITCH -> match-case (Python 3.10+)
   Java: switch (day) { case 6, 7 -> "weekend"; default -> "weekday"; }
   Py  : match day:
             case 6 | 7: ...        # | = several patterns
             case _: ...            # _ = default
   match is STRUCTURAL pattern matching: it can also destructure tuples,
   lists, dicts and objects, and add guards:  case (x, y) if x == y:
   No fall-through, no break needed.

 LOOPS
   Java for (int i = 0; i < n; i++)  ->  for i in range(n):
   Java for (T x : items)            ->  for x in items:
   need the index too?               ->  for i, x in enumerate(items):
   two lists in step?                ->  for a, b in zip(xs, ys):
   while cond:            (Python has no do-while; use while True + break)
   for/while ... else:    the else runs only if the loop did NOT break
   No labelled break: put nested loops in a function and 'return', or use
   a flag / for-else.

 INPUT: input() always returns a str -> convert with int()/float() inside
 try/except ValueError (Java: Scanner + NumberFormatException).

 HOW TO RUN:  python3 control_flow.py
============================================================================
"""
import io


def match_demos() -> None:
    print("=== match-case (Python's switch) ===")
    for day in (3, 6, 9):
        match day:
            case 1 | 2 | 3 | 4 | 5:
                kind = "weekday"
            case 6 | 7:
                kind = "weekend"
            case _:
                kind = "invalid day"
        print(f"  day {day}: {kind}")

    for month in ("FEB", "APR", "DEC"):
        match month:
            case "APR" | "JUN" | "SEP" | "NOV":
                days = 30
            case "FEB":
                year = 2028
                days = 29 if (year % 4 == 0 and year % 100 != 0) or year % 400 == 0 else 28
            case _:
                days = 31
        print(f"  {month} has {days} days")

    # Structural patterns: things Java's classic switch cannot do
    commands: list[object] = ["quit", ("move", 3, 4), ("move", 0, 0), {"cmd": "say", "text": "hi"}, [1, 2, 3, 4], 42]
    for c in commands:
        match c:
            case "quit":
                out = "quit the game"
            case ("move", 0, 0):
                out = "move to origin"
            case ("move", int(x), y) if x > 0:          # capture + type check + guard
                out = f"move right to ({x}, {y})"
            case {"cmd": "say", "text": text}:          # dict pattern
                out = f"say {text!r}"
            case [first, *rest]:                        # sequence pattern with a star
                out = f"list starting {first}, {len(rest)} more"
            case int(n):                                 # class pattern
                out = f"an int: {n}"
            case _:
                out = "unknown"
        print(f"  {c!r:<34} -> {out}")


def loop_demos() -> None:
    print("\n=== LOOPS ===")
    print("  range       : 7 x table ->", " ".join(str(7 * i) for i in range(1, 6)))
    print("  range(start, stop, step):", list(range(10, 0, -3)))
    for i, fruit in enumerate(["mango", "banana", "guava"], start=1):
        print(f"  enumerate   : {i}. {fruit}")
    for name, marks in zip(["Asha", "Ravi"], [91, 78]):
        print(f"  zip         : {name} scored {marks}")

    money, years = 1000.0, 0
    while money < 2000:
        money *= 1.07
        years += 1
    print(f"  while       : Rs 1000 at 7% doubles in {years} years (Rs {money:.2f})")

    attempts = 0
    while True:                                          # Python's do-while
        attempts += 1
        if attempts >= 1:
            break
    print(f"  do-while    : 'while True ... break' ran {attempts} time(s)")

    number, total = 98_765, 0
    while number:
        number, digit = divmod(number, 10)
        total += digit
    print(f"  digit sum   : {total}")


def break_continue_else() -> None:
    print("\n=== BREAK / CONTINUE / LOOP-ELSE ===")
    print("  continue    : odd numbers ->", [i for i in range(1, 11) if i % 2])

    n = 101
    while True:
        for d in range(2, int(n ** 0.5) + 1):
            if n % d == 0:
                break                # not prime
        else:                        # no break happened -> prime
            break
        n += 1
    print(f"  for-else    : first prime > 100 = {n}")

    grid = [[3, 8, 1], [9, 42, 7], [5, 2, 6]]

    def find(target):               # replaces Java's labelled break: return exits both loops
        for r, row in enumerate(grid):
            for c, v in enumerate(row):
                if v == target:
                    return r, c
        return None
    print(f"  nested loop exit via return: 42 at {find(42)}, 99 at {find(99)}")


def input_parsing() -> None:
    print("\n=== INPUT VALIDATION (input() simulated with a StringIO) ===")
    fake_stdin = io.StringIO("abc\n-5\n200\n42\n")
    while True:
        line = fake_stdin.readline().strip()            # real code: line = input("Enter age: ")
        print(f"  Enter age: {line} -> ", end="")
        try:
            age = int(line)
        except ValueError:
            print("not a number, try again")
            continue
        if not 0 <= age <= 150:
            print("out of range, try again")
            continue
        print("accepted")
        break
    print(f"  Final age = {age}")


if __name__ == "__main__":
    match_demos()
    loop_demos()
    break_continue_else()
    input_parsing()
