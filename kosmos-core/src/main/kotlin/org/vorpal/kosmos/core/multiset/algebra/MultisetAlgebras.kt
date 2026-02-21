package org.vorpal.kosmos.core.multiset.algebra

import org.vorpal.kosmos.algebra.morphisms.MonoidHomomorphism
import org.vorpal.kosmos.algebra.structures.BoundedLattice
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.pow
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.multiset.Multiset
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.UnaryOp

object MultisetAlgebras {
    /**
     * Create a free [CommutativeMonoid] on a type [T].
     */
    fun <T : Any> freeCommutativeMonoid(): CommutativeMonoid<Multiset<T>> = CommutativeMonoid.of(
        identity = Multiset.empty(),
        op = BinOp(Symbols.PLUS) { a, b -> a + b }
    )

    /**
     * The free commutative monoid on `T` has a universal property: any function `f: T → M` into a commutative monoid
     * extends uniquely to a monoid homomorphism `Multiset<T> → M`.
     *
     * That's what lift constructs.
     *
     * Examples:
     *
     * 1. Think of a (very reduced) game of Scrabble. Let `T = {a, b, c}` be the characters.
     * Then `M = (ℕ, +, 0)` represents possible collections of tiles, and `f` maps each character to its score.
     * Arbitrarily, let's say the letters are worth:
     * ```kotlin
     * f(a) = 3
     * f(b) = 5
     * f(c) = 2
     * ```
     * Now take the multiset `{a: 2, b: 1, c: 3}`, meaning two copies of `a`, one of `b`, three of `c`.
     * We want to find the total score of the tiles in your hand, i.e. the multiset:
     * ```kotlin
     * f(a) * 2 + f(b) * 1 + f(c) * 3
     * = 3 * 2 + 5 * 1 + 2 * 3
     * = 6 + 5 + 6
     * = 17
     * ```
     *
     * 2. The Fundamental Theorem of Arithmetic:
     *
     * Every (positive) natural number can be written uniquely as a product of primes up to ordering.
     *
     * The multiset, then, can represent any positive natural number `n` using its primes as elements and the
     * multiplicities as the powers.
     *
     * Let `M = (ℕ₊, ×, 1)`, and `f` is essentially a list of primes, i.e. it maps i to the ith prime::
     * ```kotlin
     * f(0) = 2
     * f(1) = 3
     * f(2) = 5
     * ```
     * Say we have a natural number represented by the index of its prime powers: `{0: 4, 1: 0, 2: 2}`
     * ```kotlin
     * φ({0: 4, 1: 0, 2: 2}) = f(0)^4 · f(1)^0 · f(2)^2
     *                        = 2⁴ · 3⁰ · 5²
     *                        = 16 · 1 · 25
     *                        = 400
     * ```
     * So the multiset represents the number 400. The lifted homomorphism `φ` is an isomorphism
     * between `(Multiset<ℕ>, +, ∅)` and `(ℕ₊, ×, 1)`: the Fundamental Theorem of Arithmetic
     * is precisely the statement that this lift is bijective.
     */
    fun <T : Any, M : Any> lift(
        target: CommutativeMonoid<M>,
        f: (T) -> M
    ): MonoidHomomorphism<Multiset<T>, M> = MonoidHomomorphism.of(
        domain = freeCommutativeMonoid(),
        codomain = target,
        map = UnaryOp { ms ->
            ms.support.fold(target.identity) { acc, t ->
                val power = target.pow(f(t), ms.multiplicity(t))
                target(acc, power)
            }
        }
    )

    /**
     * A monoid representing the union over multisets.
     * - The identity is the empty multiset.
     * - The monoid operation is set union, which combines multisets by taking the maximum
     *   multiplicity of each element.
     */
    fun <T : Any> multisetMaxMonoid(): CommutativeMonoid<Multiset<T>> =
        CommutativeMonoid.of(
            identity = Multiset.empty(),
            op = BinOp(Symbols.SET_UNION) { a, b -> a max b }
        )

    /**
     * A bounded distributive lattice over multisets under the pointwise min-max order.
     * - Join: pointwise maximum of multiplicities (multiset union).
     * - Meet: pointwise minimum of multiplicities (multiset intersection).
     * - Bottom: the empty multiset.
     * - Top: the given universe multiset.
     *
     * The lattice laws are violated if a multiset is introduced that contains
     * elements outside the universe or has multiplicities exceeding those in the universe.
     */
    fun <T : Any> multisetMinMaxLattice(
        universe: Multiset<T>
    ): BoundedLattice<Multiset<T>> = BoundedLattice.of(
        join = BinOp(Symbols.SET_UNION) { a, b -> a max b },
        meet = BinOp(Symbols.SET_INTERSECTION, ) { a, b -> a min b },
        bottom = Multiset.empty(),
        top = universe
    )
}
