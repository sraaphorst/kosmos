# Lattice Framework

The **Kosmos Lattice Framework** generalizes the idea of *self-indexing sequences* —
structures where each element indexes into the same sequence again. It lifts any numeric sequence or recurrence in Kosmos
into a *self-indexing structure* and provides composable index functions for
them.  It unifies previously ad-hoc constructions such as the “Prime lattice”
and “Fibonacci lattice” under a single generic API.

---

## Concept

Given a cached or closed-form sequence \( a_n \), we can define its **lattice**:

\[
L^{(d)}(n) = a_{a_{a_{\dots_{a_n}}}}
\]

where the index operator is applied `d` times.  
This creates hierarchical, recursively indexed structures such as:

- **Prime lattice:** \( p_{p_n} \), \( p_{p_{p_n}} \), …
- **Fibonacci lattice:** \( F_{F_n} \), \( F_{F_{F_n}} \), …
- **Catalan lattice**, **Bell lattice**, etc.

Any cached recurrence or closed-form sequence can be *lifted* into a lattice,
and each lattice automatically yields a composable *index function* satisfying
functor and monad laws.


---

## Components

| File | Purpose |
|------|----------|
| **`RecurrenceLattice.kt`** | Lifts any `Recurrence<T>` into an `Indexable` lattice. |
| **`LatticeIndexFunction.kt`** | Reader-style morphism \( n↦a_n \) with `andThen`, `flatMap`, `repeat`. |
| **`LatticeIndexFunctionExt.kt`** | Convenience extensions: `.asIndexFunction()`, `.asIndexIdentity()`, `.asIndexPure()`. |
| **`instances/`** | Ready-made lattices (e.g., `FibonacciLattice`, `PrimeLattice`). |


---

## 🧩 Conceptual Architecture

```text
+-------------------------+
| Recurrence<T>           | → generates a numeric sequence aₙ
+-------------------------+
           ↓ lifted via RecurrenceLattice.of()
+-------------------------+
| Indexable<L, BigInt>    | → provides index(), iterate(), row(), column()
+-------------------------+
           ↓ functional view
+-------------------------+
| LatticeIndexFunction<L> | → composable morphisms (andThen, flatMap, repeat)
+-------------------------+
```

## Example

```kotlin
import org.vorpal.kosmos.combinatorics.sequences.Fibonacci
import org.vorpal.kosmos.frameworks.lattice.*

val FibonacciLattice = RecurrenceLattice.of("Fibonacci", Fibonacci) { it }
val F = FibonacciLattice.asIndexFunction()     // n ↦ Fₙ
val F3 = F.repeat(3)                           // n ↦ F_{F_{F_n}}

println(F3.values().take(6).toList())