# Lecture 06 — Revision Notes (Theory + Code Map)

Use this file while revising. Each topic points to the demo code where the same idea is commented.

| Topic | Theory (this file) | Code |
|-------|--------------------|------|
| How `AppConfig` gets registered | §1 | `BeanScopeDemo` / `BeanInitializationDemo` / `CircularDependencyDemo` → `AppConfig.java`, `Main.java` |
| Circular dependency error & check | §2 | `CircularDependencyDemo` |
| How to solve circular dependency | §3 | `CircularDependencyDemo` (`simple/A`, `simple/B`, `OrderService`, `PaymentService`, `Main`) |
| Field vs Constructor injection | §4 | `CircularDependencyDemo` + `BeanInitializationDemo` |
| Does constructor injection break SRP? | §5 | Comments in `OrderService` / `PaymentService` |

---

# §1 How Does `AppConfig` Get Registered?

## The doubt

```java
@Configuration
@ComponentScan
public class AppConfig {
}
```

- `@Configuration` is meta-annotated with `@Component`
- `@ComponentScan` is written **on** `AppConfig` itself

> If `@Configuration` is a kind of `@Component`, and components are found by `@ComponentScan`, then how does Spring scan `AppConfig`?

## Short answer

**`AppConfig` is NOT found by `@ComponentScan`.**

It is registered **manually** in `Main`:

```java
ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
```

## Step-by-step flow

```
Main
  → new AnnotationConfigApplicationContext(AppConfig.class)
      → Spring registers AppConfig as a bean (because of @Configuration)
          → Spring reads @ComponentScan on AppConfig
              → Spring scans the package for OTHER @Component classes
```

| Class | How it enters the container |
|--------|-----------------------------|
| `AppConfig` | Explicit registration via `AnnotationConfigApplicationContext(AppConfig.class)` |
| Other beans | Found by `@ComponentScan` |

## Why `@Configuration` still uses `@Component`

`@Component` has two roles:

1. **Marker** — “this class is a Spring bean candidate”
2. **Scan target** — `@ComponentScan` looks for `@Component` / `@Service` / `@Configuration` etc.

`AppConfig` uses role 1 via **manual registration**, not role 2 via scan.

**Code refs:** comments on `AppConfig.java` + `Main.java` in all three Lecture 06 demos.

---

# §2 Circular Dependency — Error & How to Check

## What is it?

```
OrderService  →  PaymentService
PaymentService  →  OrderService   // loop
```

Plain Java (`simple.A` / `simple.B`):

```
A creates B, B creates A, ... → StackOverflowError
```

## Errors

### Constructor injection cycle (Spring)

```text
BeanCurrentlyInCreationException:
Requested bean is currently in creation: Is there an unresolvable circular reference?
```

Often wrapped as `UnsatisfiedDependencyException`. Cycle in log:

```text
orderService → paymentService → orderService
```

### Field injection cycle

May start in classic Spring Core (early incomplete bean), but **hides** bad design.  
Spring Boot 2.6+ often still fails and prints:

```text
┌─────┐
|  orderService
↑     ↓
|  paymentService
└─────┘
```

### Plain Java recursive `new`

```text
StackOverflowError
```

## How to check

1. **Run the app** — cycle often fails at context startup.
2. **Draw arrows** from every `@Autowired` / constructor param.
3. **Force constructor injection temporarily** — cycles show up loudly.
4. **Read the ASCII cycle** in the stack trace.

**Code refs:** `CircularDependencyDemo/OrderService.java`, `PaymentService.java`

---

# §3 How to SOLVE circular dependency

**Demo folder:** `Lecture 06/code/CircularDependencyDemo/`

## Case 1 — Plain Java (`A` needs `B`, `B` needs `A`)

Wrong: create the other inside the constructor → `StackOverflowError`

Right: create both first, then wire with setters:

```java
A a = new A();
B b = new B();
a.setB(b);
b.setA(a);
```

**Code:** `simple/A.java`, `simple/B.java`, `Main.demoPlainJavaCircularFix()`

## Case 2 — Spring (`OrderService` ↔ `PaymentService`)

Quick fix — `@Lazy` on **one** side:

```java
@Lazy
@Autowired
private OrderService orderService;
```

Preferred long-term: redesign (remove one direction, or extract a 3rd class).

**Code:** `OrderService.java`, `PaymentService.java`

---

# §4 Field injection vs Constructor injection

## Field injection — create bean first, fill fields later

```text
1) Spring calls no-arg constructor  → bean object exists, fields are null
2) Spring later sets @Autowired fields
```

That is why a cycle can sometimes still start with field injection: both beans can be **created first**, then fields filled **later**.  
So field injection does **not** truly solve a cycle — it can **hide** it.

## Constructor injection — deps required during construction

```text
1) Spring must already have the dependency beans
2) Then it calls: new OrderService(paymentService)
```

If A needs B and B needs A through constructors → nothing can finish → `BeanCurrentlyInCreationException`.  
Failing fast is good: it forces a design fix (or intentional `@Lazy`).

## Why we still prefer constructor injection

| Point | Constructor | Field `@Autowired` |
|--------|-------------|---------------------|
| Required deps | Explicit & mandatory | Easy to leave null |
| Immutability | Can use `final` | Cannot |
| Unit tests | `new OrderService(mock)` | Needs Spring / reflection |
| Fail fast | Missing bean fails startup | Bugs can appear later |
| Cycles | Exposed early | Often hidden |

**Code refs:**

- Field style + comments: `CircularDependencyDemo/OrderService.java`
- Constructor style + `@Lazy`: `BeanInitializationDemo/OrderService.java`, `PaymentService.java`

---

# §5 Does constructor injection break SRP / SOLID?

**No.**

```java
public OrderService(PaymentService paymentService) {
    this.paymentService = paymentService; // only stores the reference
}
```

- The constructor does **not create** `PaymentService`.
- Spring (or your test) creates it and **passes it in**.
- Receiving collaborators is normal object construction, not a second business responsibility.

| Approach | SOLID impact |
|----------|----------------|
| `new PaymentService()` inside `OrderService` | Bad — class creates its own deps (hurts DIP / coupling) |
| Constructor receives `PaymentService` | Good — DI; class focuses on its job (supports SRP + DIP) |

SRP = reasons to change / business job — **not** “constructor must have zero parameters.”

**Code refs:** class-level comments in `BeanInitializationDemo/OrderService.java` and `CircularDependencyDemo/OrderService.java`
