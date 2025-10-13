# Kosmos Combinatorics

This package implements **core combinatorial structures** built atop the Kosmos frameworks.  
It provides a comprehensive foundation for enumerative combinatorics, number sequences,
and generating functions, forming the mathematical core for later graph and hypergraph systems.

## Structure

### `combinatorics.sequence`
Implements univariate integer and combinatorial sequences, including:

| Category | Examples |
|-----------|-----------|
| **Classical** | Fibonacci, Lucas, Pell, Jacobsthal |
| **Combinatorial** | Catalan, Bell, Motzkin, Schröder |
| **Graph-related** | CayleyTrees, ConnectedGraphs, RootedTrees, LabeledDAGs |
| **Other** | Derangement, Partition, Euler, Bernoulli |

All are defined as cached recursive or closed-form sequences using the framework in `frameworks.sequence`.

---

### `combinatorics.array`
Implements **bivariate combinatorial arrays** (`A(n, k)`) such as:

| Array | Description |
|--------|-------------|
| **Pascal** | Binomial coefficients |
| **Stirling (1st, 2nd)** | Permutation and partition counts |
| **Lah, Narayana, Delannoy, Schroder** | Ordered partitions and lattice path counts |

These arrays use `frameworks.array` to enable recursive and closed-form access.

---

### `combinatorics.meta`
Implements **analytic or derived sequences** that generalize other combinatorial families:

| Sequence | Description |
|-----------|-------------|
| **Harmonic** | Hₙ = Σₖ₌₁ⁿ 1/k |
| **Bernoulli (±)** | Defined by Σₖ binom(n+1,k)Bₖ = 0 |
| **Euler** | Appearing in sec(x) expansions |
| **Derangement** | Permutations without fixed points |

---

## Purpose

This module provides the **enumerative and algebraic backbone** for Kosmos:
it unifies combinatorial reasoning under a single algebraic and functional framework,
supporting exact arithmetic, lazy evaluation, and high extensibility.

---

## Example

```kotlin
println(Catalan.take(10).toList())
// [1, 1, 2, 5, 14, 42, 132, 429, 1430, 4862]
```


Future expansions:
-	Polynomial extensions (Bernoulli, Euler polynomials)
-	Generating functions and species operations
-	Integration with `org.vorpal.kosmos.graphs` and `org.vorpal.kosmos.hypergraphs`