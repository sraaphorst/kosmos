# Kosmos

[![Tests](https://github.com/sraaphorst/kosmos/actions/workflows/tests.yml/badge.svg?branch=main)](https://github.com/sraaphorst/kosmos/actions/workflows/tests.yml?query=branch%3Amain)

*A universe of algebraic structures for Kotlin.*

Kosmos models mathematics the way mathematicians build it: a field is an additive group and a multiplicative monoid
tied together by distributivity, an algebra is a ring that is also a module, and so on. Each structure is a Kotlin
interface, each concrete example (ℤ, ℚ, the octonions, ℤ/nℤ, …) is a value implementing it, and each structure comes
with its laws, which are executable property tests.

Kosmos is in early development. The APIs change freely, and there is no published release yet.

## What is in it

| Area | Package (`org.vorpal.kosmos.…`) | Highlights |
|---|---|---|
| Algebraic structures | `algebra.structures` | Magmas through fields, rings with involution, modules, vector spaces, algebras, star algebras, heaps, lattices, meadows, wheels ([list](kosmos-core/src/main/kotlin/org/vorpal/kosmos/algebra/structures/README.md)) |
| Polynomials | `algebra.poly` | Polynomials over any (semi)ring, polynomial towers such as ℚ[x][y] |
| Hypercomplex numbers | `hypercomplex` | Complex numbers, quaternions, octonions, sextonions, their split forms and complexifications, dual numbers, all generic over the base ring via Cayley–Dickson doubling ([guide](kosmos-core/src/main/kotlin/org/vorpal/kosmos/hypercomplex/README.md)) |
| Number systems | `core.rational`, `numberfields`, `numbertheory` | Exact rationals, Gaussian and Eisenstein integers and rationals, primes |
| Linear algebra | `linear` | Fixed-size and dense vectors and matrices with their module and algebra structures, row reduction, permutation matrices ([guide](docs/kosmos-linear.md)) |
| Combinatorics | `combinatorics` | Permutations, integer sequences, partitions, block designs and Steiner systems, the Fano plane ([guide](kosmos-core/src/main/kotlin/org/vorpal/kosmos/combinatorics/README.md)) |
| Also | `graphs`, `analysis`, `categories`, `functional` | Graphs, scalar and vector fields, bijections and isomorphisms, functional data structures and optics |

## A taste

```kotlin
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras.RationalField
import org.vorpal.kosmos.core.rational.Rational

fun main() {
    val pr = RationalAlgebras.printableRationalPretty
    
    // You can preface add and reciprocal with RationalField and not use the embedded run if you prefer.
    RationalField.run {
        val rHalf = Rational.of(1, 2)
        val rOne = add(half, half)
        println(pr(rOne))              // prints 1
        val threeOverTwo = reciprocal(Rational.of(2, 3))
        println(pr(threeOverTwo))      // prints 3/2
    }
}
```

Structures are tested against their laws. A spec registers the law graph of a structure, and every law becomes its own
test, nested the way the structure is built:

```kotlin
class RationalFieldSpec : FunSpec({
    lawTests(
        FieldLaws(RationalField, ArbRational.small, RationalAlgebras.eqRational, RationalAlgebras.printableRationalPretty)
    )
})
```

A failing law is reported with its full path (for example
`Field > DivisionRing > Ring > Rng > Hemiring > Semigroup (*) > associativity (*)`), the counterexample, and the seed
and call to replay it. See [the law-testing guide](docs/03-kosmos-laws.md).

## Modules

| Module | Purpose |
|---|---|
| [`kosmos-core`](kosmos-core) | The structures, their instances, and the algorithms on them |
| [`kosmos-laws`](kosmos-laws) | The laws, the law suites of every structure, and the runner that executes them as a graph |
| [`kosmos-testkit`](kosmos-testkit) | Generators (Kotest `Arb`s) for Kosmos types, the Kotest integration, and the test specs |
| `kosmos-noise` | Experimental noise and random-field generation (currently outdated)|

## Building

Kosmos needs JDK 21. Gradle and Kotlin are fetched by the wrapper.

```
./gradlew build
./gradlew :kosmos-laws:test :kosmos-testkit:test
```

## Documentation

- [Introduction](docs/00-introduction.md): the ideas behind Kosmos and how the pieces fit together.
- [Law testing](docs/03-kosmos-laws.md): writing laws and specs, reading reports, replaying failures.
- [The algebraic hierarchy](docs/kosmos_algebra_hierarchy.svg) as a diagram.
- [Kosmos Discussions](https://github.com/sraaphorst/kosmos/discussions) for questions and ideas.
- [Here Be Dragons](docs/HereBeDragons.md): lore, bloopers and stranger creatures.

## License

[BSD 3-Clause](LICENSE.md).
