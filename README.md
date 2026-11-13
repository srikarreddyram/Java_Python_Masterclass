# Java + Python Masterclass

A side-by-side programming course that teaches the same core concepts twice: once in
**Java 17+** and once in idiomatic **Python 3.11+**. It covers course Sections 01-16,
from setup and first program through OOP, generics, lambdas, the collections framework
and final/immutable/sealed classes, with **28 Java and 28 Python programs**.

![Java 17+](https://img.shields.io/badge/Java-17%2B-orange)
![Python 3.11+](https://img.shields.io/badge/Python-3.11%2B-blue)
![Dependencies](https://img.shields.io/badge/dependencies-none-green)

## What's in each module

Every module folder contains:

* a **concepts program**: each language feature demonstrated, with comments explaining the output
* a **real-world app**: a small application (EMI calculator, ATM, payment gateway, ledger...)
  built only from that section's concepts plus earlier ones
* `<folder>_Notes.pdf`: illustrated notes comparing Java and Python side by side, with
  memory/class/flow diagrams, an "under the hood" walkthrough, pitfalls and interview Q&A
  (the same PDF appears in both language trees)

## Repository layout

```
Java_Python_Masterclass/
├── Java_Masterclass/            # Java 17+ sources (see its README)
│   └── 07_OOP_Classes_Inheritance/
│       ├── ClassesAndInheritance.java
│       ├── BankAccountSystem.java
│       └── 07_OOP_Classes_Inheritance_Notes.pdf
└── Python_Masterclass/          # Python 3.11+ sources (see its README)
    └── 07_OOP_Classes_Inheritance/
        ├── classes_and_inheritance.py
        ├── bank_account_system.py
        └── 07_OOP_Classes_Inheritance_Notes.pdf
```

## Quick start

No build tools or third-party packages are needed, only a JDK and/or Python.

```bash
# Java (single-file launcher, JDK 17+)
cd Java_Masterclass/07_OOP_Classes_Inheritance
java BankAccountSystem.java

# Python (3.11+)
cd Python_Masterclass/07_OOP_Classes_Inheritance
python bank_account_system.py
```

See [Java_Masterclass/README.md](Java_Masterclass/README.md) and
[Python_Masterclass/README.md](Python_Masterclass/README.md) for run-everything
scripts, type-checking with mypy, and per-module file lists.

## Suggested study flow

1. Read the module's notes PDF.
2. Run the concepts program and read its comments next to the output.
3. Run the real-world app, then modify it (add a feature, break it on purpose).
4. Compare the Java and Python versions of the same module.

## Modules

1. **01_02_Setup_First_Program**: Setup, the JDK & JShell, first program, how source becomes bytecode  (app: Environment doctor (toolchain health check))
2. **03_Fundamentals**: Variables, primitive types, casting, operators, char, boolean, String  (app: Loan EMI calculator with amortisation table)
3. **04_05_Expressions_Methods**: Expressions, statements, scope, methods, overloading, varargs  (app: Slab-tariff electricity bill generator)
4. **06_Control_Flow**: switch / match, all loops, break/continue, input validation  (app: Menu-driven ATM machine)
5. **07_OOP_Classes_Inheritance**: Classes, constructors, inheritance, records, String & StringBuilder  (app: Bank account system (savings / current))
6. **08_OOP_Composition_Polymorphism**: Composition, encapsulation, polymorphism, dynamic dispatch  (app: Ride-hailing fare engine)
7. **09_Arrays**: Arrays, java.util.Arrays, 2-D arrays, Python lists & slicing  (app: Class gradebook analytics)
8. **10_Lists_Iterators_Enums**: ArrayList/LinkedList, iterators, autoboxing, enums  (app: Road-trip itinerary planner)
9. **11_Abstraction_Interfaces**: Interfaces, abstract classes, ABCs & Protocols, template method  (app: Payment gateway (card / UPI / wallet))
10. **12_Generics**: Generic classes/methods, bounds, wildcards, erasure, typing generics  (app: Type-safe sports league manager)
11. **13_Nested_Classes**: Static nested, inner, local, anonymous classes; closures & decorators  (app: Notification center (event listeners))
12. **14_Lambdas_Functional**: Lambdas, functional interfaces, method references, pipelines  (app: Expense tracker with a rules engine)
13. **15_Collections_Framework**: List/Set/Map/Queue/heap families and their Python counterparts  (app: Dark-store inventory & order fulfilment)
14. **16_Final_Immutable_Sealed**: final, immutable classes, initializers, sealed types  (app: Immutable, auditable bank ledger)

## Notes

* `06_Control_Flow` ATM apps run a scripted demo by default; pass `live` to type input yourself.
* Python files use type hints throughout. Lines marked `# type: ignore[...]  # deliberate`
  are intentional mistakes that show what the type checker catches.
