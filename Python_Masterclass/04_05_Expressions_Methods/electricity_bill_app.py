"""
============================================================================
 SECTIONS 04-05 : REAL-WORLD APP - Electricity Bill Generator (Python edition)
============================================================================

 Same tariff as ElectricityBillApp.java (illustrative, not a real state tariff):
   Domestic : 0-100 units free | 101-200 Rs 2.25 | 201-400 Rs 4.50 |
              401-500 Rs 6.00 | 500+ Rs 8.00
   Commercial: flat Rs 8.50/unit
   Fixed charge Rs 50 / Rs 150, electricity duty 5% of the energy charge

 What changes in Python
  - The three Java overloads become ONE function with defaults and
    keyword-only arguments: generate_bill(units=None, *, commercial=False,
    readings=None)
  - Slabs are DATA (a list of tuples) instead of hard-coded if-chains, so the
    tariff can be edited without touching the logic
  - The bill is returned as a dataclass (a small record type, see Section 07)

 HOW TO RUN:  python3 electricity_bill_app.py
============================================================================
"""
from dataclasses import dataclass

DOMESTIC_SLABS = [          # (from_unit, to_unit, rate)
    (101, 200, 2.25),
    (201, 400, 4.50),
    (401, 500, 6.00),
    (501, float("inf"), 8.00),
]
COMMERCIAL_RATE = 8.50
FIXED = {"domestic": 50.0, "commercial": 150.0}
DUTY_RATE = 0.05


@dataclass
class Bill:
    units: int
    kind: str
    energy: float
    fixed: float
    duty: float

    @property
    def total(self) -> float:
        return self.energy + self.fixed + self.duty

    def __str__(self) -> str:
        return (f"{self.kind.upper():<10} {self.units:4d} units | energy Rs {self.energy:8.2f} + fixed Rs {self.fixed:6.2f}"
                f" + duty Rs {self.duty:6.2f} = Rs {self.total:9.2f}")


def units_in_slab(units: int, start: int, end: float) -> int:
    return 0 if units < start else int(min(units, end) - start + 1)


def energy_charge(units: int, commercial: bool) -> float:
    if commercial:
        return units * COMMERCIAL_RATE
    return sum(units_in_slab(units, s, e) * rate for s, e, rate in DOMESTIC_SLABS)


def generate_bill(units: int | None = None, *, commercial: bool = False,
                  readings: tuple[int, int] | None = None) -> Bill | None:
    """One flexible entry point replaces Java's three overloads."""
    if readings is not None:
        previous, current = readings
        if current < previous:
            print(f"  ERROR: meter reading went backwards ({previous} -> {current})")
            return None
        print(f"  Meter: {previous} -> {current}")
        units = current - previous
    if units is None or units < 0:
        print("  ERROR: units must be a non-negative number")
        return None
    kind = "commercial" if commercial else "domestic"
    energy = energy_charge(units, commercial)
    bill = Bill(units, kind, energy, FIXED[kind], energy * DUTY_RATE)
    print("  " + str(bill))
    return bill


def usage_band(units: int) -> str:
    if units <= 100:
        return "LOW (fully subsidised)"
    elif units <= 300:
        return "MODERATE"
    elif units <= 500:
        return "HIGH"
    return "VERY HIGH - consider a solar rooftop"


def main() -> None:
    print("=== MONTHLY BILLS ===")
    for units in (80, 150, 320, 480, 750):
        bill = generate_bill(units)
        assert bill is not None                  # narrows "Bill | None" to "Bill" for the type checker
        print(f"             usage band: {usage_band(units)}, average Rs {bill.total / units:.2f}/unit")

    print("\n=== SAME USAGE, DIFFERENT CONNECTION TYPE ===")
    generate_bill(320)
    generate_bill(320, commercial=True)

    print("\n=== FROM METER READINGS ===")
    generate_bill(readings=(10_450, 10_712))
    generate_bill(readings=(5_000, 4_900))
    generate_bill(-5)

    print("\n=== WHAT IF? Saving 50 units a month ===")
    before, after = generate_bill(430), generate_bill(380)
    assert before and after
    saving = before.total - after.total
    print(f"  Monthly saving Rs {saving:.2f}, yearly Rs {saving * 12:.2f}")


if __name__ == "__main__":
    main()
