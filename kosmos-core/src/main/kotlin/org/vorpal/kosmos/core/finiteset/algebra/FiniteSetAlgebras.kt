package org.vorpal.kosmos.core.finiteset.algebra

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.BooleanAlgebra
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.algebra.structures.instances.FiniteFieldAlgebras
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import java.math.BigInteger

/**
 * Main structures:
 * - [finiteSetIntersectionMonoid] over a universe
 * - [finiteSetUnionMonoid] (optionally over a universe)
 * - [finiteSetSymmetricDifferenceAbelianGroup] (optionally over a universe)
 * - [finiteSetBooleanRing] over a universe
 * - [finiteSetSemiring] over a universe
 * - [finiteSetBooleanAlgebra] over a universe
 * - [finiteSetF2VectorSpace] over a universe
 */
object FiniteSetAlgebras {
    private fun <A> intersection(universe: FiniteSet<A>): BinOp<FiniteSet<A>> =
        BinOp(Symbols.SET_INTERSECTION) { a, b ->
            check(a.isSubsetOf(universe)) { "Set $a is not a subset of $universe" }
            check(b.isSubsetOf(universe)) { "Set $b is not a subset of $universe" }
            a intersect b
        }

    private fun <A> union(universe: FiniteSet<A>): BinOp<FiniteSet<A>> =
        BinOp(Symbols.SET_UNION) { a, b ->
            check(a.isSubsetOf(universe)) { "Set $a is not a subset of $universe" }
            check(b.isSubsetOf(universe)) { "Set $b is not a subset of $universe" }
            a union b
        }

    private fun <A> symmetricDifference(universe: FiniteSet<A>): BinOp<FiniteSet<A>> =
        BinOp(Symbols.SET_SYMM_DIFF) { a, b ->
            check(a.isSubsetOf(universe)) { "Set $a is not a subset of $universe" }
            check(b.isSubsetOf(universe)) { "Set $b is not a subset of $universe" }
            a symmetricDifference b
        }

    /**
     * For a finite set `S` (the parameter [universe]), create the commutative monoid `(P(S), ∩, S)`.
     */
    fun <A> finiteSetIntersectionMonoid(universe: FiniteSet<A>): CommutativeMonoid<FiniteSet<A>> = CommutativeMonoid.of(
        identity = universe,
        op = intersection(universe)
    )

    /**
     * Create a commutative monoid on all finite sets of elements of type [A] under union with identity `Ø`.
     */
    fun <A> finiteSetUnionMonoid(): CommutativeMonoid<FiniteSet<A>> = CommutativeMonoid.of(
        identity = FiniteSet.of(),
        op = BinOp(Symbols.SET_UNION) { a, b -> a union b }
    )

    /**
     * For a finite set `S` (the parameter [universe]), create the commutative monoid `(P(S), ∪, Ø)`.
     */
    fun <A> finiteSetUnionMonoid(universe: FiniteSet<A>): CommutativeMonoid<FiniteSet<A>> = CommutativeMonoid.of(
        identity = FiniteSet.of(),
        op = union(universe)
    )

    /**
     * Create an abelian group on all finite sets of elements of type [A] under symmetric difference with identity `Ø`.
     *
     * Note that every element is its own inverse: characteristic 2.
     */
    fun <A> finiteSetSymmetricDifferenceAbelianGroup(): AbelianGroup<FiniteSet<A>> = AbelianGroup.of(
        identity = FiniteSet.of(),
        op = BinOp(Symbols.SET_SYMM_DIFF) { a, b -> a symmetricDifference b },
        inverse = Endo(Symbols.NOTHING, Identity())
    )

    /**
     * For a finite set `S` (the parameter [universe]), create the abelian group `(P(S), Δ, Ø)`.
     *
     * Note that every element is its own inverse: characteristic 2.
     */
    fun <A> finiteSetSymmetricDifferenceAbelianGroup(universe: FiniteSet<A>): AbelianGroup<FiniteSet<A>> = AbelianGroup.of(
        identity = FiniteSet.of(),
        op = symmetricDifference(universe),
        inverse = Endo(Symbols.NOTHING, Identity())
    )

    /**
     * Boolean ring over a fixed universe [universe] `S`, `(P(S), Δ, ∩)`, with:
     * - addition: `a Δ b` (symmetric difference), identity `Ø`
     * - multiplication: `a ∩ b` (intersection), identity `S`
     */
    fun <A> finiteSetBooleanRing(universe: FiniteSet<A>): CommutativeRing<FiniteSet<A>> = CommutativeRing.of(
        add = finiteSetSymmetricDifferenceAbelianGroup(universe),
        mul = finiteSetIntersectionMonoid(universe)
    )

    /**
     * Semiring over a fixed universe [universe] `S` with:
     * - addition monoid: `a ∪ b` (idempotent), identity `Ø`.
     * - multiplication monoid: `a ∩ b` (idempotent), identity `S`.
     *
     * Distribution works in both directions.
     */
    fun <A> finiteSetSemiring(universe: FiniteSet<A>): Semiring<FiniteSet<A>> = Semiring.of(
        add = finiteSetUnionMonoid(universe),
        mul = finiteSetIntersectionMonoid(universe)
    )

    /**
     * Boolean algebra on a fixed universe [universe] `S`:
     *  - bottom = `∅`, top = `S`
     *  - join = `∪`, meet = `∩`
     *  - not(a) = `aᶜ`
     */
    fun <A> finiteSetBooleanAlgebra(universe: FiniteSet<A>): BooleanAlgebra<FiniteSet<A>> = BooleanAlgebra.of(
        join = union(universe),
        meet = intersection(universe),
        bottom = FiniteSet.of(),
        top = universe,
        not = Endo(Symbols.SET_COMPLEMENT_POST) { set ->
            check(set isSubsetOf universe) { "Set $set is not a subset of $universe" }
            universe - set
        }
    )

    /**
     * The finite set abelian group under symmetric difference is a vector space over the field `𝔽_2`.
     *
     * The implicit basis of the vector space comprises the singleton subsets {x_i} of the universe, and thus
     * the dimension is the size of the universe.
     *
     * This is similar to the boolean algebra above, but is weaker in that it lacks:
     * - intersection
     * - complement
     * - top element
     */
    fun <A> finiteSetF2VectorSpace(universe: FiniteSet<A>): FiniteVectorSpace<BigInteger, FiniteSet<A>> =
        FiniteVectorSpace.of(
            scalars = FiniteFieldAlgebras.F2,
            add = finiteSetSymmetricDifferenceAbelianGroup(universe),
            dimension = universe.size,
            leftAction = LeftAction { s, set ->
                check(set.isSubsetOf(universe)) { "Set $set is not a subset of $universe" }
                when (s) {
                    BigInteger.ZERO -> FiniteSet.of()
                    BigInteger.ONE -> set
                    else -> throw IllegalArgumentException("s must be 0 or 1, got $s")
                }
            }
        )
}
