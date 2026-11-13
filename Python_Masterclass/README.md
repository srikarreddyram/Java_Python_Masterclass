# Python Masterclass (mirrors Java Sections 01-16)

Requires **Python 3.11 or newer**. Standard library only.

```bash
cd 07_OOP_Classes_Inheritance
python bank_account_system.py
```

Type hints are used throughout. Check them like a compiler would:

```bash
pip install mypy
mypy --strict-optional */*.py     # or one file at a time
```

Lines marked `# type: ignore[...]  # deliberate` are intentional mistakes kept to
show what the checker catches.

Run everything:

```bash
for f in */*.py; do echo "=== $f ==="; (cd "$(dirname "$f")" && python "$(basename "$f")"); done
```

Each module folder also contains `<folder>_Notes.pdf` with illustrated theory notes.

| Module | Topics | Real-world app | Files |
|---|---|---|---|
| `01_02_Setup_First_Program` | Setup, the JDK & JShell, first program, how source becomes bytecode | Environment doctor (toolchain health check) | hello_python.py, system_info_app.py |
| `03_Fundamentals` | Variables, primitive types, casting, operators, char, boolean, String | Loan EMI calculator with amortisation table | loan_emi_calculator.py, python_fundamentals.py |
| `04_05_Expressions_Methods` | Expressions, statements, scope, methods, overloading, varargs | Slab-tariff electricity bill generator | electricity_bill_app.py, functions_and_expressions.py |
| `06_Control_Flow` | switch / match, all loops, break/continue, input validation | Menu-driven ATM machine | atm_machine_app.py, control_flow.py |
| `07_OOP_Classes_Inheritance` | Classes, constructors, inheritance, records, String & StringBuilder | Bank account system (savings / current) | bank_account_system.py, classes_and_inheritance.py |
| `08_OOP_Composition_Polymorphism` | Composition, encapsulation, polymorphism, dynamic dispatch | Ride-hailing fare engine | composition_polymorphism.py, ride_hailing_app.py |
| `09_Arrays` | Arrays, java.util.Arrays, 2-D arrays, Python lists & slicing | Class gradebook analytics | gradebook_analytics_app.py, sequences_deep_dive.py |
| `10_Lists_Iterators_Enums` | ArrayList/LinkedList, iterators, autoboxing, enums | Road-trip itinerary planner | lists_iterators_enums.py, travel_itinerary_app.py |
| `11_Abstraction_Interfaces` | Interfaces, abstract classes, ABCs & Protocols, template method | Payment gateway (card / UPI / wallet) | abstraction_protocols.py, payment_gateway_app.py |
| `12_Generics` | Generic classes/methods, bounds, wildcards, erasure, typing generics | Type-safe sports league manager | generics_typing.py, sports_league_app.py |
| `13_Nested_Classes` | Static nested, inner, local, anonymous classes; closures & decorators | Notification center (event listeners) | nested_closures_decorators.py, notification_center_app.py |
| `14_Lambdas_Functional` | Lambdas, functional interfaces, method references, pipelines | Expense tracker with a rules engine | expense_rules_engine_app.py, functional_python.py |
| `15_Collections_Framework` | List/Set/Map/Queue/heap families and their Python counterparts | Dark-store inventory & order fulfilment | collections_python.py, inventory_fulfillment_app.py |
| `16_Final_Immutable_Sealed` | final, immutable classes, initializers, sealed types | Immutable, auditable bank ledger | immutability_python.py, ledger_app.py |
