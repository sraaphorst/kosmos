package org.vorpal.kosmos.core.finiteset.algebra

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.BooleanAlgebra
import org.vorpal.kosmos.algebra.structures.BooleanAlgebras
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Semiring
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.finiteset.FiniteSet
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

infix fun <A> FiniteSet<A>.meet(other: FiniteSet<A>): FiniteSet<A> = this.intersect(other)

/* ---------- Monoids on P(S) ---------- */

/**
 * For a finite set S, create the commutative monoid (P(S), ∪) where the identity is Ø.
 */
fun <A> finiteSetUnionMonoid(): CommutativeMonoid<FiniteSet<A>> = object : CommutativeMonoid<FiniteSet<A>> {
    override val identity: FiniteSet<A> = FiniteSet.of()
    override val op: BinOp<FiniteSet<A>> = BinOp(symbol = Symbols.SET_UNION) { a, b -> a + b }
}

/**
 * For a finite set S, create the monoid (P(S), ∩) where the identity is S.
 */
fun <A> finiteSetIntersectionMonoid(fullSet: FiniteSet<A>): CommutativeMonoid<FiniteSet<A>> = object : CommutativeMonoid<FiniteSet<A>> {
    override val identity: FiniteSet<A> = fullSet
    override val op: BinOp<FiniteSet<A>> = BinOp(symbol = Symbols.SET_INTERSECTION) { a, b ->
        check(a.isSubsetOf(fullSet)) { "Set $a is not a subset of $fullSet" }
        check(b.isSubsetOf(fullSet)) { "Set $b is not a subset of $fullSet" }
        a.intersect(b)
    }
}

/** (P(S), Δ, Ø) — every element is its own inverse; characteristic 2. */
fun <A> finiteSetSymmetricDifferenceAbelianGroup(): AbelianGroup<FiniteSet<A>> =
    object : AbelianGroup<FiniteSet<A>> {
        override val identity: FiniteSet<A> = FiniteSet.of()
        override val op: BinOp<FiniteSet<A>> =
            BinOp(symbol = Symbols.SET_SYMM_DIFF) { a, b -> a symmetricDifference b }
        override val inverse: (FiniteSet<A>) -> FiniteSet<A> = { it } // A = -A under Δ
    }

/* ---------- Boolean ring on P(S) with 1 = S ---------- */

/**
 * Boolean ring (P(S), +, ·) with:
 *  - addition  a + b = a Δ b (symmetric difference),
 *  - multiply  a · b = a ∩ b (intersection),
 *  - 0 = Ø, 1 = S.
 *
 * NOTE: requires a fixed universe S so that `1 = S`.
 */
fun <A> finiteSetBooleanRing(fullSet: FiniteSet<A>): CommutativeRing<FiniteSet<A>> =
    CommutativeRing.of(
        add = finiteSetSymmetricDifferenceAbelianGroup(),
        mul = finiteSetIntersectionMonoid(fullSet)
    )

/* ---------- Idempotent semiring (∪, ∩) on P(S) ---------- */

/**
 * Semiring with:
 * - addition: union monoid (idempotent)
 * - multiplication: intersection monoid (over universe, idempotent)
 *
 * Distribution works in both directions.
 */
fun <A> finiteSetUnionIntersectionSemiring(fullSet: FiniteSet<A>): Semiring<FiniteSet<A>> =
    Semiring.of(
        add = finiteSetUnionMonoid(),
        mul = finiteSetIntersectionMonoid(fullSet)
    )

/* ---------- Boolean algebra on P(S) ---------- */

/**
 * Boolean algebra on P(S):
 *  - bottom = ∅, top = S
 *  - join = ∪, meet = ∩
 *  - not(a) = aᶜ
 *
 * All operations are guarded to remain within the fixed universe S.
 */
fun <A> finiteSetBooleanAlgebra(fullSet: FiniteSet<A>): BooleanAlgebra<FiniteSet<A>> =
    BooleanAlgebras.of(
        join = BinOp(Symbols.SET_UNION) { a, b ->
            check(a.isSubsetOf(fullSet)) { "Set $a is not a subset of $fullSet" }
            check(b.isSubsetOf(fullSet)) { "Set $b is not a subset of $fullSet" }
            a + b
        },
        meet = BinOp(Symbols.SET_INTERSECTION) { a, b ->
            check(a.isSubsetOf(fullSet)) { "Set $a is not a subset of $fullSet" }
            check(b.isSubsetOf(fullSet)) { "Set $b is not a subset of $fullSet" }
            a.intersect(b)
        },
        bottom = FiniteSet.of(),
        top = fullSet,
        not = Endo(symbol = Symbols.SET_COMPLEMENT_POST) { a ->
            check(a.isSubsetOf(fullSet)) { "Set $a is not a subset of $fullSet" }
            fullSet - a
        }
    )
