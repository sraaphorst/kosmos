# Kosmos: Introduction

Kosmos is a Kotlin library of mathematical structures and the laws they obey. The idea behind it:

1. **Define a structure as a value.** A `Monoid<A>` is an interface with an operation and an identity; the monoid of
   integers under addition is a value implementing it. Bigger structures are built from smaller ones exactly as in
   mathematics: a `Ring<A>` has an additive `AbelianGroup<A>` and a multiplicative `Monoid<A>`, and its multiplication
   distributes over its addition.
2. **State its laws as executable checks.** Every structure has a law suite in `kosmos-laws`: `MonoidLaws` checks
   associativity and identity, `RingLaws` checks everything a ring must satisfy, and so on. A law is a property test,
   run against generated inputs, with the structure's own notion of equality.
3. **Run the laws as a graph, and read the report.** A suite is composed from the suites of its parts, so the laws of a
   field form a graph: the field's additive group, its multiplicative monoid, distributivity, inverses. Running it
   produces a report that mirrors that graph: every failure comes with its full path, a counterexample and the seed to
   replay it. A check reached through several parts of the graph runs once.

A passing report is evidence, not proof: it says the laws held on the inputs that were generated. Kosmos algorithms
take structures directly (a `Field<A>`, not a certified wrapper); the law graph is how Kosmos, and anyone adding a
structure, finds out whether an implementation deserves the name.

## A small example

```kotlin
// A structure is a value. Here, q is of type Field<Rational>.
val q = RationalAlgebras.RationalField
q.mul(Rational.of(2, 3), Rational.of(3, 4))   // 1/2

// Its laws are a graph, registered as nested Kotest tests.
class RationalFieldSpec : FunSpec({
    lawTests(FieldLaws(q, ArbRational.small, RationalAlgebras.eqRational, RationalAlgebras.printableRationalPretty))
})
```

If, say, multiplication were not associative, the report would point at
`Field > DivisionRing > Ring > Rng > Hemiring > Semigroup (*) > associativity (*)` and show the three rationals that
break it.

## The modules

* **`kosmos-core`** holds the structures and everything built on them:
  * the algebraic hierarchy, from magmas and semigroups through groups, rings and fields to modules, vector spaces,
    algebras and star algebras (see the
    [list of structures](../kosmos-core/src/main/kotlin/org/vorpal/kosmos/algebra/structures/README.md) and the
    [hierarchy diagram](kosmos_algebra_hierarchy.svg));
  * concrete instances: integers, exact rationals, ℤ/nℤ, Gaussian and Eisenstein numbers, polynomials, matrices;
  * the hypercomplex numbers, generic over their base ring through Cayley–Dickson doubling
    ([guide](../kosmos-core/src/main/kotlin/org/vorpal/kosmos/hypercomplex/README.md));
  * linear algebra ([guide](kosmos-linear.md)), combinatorics, graphs, analysis and functional programming tools.

* **`kosmos-laws`** holds the laws and the machinery that runs them ([guide](03-kosmos-laws.md)):
  * individual laws such as associativity, commutativity, distributivity, invertibility or the Moufang identities,
    each stated about specific operations;
  * law suites, one per structure, each composed from the suites of the structure's parts;
  * the law graph, the runner and the report (text, and JSON via `kosmos-testkit`).
  * It depends on Kotest's property testing at runtime, which is why it is a separate module.

* **`kosmos-testkit`** holds the generators (Kotest `Arb`s) for Kosmos types, the integration that turns a law graph
  into nested Kotest tests, and the specs that test Kosmos itself.

* **`kosmos-noise`** is an experimental module for noise and random fields. (outdated)

## Where to go next

* [Law testing](03-kosmos-laws.md): writing laws and specs, reading reports, replaying failures.
* [Hypercomplex numbers](../kosmos-core/src/main/kotlin/org/vorpal/kosmos/hypercomplex/README.md): from ℂ to the
  octonions, sextonions and their split and complexified relatives.
* [Here Be Dragons](HereBeDragons.md): for the stranger creatures.
