# Java Masterclass (Sections 01-16)

Requires **JDK 17 or newer** (records, sealed types, switch expressions, text blocks,
pattern matching for instanceof).

Every `.java` file is one self-contained program (helper types are nested), so
each runs on its own:

```bash
cd 07_OOP_Classes_Inheritance
javac BankAccountSystem.java && java BankAccountSystem   # classic
java BankAccountSystem.java                              # single-file launcher
```

`06_Control_Flow/AtmMachineApp` runs a scripted demo; pass `live` to type input yourself.

Run everything (bash):

```bash
for f in */*.java; do d=$(dirname "$f"); c=$(basename "$f" .java);   (cd "$d" && javac "$c.java" && echo "=== $c ===" && java "$c"); done
```

Suggested order per folder: read the notes PDF, run the concepts program
(read its comments next to the output), then study and modify the real-world app.

Each module folder also contains `<folder>_Notes.pdf` with illustrated theory notes.

| Module | Topics | Real-world app | Files |
|---|---|---|---|
| `01_02_Setup_First_Program` | Setup, the JDK & JShell, first program, how source becomes bytecode | Environment doctor (toolchain health check) | HelloJava.java, SystemInfoApp.java |
| `03_Fundamentals` | Variables, primitive types, casting, operators, char, boolean, String | Loan EMI calculator with amortisation table | JavaFundamentals.java, LoanEmiCalculator.java |
| `04_05_Expressions_Methods` | Expressions, statements, scope, methods, overloading, varargs | Slab-tariff electricity bill generator | ElectricityBillApp.java, MethodsAndExpressions.java |
| `06_Control_Flow` | switch / match, all loops, break/continue, input validation | Menu-driven ATM machine | AtmMachineApp.java, ControlFlow.java |
| `07_OOP_Classes_Inheritance` | Classes, constructors, inheritance, records, String & StringBuilder | Bank account system (savings / current) | BankAccountSystem.java, ClassesAndInheritance.java |
| `08_OOP_Composition_Polymorphism` | Composition, encapsulation, polymorphism, dynamic dispatch | Ride-hailing fare engine | CompositionPolymorphism.java, RideHailingApp.java |
| `09_Arrays` | Arrays, java.util.Arrays, 2-D arrays, Python lists & slicing | Class gradebook analytics | ArraysDeepDive.java, GradebookAnalyticsApp.java |
| `10_Lists_Iterators_Enums` | ArrayList/LinkedList, iterators, autoboxing, enums | Road-trip itinerary planner | ListsIteratorsEnums.java, TravelItineraryApp.java |
| `11_Abstraction_Interfaces` | Interfaces, abstract classes, ABCs & Protocols, template method | Payment gateway (card / UPI / wallet) | AbstractionInterfaces.java, PaymentGatewayApp.java |
| `12_Generics` | Generic classes/methods, bounds, wildcards, erasure, typing generics | Type-safe sports league manager | GenericsDeepDive.java, SportsLeagueApp.java |
| `13_Nested_Classes` | Static nested, inner, local, anonymous classes; closures & decorators | Notification center (event listeners) | NestedClasses.java, NotificationCenterApp.java |
| `14_Lambdas_Functional` | Lambdas, functional interfaces, method references, pipelines | Expense tracker with a rules engine | ExpenseRulesEngineApp.java, LambdasFunctional.java |
| `15_Collections_Framework` | List/Set/Map/Queue/heap families and their Python counterparts | Dark-store inventory & order fulfilment | CollectionsFramework.java, InventoryFulfillmentApp.java |
| `16_Final_Immutable_Sealed` | final, immutable classes, initializers, sealed types | Immutable, auditable bank ledger | FinalImmutableSealed.java, LedgerApp.java |
