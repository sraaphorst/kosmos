# Kosmos Frameworks

This package provides the **foundational abstractions** for defining recursive and analytical mathematical structures.  
It underlies all combinatorial, graph, and algebraic systems in Kosmos.

## Overview

### `sequence`
Defines a uniform interface for univariate recurrences (`aₙ`) and closed-form expressions.

- **`Recurrence<T>`** — defines lazy, memoized recursive sequences.
- **`CachedRecurrenceImplementation<T>`** — base class for caching recursive sequences.
- **`CachedLinearRecurrenceImplementation<T, S>`** — general linear recurrence over a module or numeric type.
- **`ClosedForm<T>` / `CachedClosedFormImplementation<T>`** — supports analytical formulas with caching.

Together, these allow construction of arbitrary recursive or analytical sequences over types such as
`BigInteger`, `Rational`, or future algebraic structures (`Polynomial<T>`, `Field<T>`).

### `array`
Defines the corresponding bivariate framework for combinatorial arrays (`A(n, k)`).

- **`BivariateRecurrence<T>` / `CachedBivariateArray<T>`**
- **Support for Pascal-, Stirling-, Lah-, Narayana-type triangles**

Both frameworks emphasize:
- **Type safety** (strongly typed over numeric domains)
- **Lazy evaluation** (via Kotlin `Sequence`)
- **Thread-safe caching** (using `ConcurrentHashMap`)

## Example

```kotlin
object Fibonacci : CachedRecurrence<BigInteger> by FibonacciRecurrence

private object FibonacciRecurrence :
    CachedLinearRecurrenceImplementation<BigInteger, Int>(
        initialValues = listOf(BigInteger.ZERO, BigInteger.ONE),
        selectors = listOf(-1, -2),
        coefficients = listOf(1, 1),
        zero = BigInteger.ZERO,
        multiply = Action { s, t -> s.toBigInteger() * t },
        add = BinOp(BigInteger::add)
    )
```