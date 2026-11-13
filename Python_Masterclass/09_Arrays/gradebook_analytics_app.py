"""
============================================================================
 SECTION 09 : REAL-WORLD APP - Class Gradebook Analytics (Python edition)
============================================================================

 Same marks register as GradebookAnalyticsApp.java (students x subjects), but
 using Python sequence tools:
  - zip(*marks) turns rows into columns (per-subject view) in one line
  - sorted(range(n), key=...) builds a rank list without touching the data
  - enumerate / max(..., key=...) for toppers
  - '#' * n for histogram bars, statistics.mean / median / pstdev

 HOW TO RUN:  python3 gradebook_analytics_app.py
============================================================================
"""
import statistics

SUBJECTS = ["DSA", "Java", "DBMS", "OS", "Maths"]
STUDENTS = ["Srikar", "Himesh", "Krrish", "Sathvik", "Praveen", "Ananya", "Meera"]
MARKS = [
    [88, 94, 76, 81, 90],
    [72, 65, 80, 58, 70],
    [91, 85, 88, 92, 79],
    [45, 38, 62, 55, 49],
    [67, 74, 59, 61, 83],
    [95, 89, 93, 87, 98],
    [58, 71, 35, 66, 60],
]
PASS_MARK = 40
GRADE_CUTOFFS = [(90, "O"), (80, "A"), (70, "B"), (60, "C"), (50, "D"), (0, "F")]


def grade(percent: float) -> str:
    return next(g for cutoff, g in GRADE_CUTOFFS if percent >= cutoff)


def main() -> None:
    n, m = len(STUDENTS), len(SUBJECTS)
    totals = [sum(row) for row in MARKS]
    percents = [t / (m * 100) * 100 for t in totals]

    print("=== STUDENT REPORT ===")
    print(f"  {'Name':<8} {str(SUBJECTS):<34} Total    %   Grade")
    for name, row, total, pct in zip(STUDENTS, MARKS, totals, percents):
        print(f"  {name:<8} {str(row):<34} {total:5d}  {pct:5.1f}   {grade(pct)}")

    print("\n=== SUBJECT ANALYTICS (columns via zip(*MARKS)) ===")
    for subject, column in zip(SUBJECTS, zip(*MARKS)):
        best = max(range(n), key=lambda s: column[s])
        passed = sum(1 for v in column if v >= PASS_MARK)
        print(f"  {subject:<6} avg {statistics.mean(column):5.1f} | median {statistics.median(column):4.0f} | "
              f"stdev {statistics.pstdev(column):4.1f} | topper {STUDENTS[best]:<7} ({column[best]}) | pass {passed / n:4.0%}")

    print("\n=== RANK LIST ===")
    for rank, s in enumerate(sorted(range(n), key=lambda i: totals[i], reverse=True), start=1):
        print(f"  #{rank} {STUDENTS[s]:<8} {totals[s]} ({percents[s]:.1f}%)")

    print("\n=== GRADE DISTRIBUTION ===")
    grades = [grade(p) for p in percents]
    for _, g in GRADE_CUTOFFS:
        c = grades.count(g)
        print(f"  {g} | {'#' * (c * 3):<12} {c}")

    print(f"\n=== SUPPLEMENTARY EXAM ALERTS (any subject < {PASS_MARK}) ===")
    alerts = {name: [sub for sub, v in zip(SUBJECTS, row) if v < PASS_MARK] for name, row in zip(STUDENTS, MARKS)}
    alerts = {k: v for k, v in alerts.items() if v}
    for name, subs in alerts.items():
        print(f"  {name} -> re-exam in: {' '.join(subs)}")
    print("  none" if not alerts else "")

    avg = statistics.mean(percents)
    print(f"Class average {avg:.1f}% | above average: {sum(p > avg for p in percents)} of {n} students")


if __name__ == "__main__":
    main()
