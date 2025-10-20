package org.vorpal.kosmos.combinatorics

import org.vorpal.kosmos.categories.Bijection
import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.FiniteSet.Companion.sorted
import org.vorpal.kosmos.core.gcd
import org.vorpal.kosmos.core.lcm

/**
 * A permutation is a bijection from a finite set to itself.
 * It therefore extends both Bijection<A, A> and Isomorphism<A, A>.
 */
data class Permutation<A>(
    override val domain: FiniteSet<A>,
    override val forward: Morphism<A, A>,
    override val backward: Morphism<A, A>
) : Bijection<A, A> {

    override val codomain = domain

    companion object {
        /** Construct from a total bijective mapping. */
        fun <A> of(domain: FiniteSet<A>, mapping: Map<A, A>): Permutation<A> {
            require(mapping.keys == domain.toSet()) { "Mapping must be total over domain." }
            require(mapping.values.toSet() == domain.toSet()) { "Mapping must be bijective." }
            val inverse = mapping.entries.associate { (k, v) -> v to k }
            return Permutation(
                domain,
                { a -> mapping[a] ?: error("Element $a not in domain.") },
                { a -> inverse[a] ?: error("Element $a not in domain.") }
            )
        }

        /** Identity permutation on a finite set. */
        fun <A> identity(domain: FiniteSet<A>): Permutation<A> =
            Permutation(domain, { it }, { it })
    }

    val isEmpty: Boolean
        get() = domain.isEmpty
    val isNotEmpty: Boolean
        get() = !isEmpty

    /** Apply the permutation to an element. */
    override fun apply(a: A): A =
        forward.apply(a)

    /** Provide access via [] lookup. */
    operator fun get(a: A): A =
        forward.apply(a)

    /** Compose permutations (function composition). */
    infix fun then(other: Permutation<A>): Permutation<A> {
        require(domain == other.domain) { "Permutation domains differ." }
        val composed = domain.associateWith { a -> this.apply(other.apply(a)) }
        return of(domain, composed)
    }

    /** Invert the permutation. */
    override fun inverse(): Permutation<A> = Permutation(domain, backward, forward)

    /** Compute the disjoint cycles of the permutation. */
    fun cycles(): List<List<A>> {
        tailrec fun aux(
            remaining: Set<A> = domain.toSet(),
            acc: List<List<A>> = emptyList()
        ): List<List<A>> =
            if (remaining.isEmpty()) acc
            else {
                val start = remaining.first()
                val cycle = generateCycle(start)
                val nextRemaining = remaining - cycle.toSet()
                val nextAcc = if (cycle.size > 1) acc + listOf(cycle) else acc
                aux(nextRemaining, nextAcc)
            }

        return aux()
    }

    /** Order of the permutation (least positive n with p^n = id). */
    fun order(): Int = cycles().fold(1) { acc, cycle -> lcm(acc, cycle.size) }

    /** Power of the permutation. */
    fun exp(power: Int): Permutation<A> {
        require(power >= 0) { "Power must be non-negative, was $power" }
        return (1..power).fold(identity(domain)) { acc, _ -> acc then this }
    }

    /** Internal: generate one cycle starting from [start]. */
    private fun generateCycle(start: A): List<A> {
        tailrec fun go(cur: A = start, acc: List<A> = emptyList()): List<A> =
            if (cur == start && acc.isNotEmpty()) acc
            else go(this.apply(cur), acc + cur)
        return go()
    }
}

/* -----------------------------------------------------------------------
 * FiniteSet extensions for creating common permutations.
 * --------------------------------------------------------------------- */

/** Identity permutation on this finite set. */
fun <A> FiniteSet<A>.identityPermutation(): Permutation<A> =
    Permutation.identity(this)

/** Construct a cyclic permutation with a given shift. */
fun <A> FiniteSet<A>.cyclicPermutation(shift: Int = 1): Permutation<A> {
    val elements = toList()
    val n = elements.size
    require(gcd(shift.mod(n), n) == 1) {
        "Shift $shift must be coprime to $n for a cyclic permutation."
    }
    val mapping = elements.mapIndexed { i, e ->
        e to elements[(i + shift).mod(n)]
    }.toMap()
    return Permutation.of(this, mapping)
}

/** Cyclic permutation on the sorted elements of this finite set. */
fun <A : Comparable<A>> FiniteSet<A>.cyclicPermutationSorted(shift: Int = 1): Permutation<A> =
    sorted(this).cyclicPermutation(shift)

/** Shift permutation by k positions (wrap-around). */
fun <A> FiniteSet<A>.shiftPermutation(shift: Int): Permutation<A> {
    val elems = toList()
    val mapping = elems.mapIndexed { i, e -> e to elems[(i + shift).mod(size)] }.toMap()
    return Permutation.of(this, mapping)
}

/** Shift permutation on a sorted version of this finite set. */
fun <A : Comparable<A>> FiniteSet<A>.shiftPermutationSorted(shift: Int): Permutation<A> =
    sorted(this).shiftPermutation(shift)
