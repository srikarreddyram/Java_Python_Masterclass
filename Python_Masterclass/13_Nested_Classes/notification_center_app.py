"""
============================================================================
 SECTION 13 : REAL-WORLD APP - Notification Center (Python edition)
============================================================================

 Same event/listener system as NotificationCenterApp.java. In Python the
 listeners don't need to be classes at all - any CALLABLE works:
   - SMS channel      : a plain function
   - Email channel    : a CLOSURE that keeps its own 'sent' counter (nonlocal)
   - Push channel     : built by a FACTORY function with quiet-hours captured
   - Audit channel    : a lambda
   - Subscription     : a nested class whose cancel() is handed the center
   - @center.on(...)  : a DECORATOR that registers a function as a listener
                        (exactly how Flask's @app.route works)

 HOW TO RUN:  python3 notification_center_app.py
============================================================================
"""
from collections import Counter
from collections.abc import Callable
from dataclasses import dataclass
from enum import Enum, auto


class Type(Enum):
    ORDER_PLACED = auto()
    ORDER_DELIVERED = auto()
    PAYMENT_FAILED = auto()
    OTP = auto()
    PROMO = auto()


@dataclass(frozen=True)
class Event:
    type: Type
    user: str
    message: str
    hour: int


Filter = Callable[[Event], bool]
Handler = Callable[[Event], None]


class NotificationCenter:
    class Subscription:                               # nested class; gets the center explicitly
        def __init__(self, center: "NotificationCenter", sub_id: int, name: str):
            self._center, self.id, self.name = center, sub_id, name

        def cancel(self) -> None:
            del self._center._listeners[self.id]
            print(f"  ({self.name} unsubscribed; {len(self._center._listeners)} listeners left)")

    def __init__(self):
        self._listeners: dict[int, tuple[Filter, Handler]] = {}
        self._next_id = 1
        self.delivery_log: list[str] = []

    def subscribe(self, name: str, wants: Filter, handler: Handler) -> "NotificationCenter.Subscription":
        sub = NotificationCenter.Subscription(self, self._next_id, name)
        self._listeners[self._next_id] = (wants, handler)
        self._next_id += 1
        return sub

    def on(self, *types: Type, name: str = "decorated"):
        """Decorator: @center.on(Type.X) registers the function below it."""
        def register(fn: Handler) -> Handler:
            self.subscribe(name, lambda e: e.type in types, fn)
            return fn
        return register

    def publish(self, e: Event) -> None:
        print(f"\n>> {e.hour:02d}:00 {e.type.name} for {e.user}: {e.message}")
        delivered = 0
        for wants, handler in list(self._listeners.values()):
            if wants(e):
                handler(e)
                delivered += 1
        if not delivered:
            print("   (no channel accepted this event)")


def make_email_channel(center: NotificationCenter) -> Handler:
    sent = 0

    def send(e: Event) -> None:
        nonlocal sent                                 # closure state, like a field on an anonymous class
        sent += 1
        print(f"   EMAIL -> to {e.user.lower()}@example.com (#{sent}): {e.message}")
        center.delivery_log.append("EMAIL")
    return send


def make_push_channel(center: NotificationCenter, quiet_from: int, quiet_to: int) -> tuple[Filter, Handler]:
    def is_quiet(hour: int) -> bool:                  # local helper (Java: local class)
        if quiet_from > quiet_to:
            return hour >= quiet_from or hour < quiet_to
        return quiet_from <= hour < quiet_to

    def wants(e: Event) -> bool:
        urgent = e.type in (Type.OTP, Type.PAYMENT_FAILED)
        return urgent or not is_quiet(e.hour)

    def send(e: Event) -> None:
        print(f"   PUSH  -> {e.user}'s phone: {e.message}")
        center.delivery_log.append("PUSH")
    return wants, send


def main() -> None:
    center = NotificationCenter()

    def sms(e: Event) -> None:
        text = f"Your OTP is {e.message}. Do not share it." if e.type is Type.OTP else e.message
        print(f"   SMS   -> {text}")
        center.delivery_log.append("SMS")

    sms_sub = center.subscribe("SMS", lambda e: e.type in (Type.OTP, Type.PAYMENT_FAILED), sms)
    center.subscribe("Email", lambda e: e.type is not Type.OTP, make_email_channel(center))
    center.subscribe("Push", *make_push_channel(center, 22, 7))
    audit_sub = center.subscribe("Audit", lambda e: True, lambda e: print(f"   AUDIT -> stored {e.type.name}"))

    @center.on(Type.ORDER_DELIVERED, name="Rating prompt")
    def ask_for_rating(e: Event) -> None:
        print(f"   RATE  -> {e.user}, how was your order? (registered via decorator)")

    center.publish(Event(Type.ORDER_PLACED, "Srikar", "Order #5531 confirmed, Rs 486", 13))
    center.publish(Event(Type.OTP, "Srikar", "482913", 23))
    center.publish(Event(Type.PROMO, "Meera", "50% off on biryani tonight", 23))

    audit_sub.cancel()
    sms_sub.cancel()
    center.publish(Event(Type.PAYMENT_FAILED, "Ravi", "Payment of Rs 1,250 failed. Retry?", 2))
    center.publish(Event(Type.ORDER_DELIVERED, "Srikar", "Order #5531 delivered", 14))

    print(f"\nDeliveries per channel: {dict(sorted(Counter(center.delivery_log).items()))}")


if __name__ == "__main__":
    main()
