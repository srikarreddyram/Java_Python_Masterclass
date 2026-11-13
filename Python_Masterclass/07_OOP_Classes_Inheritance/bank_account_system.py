"""
============================================================================
 SECTION 07 : REAL-WORLD APP - Bank Account System (Python edition)
============================================================================

                 BankAccount (base)
               /                  \\
      SavingsAccount          CurrentAccount
      interest + min balance  overdraft + monthly fee

 Same behaviour as BankAccountSystem.java. Python-specific techniques:
  - itertools.count() as the account-number generator (class attribute)
  - @property for a read-only balance; _record() as the single writer
  - dataclasses for Customer/Transaction (frozen = Java record)
  - a class attribute PREFIX overridden in subclasses (no method needed)
  - __str__ / __repr__ / __len__ / __iter__ so accounts behave like
    built-in objects (len(account), for t in account)

 HOW TO RUN:  python3 bank_account_system.py
============================================================================
"""
from dataclasses import dataclass
from itertools import count


@dataclass(frozen=True)
class Customer:
    name: str
    email: str


@dataclass(frozen=True)
class Transaction:
    kind: str
    amount: float
    balance_after: float
    note: str = ""

    def __str__(self) -> str:
        sign = "+" if self.amount >= 0 else ""
        return f"    {self.kind:<9} {sign + format(self.amount, ',.2f'):>12}  bal {self.balance_after:>12,.2f}  {self.note}"


class BankAccount:
    PREFIX = "AC"
    _numbers = count(1001)                          # shared generator: 1001, 1002, ...

    def __init__(self, holder: Customer, opening: float):
        self.number = f"{self.PREFIX}{next(BankAccount._numbers)}"   # self.PREFIX finds the SUBCLASS value
        self.holder = holder
        self._balance = 0.0
        self._history: list[Transaction] = []
        self._record("OPEN", opening, "opening deposit")

    @property
    def balance(self) -> float:                     # read-only from outside
        return self._balance

    def deposit(self, amount: float) -> None:
        if amount <= 0:
            raise ValueError("Deposit must be positive")
        self._record("DEPOSIT", amount)

    def withdraw(self, amount: float, note: str = "") -> bool:
        if amount <= 0 or not self.can_withdraw(amount):
            print(f"  [{self.number}] DECLINED withdrawal of Rs {amount:,.2f} ({self.decline_reason()})")
            return False
        self._record("WITHDRAW", -amount, note)
        return True

    # ---- hooks for subclasses ----
    def can_withdraw(self, amount: float) -> bool:
        return amount <= self._balance

    def decline_reason(self) -> str:
        return "insufficient funds"

    def month_end(self) -> None:
        pass

    # ---- the only place that changes the balance ----
    def _record(self, kind: str, signed_amount: float, note: str = "") -> None:
        self._balance += signed_amount
        self._history.append(Transaction(kind, signed_amount, self._balance, note))

    # ---- make accounts feel like built-ins ----
    def __len__(self) -> int:
        return len(self._history)

    def __iter__(self):
        return iter(self._history)

    def __str__(self) -> str:
        return f"{self.number} {type(self).__name__:<15} {self.holder.name:<8} Rs {self._balance:>12,.2f}"


class SavingsAccount(BankAccount):
    PREFIX = "SB"
    MIN_BALANCE = 1_000

    def __init__(self, holder: Customer, opening: float, annual_rate: float = 3.5):
        super().__init__(holder, opening)
        self.annual_rate = annual_rate

    def can_withdraw(self, amount: float) -> bool:
        return self.balance - amount >= self.MIN_BALANCE

    def decline_reason(self) -> str:
        return f"would go below minimum balance Rs {self.MIN_BALANCE:,}"

    def month_end(self) -> None:
        self._record("INTEREST", self.balance * self.annual_rate / 12 / 100, f"{self.annual_rate}% p.a.")


class CurrentAccount(BankAccount):
    PREFIX = "CA"
    MONTHLY_FEE = 250

    def __init__(self, holder: Customer, opening: float, overdraft_limit: float):
        super().__init__(holder, opening)
        self.overdraft_limit = overdraft_limit

    def can_withdraw(self, amount: float) -> bool:
        return self.balance - amount >= -self.overdraft_limit

    def decline_reason(self) -> str:
        return f"exceeds overdraft limit Rs {self.overdraft_limit:,.0f}"

    def month_end(self) -> None:
        self._record("FEE", -self.MONTHLY_FEE, "account maintenance")
        if self.balance < 0:
            self._record("OD-INT", self.balance * 0.15 / 12, "overdraft interest")


def transfer(src: BankAccount, dst: BankAccount, amount: float) -> bool:
    if src.withdraw(amount, f"transfer to {dst.number}"):
        dst._record("TRANSFER", amount, f"from {src.number}")   # same 'package' - allowed by convention
        print(f"  Transferred Rs {amount:,.2f} {src.number} -> {dst.number}")
        return True
    return False


def main() -> None:
    srikar, meera = Customer("Srikar", "srikar@example.com"), Customer("Meera", "meera@example.com")
    s1 = SavingsAccount(srikar, 25_000)
    s2 = SavingsAccount(meera, 1_50_000, 4.0)
    c1 = CurrentAccount(meera, 40_000, 50_000)
    accounts = [s1, s2, c1]

    print("=== ACCOUNTS OPENED ===")
    for a in accounts:
        print(f"  {a}")

    print("\n=== DAILY ACTIVITY ===")
    s1.deposit(12_000)
    s1.withdraw(35_500, "laptop")
    s1.withdraw(1_000, "dinner")
    c1.withdraw(75_000, "supplier payment")
    c1.withdraw(20_000, "rent")
    transfer(s2, s1, 10_000)
    transfer(s1, c1, 50_000)
    try:
        s1.balance = 10**9  # type: ignore[misc]  # deliberate: read-only property
    except AttributeError:
        print("  s1.balance = 1e9 -> AttributeError (read-only property)")

    print("\n=== MONTH END (each subclass runs its own rule) ===")
    for a in accounts:
        a.month_end()
        print(f"  {a}")

    for acc in (s1, c1):
        print(f"\n=== PASSBOOK: {acc.number} ({len(acc)} entries, via __len__) ===")
        for t in acc:                                   # works because of __iter__
            print(t)

    print(f"\nBank's net balance across accounts: Rs {sum(a.balance for a in accounts):,.2f}")


if __name__ == "__main__":
    main()
