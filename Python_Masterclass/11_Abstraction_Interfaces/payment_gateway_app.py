"""
============================================================================
 SECTION 11 : REAL-WORLD APP - Payment Gateway (Python edition)
============================================================================

  class PaymentMethod(Protocol)    <- what checkout() needs (structural)
  class BaseProcessor(ABC)         <- TEMPLATE METHOD pay(): validate -> fraud
                                      -> authorize -> fee -> receipt
      @abstractmethod authorize / fee / name
  CardProcessor, UpiProcessor, WalletProcessor
  Refundable / Rechargeable        <- optional capabilities as Protocols,
                                      checked at runtime with isinstance()
                                      (needs @runtime_checkable)

 Python notes
  - Python can't mark pay() 'final' at runtime; typing.final documents the
    intent and type checkers enforce it.
  - re.fullmatch validates the UPI VPA.
  - Receipts are frozen dataclasses with a custom __str__.

 HOW TO RUN:  python3 payment_gateway_app.py
============================================================================
"""
import re
from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from typing import Protocol, final, runtime_checkable


@dataclass(frozen=True)
class Order:
    id: str
    customer: str
    amount: float


@dataclass(frozen=True)
class Receipt:
    order_id: str
    method: str
    amount: float
    fee: float
    status: str

    def __str__(self) -> str:
        return f"  {self.order_id:<6} {self.method:<18} Rs {self.amount:>10,.2f}  fee Rs {self.fee:6.2f}  {self.status}"


class PaymentMethod(Protocol):
    def pay(self, order: Order) -> Receipt: ...


@runtime_checkable
class Refundable(Protocol):
    def refund(self, order: Order, amount: float) -> float: ...


@runtime_checkable
class Rechargeable(Protocol):
    def top_up(self, amount: float) -> None: ...


class BaseProcessor(ABC):
    FRAUD_LIMIT = 2_00_000

    def __init__(self):
        self.log: list[str] = []

    @property
    @abstractmethod
    def name(self) -> str: ...

    @abstractmethod
    def authorize(self, order: Order) -> bool: ...

    @abstractmethod
    def fee(self, amount: float) -> float: ...

    @final
    def pay(self, order: Order) -> Receipt:          # TEMPLATE METHOD
        if order.amount <= 0:
            return self._fail(order, "invalid amount")
        if order.amount > self.FRAUD_LIMIT:
            return self._fail(order, "flagged by fraud check")
        if not self.authorize(order):
            return self._fail(order, "authorisation declined")
        self.log.append(f"captured {order.id}")
        return Receipt(order.id, self.name, order.amount, round(self.fee(order.amount), 2), "SUCCESS")

    def _fail(self, order: Order, reason: str) -> Receipt:
        self.log.append(f"failed {order.id}: {reason}")
        return Receipt(order.id, self.name, order.amount, 0, f"FAILED ({reason})")


class CardProcessor(BaseProcessor):
    def __init__(self, last4: str, credit_limit: float):
        super().__init__()
        self.last4, self.credit_limit, self.used = last4, credit_limit, 0.0

    @property
    def name(self) -> str:
        return f"Card **{self.last4}"

    def authorize(self, order: Order) -> bool:
        if order.amount > 5_000:
            self.log.append(f"3-D Secure OTP verified for {order.id}")
        if self.used + order.amount > self.credit_limit:
            return False
        self.used += order.amount
        return True

    def fee(self, amount: float) -> float:
        return amount * 0.02

    def refund(self, order: Order, amount: float) -> float:   # makes it Refundable (structurally)
        self.used -= amount
        self.log.append(f"refunded Rs {amount} for {order.id}")
        return amount


class UpiProcessor(BaseProcessor):
    VPA = re.compile(r"[\w.]+@\w+")

    def __init__(self, vpa: str):
        super().__init__()
        self.vpa = vpa

    @property
    def name(self) -> str:
        return f"UPI {self.vpa}"

    def authorize(self, order: Order) -> bool:
        return bool(self.VPA.fullmatch(self.vpa)) and order.amount <= 1_00_000

    def fee(self, amount: float) -> float:
        return 0.0

    def refund(self, order: Order, amount: float) -> float:
        self.log.append(f"UPI reversal Rs {amount}")
        return amount


class WalletProcessor(BaseProcessor):
    def __init__(self, opening: float):
        super().__init__()
        self.balance = opening

    @property
    def name(self) -> str:
        return "Wallet"

    def authorize(self, order: Order) -> bool:
        if order.amount > self.balance:
            return False
        self.balance -= order.amount
        return True

    def fee(self, amount: float) -> float:
        return min(amount * 0.01, 25)

    def top_up(self, amount: float) -> None:
        self.balance += amount


def checkout(order: Order, method: PaymentMethod) -> Receipt:
    return method.pay(order)


@dataclass
class Ledger:
    revenue: float = 0.0
    fees: float = 0.0
    receipts: list[Receipt] = field(default_factory=list)

    def add(self, r: Receipt) -> None:
        self.receipts.append(r)
        if r.status == "SUCCESS":
            self.revenue += r.amount
            self.fees += r.fee


def main() -> None:
    card, upi = CardProcessor("4242", 60_000), UpiProcessor("srikar@okbank")
    bad_upi, wallet = UpiProcessor("not-a-vpa"), WalletProcessor(1_500)
    cart = [
        (Order("#1001", "Srikar", 3_499), card),
        (Order("#1002", "Srikar", 54_999), card),
        (Order("#1003", "Meera", 12_750), upi),
        (Order("#1004", "Meera", 899), bad_upi),
        (Order("#1005", "Ravi", 2_400), wallet),
        (Order("#1006", "Ravi", 3_50_000), upi),
    ]
    ledger = Ledger()
    print("=== CHECKOUT ===")
    for order, method in cart:
        r = checkout(order, method)
        ledger.add(r)
        print(r)

    print("\n=== CAPABILITIES (runtime_checkable Protocols) ===")
    wallet.top_up(2_000)
    print(f"  wallet topped up -> balance Rs {wallet.balance:,.2f}")
    r = checkout(Order("#1005", "Ravi", 2_400), wallet)
    ledger.add(r)
    print(r)
    for m in (card, upi, wallet):
        print(f"  {m.name:<18} refunds: {'yes' if isinstance(m, Refundable) else 'no':<3}  "
              f"top-ups: {'yes' if isinstance(m, Rechargeable) else 'no'}")

    print("\n=== CUSTOMER RETURNS ORDER #1001 (paid by card) ===")
    used_method: PaymentMethod = card
    if isinstance(used_method, Refundable):
        ledger.revenue -= used_method.refund(Order("#1001", "Srikar", 3_499), 3_499)
        print(f"  refunded Rs 3,499.00 to {card.name}")

    print(f"\nSettled revenue Rs {ledger.revenue:,.2f} | gateway fees Rs {ledger.fees:,.2f} | "
          f"merchant receives Rs {ledger.revenue - ledger.fees:,.2f}")
    print(f"Card processor audit log: {card.log}")


if __name__ == "__main__":
    main()
