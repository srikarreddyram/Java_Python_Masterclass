"""
============================================================================
 SECTION 06 : REAL-WORLD APP - ATM Machine (Python edition)
============================================================================

 Same rules as AtmMachineApp.java:
  - 3 PIN attempts, then the card is blocked
  - withdrawals: multiples of Rs 100, max Rs 20,000 per txn, Rs 40,000 per day
  - fewest notes (500/200/100), mini statement

 Python differences worth noticing
  - the menu dispatch is a match-case on the choice
  - the script is fed through an iterator (iter(...)), and next() reads the
    next "keystroke" - pass 'live' to use real input() instead
  - state lives in a small class instead of static fields
  - the guard-clause style is identical to the Java version

 HOW TO RUN:  python3 atm_machine_app.py        (or: python3 atm_machine_app.py live)
============================================================================
"""
import sys

CORRECT_PIN, PER_TXN_LIMIT, DAILY_LIMIT = 4321, 20_000, 40_000

SCRIPT = ["1111", "4321",               # wrong PIN, then correct
          "1",                          # balance
          "2", "2500",                  # withdraw
          "2", "1250",                  # not a multiple of 100
          "2", "25000",                 # above the per-transaction limit
          "3", "abc", "5000",           # deposit: bad input, then 5000
          "2", "18000", "2", "20000",   # 2,500 + 18,000 + 20,000 > daily limit
          "4", "9", "5"]                # statement, invalid option, exit


class Atm:
    def __init__(self, live: bool):
        self.balance = 52_750
        self.withdrawn_today = 0
        self.statement: list[str] = []
        self.live = live
        self._script = iter(SCRIPT)

    def read_int(self, prompt: str) -> int:
        while True:
            if self.live:
                line = input(f"  {prompt}: ").strip()
            else:
                line = next(self._script)
                print(f"  {prompt}: {'****' if prompt.startswith('Enter PIN') else line}")
            try:
                return int(line)
            except ValueError:
                print(f"  {line!r} is not a number.")

    def authenticate(self) -> bool:
        for attempt in range(1, 4):
            if self.read_int(f"Enter PIN (attempt {attempt}/3)") == CORRECT_PIN:
                print("  PIN verified.")
                return True
            print("  Wrong PIN.")
        return False

    @staticmethod
    def notes_for(amount: int) -> str:
        parts = []
        for note in (500, 200, 100):
            count, amount = divmod(amount, note)
            if count:
                parts.append(f"{count} x Rs {note}")
        return "  ".join(parts)

    def withdraw(self, amount: int) -> None:
        if amount <= 0:
            print("  Amount must be positive."); return
        if amount % 100:
            print("  Please enter a multiple of Rs 100."); return
        if amount > PER_TXN_LIMIT:
            print(f"  Max Rs {PER_TXN_LIMIT:,} per transaction."); return
        if self.withdrawn_today + amount > DAILY_LIMIT:
            print(f"  Daily limit reached: you can withdraw Rs {DAILY_LIMIT - self.withdrawn_today:,} more today."); return
        if amount > self.balance:
            print("  Insufficient funds."); return
        self.balance -= amount
        self.withdrawn_today += amount
        print(f"  Dispensing: {self.notes_for(amount)}")
        print(f"  Please collect Rs {amount:,}. Balance Rs {self.balance:,}")
        self.statement.append(f"  WITHDRAW  -Rs {amount:>7,}   balance Rs {self.balance:>9,}")

    def deposit(self, amount: int) -> None:
        if amount <= 0 or amount % 100:
            print("  Deposit a positive multiple of Rs 100."); return
        self.balance += amount
        print(f"  Rs {amount:,} deposited. Balance Rs {self.balance:,}")
        self.statement.append(f"  DEPOSIT   +Rs {amount:>7,}   balance Rs {self.balance:>9,}")

    def run(self) -> None:
        print("===== WELCOME TO DSA BANK ATM =====")
        if not self.authenticate():
            print("Card BLOCKED after 3 wrong PINs. Please contact your branch.")
            return
        while True:
            print("\n[1] Balance  [2] Withdraw  [3] Deposit  [4] Mini statement  [5] Exit")
            match self.read_int("Choose option"):
                case 1:
                    print(f"  Available balance: Rs {self.balance:,}")
                case 2:
                    self.withdraw(self.read_int("Amount to withdraw"))
                case 3:
                    self.deposit(self.read_int("Amount to deposit"))
                case 4:
                    print("\n".join(self.statement) or "  (no transactions)")
                case 5:
                    print("  Thank you for banking with us. Please collect your card.")
                    break
                case other:
                    print(f"  Invalid option {other}, try again.")


if __name__ == "__main__":
    Atm(live=len(sys.argv) > 1 and sys.argv[1] == "live").run()
