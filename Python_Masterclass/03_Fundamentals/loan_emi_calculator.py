"""
============================================================================
 SECTION 03 : REAL-WORLD APP - Loan EMI Calculator (Python edition)
============================================================================

        EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
   P = principal, r = monthly rate (annual% / 12 / 100), n = months

 Same maths as LoanEmiCalculator.java, written the Python way:
  - ** instead of Math.pow, ints that never overflow
  - Decimal for the final rupee figure (like BigDecimal)
  - f-string format spec  {x:,.2f}  instead of printf("%,.2f")
  - Indian digit grouping (12,34,567.89) done with a small helper, because
    Python's ',' format gives Western grouping (1,234,567.89)

 HOW TO RUN:  python3 loan_emi_calculator.py
============================================================================
"""
from decimal import ROUND_HALF_UP, Decimal


def emi(principal: float, annual_rate_percent: float, months: int) -> float:
    r = annual_rate_percent / 12 / 100
    if r == 0:
        return principal / months
    growth = (1 + r) ** months
    return principal * r * growth / (growth - 1)


def inr(amount: float) -> str:
    """Format with Indian grouping: 5000000 -> 50,00,000.00"""
    sign = "-" if amount < 0 else ""
    whole, frac = f"{abs(amount):.2f}".split(".")
    if len(whole) > 3:
        head, tail = whole[:-3], whole[-3:]
        groups: list[str] = []
        while len(head) > 2:
            groups.insert(0, head[-2:])
            head = head[:-2]
        if head:
            groups.insert(0, head)
        whole = ",".join(groups) + "," + tail
    return f"{sign}Rs {whole}.{frac}"


def schedule(title: str, principal: float, rate: float, months: int, show_rows: int) -> None:
    e = emi(principal, rate, months)
    print(f"=== {title} ===")
    print(f"  Principal {inr(principal)} | Rate {rate:.2f}% p.a. | Tenure {months} months ({months // 12} yrs)")
    print(f"  EMI = Rs {Decimal(str(e)).quantize(Decimal('0.01'), ROUND_HALF_UP)}")
    print("  Month |         EMI |   Interest |  Principal |          Balance")
    balance, total_interest = principal, 0.0
    for m in range(1, months + 1):
        interest = balance * rate / 12 / 100
        principal_part = e - interest
        balance -= principal_part
        total_interest += interest
        if m <= show_rows or m > months - 2:
            print(f"  {m:5d} | {e:11,.2f} | {interest:10,.2f} | {principal_part:10,.2f} | {max(balance, 0):16,.2f}")
        elif m == show_rows + 1:
            print("    ... |         ... |        ... |        ... |              ...")
    print(f"  Total paid {inr(e * months)}  (interest {inr(total_interest)} = {total_interest / principal:.1%} of the loan)\n")


def main() -> None:
    schedule("Home loan", 50_00_000, 8.5, 20 * 12, 3)
    schedule("Car loan", 8_00_000, 9.25, 5 * 12, 3)
    schedule("Education loan", 15_00_000, 10.0, 7 * 12, 2)

    print("=== Same Rs 50 lakh at 8.5%: tenure vs total interest ===")
    for years in (10, 15, 20, 25, 30):
        e = emi(50_00_000, 8.5, years * 12)
        interest = e * years * 12 - 50_00_000
        flag = "interest > principal!" if interest > 50_00_000 else ""
        print(f"  {years:2d} yrs: EMI {inr(e):>15}  total interest {inr(interest):>17}  {flag}")


if __name__ == "__main__":
    main()
