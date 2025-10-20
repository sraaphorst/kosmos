package org.vorpal.kosmos.combinatorics

import org.vorpal.kosmos.categories.Bijection
import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.gcd
import org.vorpal.kosmos.core.lcm

/**
 * A permutation is a bijection from a finite set to itself.
 * It behaves as an isomorphism in the category of finite sets.
 *
 * The internal representation is the forward mapping (domain → codomain),
 * from which the inverse mapping is derived lazily.
 */
data class Permutation<A>(
    override val domain: FiniteSet<A>,
    private val mapping: Map<A, A>
) : Bijection<A, A> {

    override val codomain: FiniteSet<A> = domain

    init {
        require(mapping.keys == domain.toSet()) { "Mapping must be total over domain." }
        require(mapping.values.toSet() == domain.toSet()) { "Mapping must be bijective." }
    }

    val isEmpty: Boolean
        get() = domain.isEmpty
    val isNotEmpty: Boolean
        get() = !isEmpty

    /** Inverse mapping, computed lazily. */
    private val inverseMapping: Map<A, A> by lazy {
        mapping.entries.associate { (k, v) -> v to k }
    }

    /** The forward morphism f : A → A. */
    override val forward: Morphism<A, A> = Morphism { a ->
        mapping[a] ?: error("Element $a not in domain.")
    }

    /** The inverse morphism f⁻¹ : A → A. */
    override val backward: Morphism<A, A> = Morphism { a ->
        inverseMapping[a] ?: error("Element $a not in domain.")
    }

    /** Apply permutation to an element. */
    override fun apply(a: A): A = forward.apply(a)

    /** Apply inverse permutation to an element. */
    fun applyInverse(a: A): A = backward.apply(a)

    /** Provide access via [] lookup. */
    operator fun get(a: A): A =
        forward.apply(a)

    /** Composition (function composition). */
    infix fun then(other: Permutation<A>): Permutation<A> {
        require(domain == other.domain) { "Permutation domains differ." }
        val composed = domain.associateWith { a -> this.apply(other.apply(a)) }
        return Permutation(domain, composed)
    }

    /** Inverse permutation. */
    override fun inverse(): Permutation<A> = Permutation(domain, inverseMapping)

    /** True if this permutation acts as the identity. */
    val isIdentity: Boolean
        get() = domain.all { apply(it) == it }

    /** Compute disjoint cycles of the permutation. */
    fun cycles(): List<List<A>> {
        tailrec fun aux(remaining: Set<A>, acc: List<List<A>>): List<List<A>> =
            if (remaining.isEmpty()) acc
            else {
                val start = remaining.first()
                val cycle = generateCycle(start)
                val nextRemaining = remaining - cycle.toSet()
                val nextAcc = if (cycle.size > 1) acc + listOf(cycle) else acc
                aux(nextRemaining, nextAcc)
            }
        return aux(domain.toSet(), emptyList())
    }

    private fun generateCycle(start: A): List<A> {
        tailrec fun go(cur: A, acc: List<A>): List<A> =
            if (cur == start && acc.isNotEmpty()) acc
            else go(apply(cur), acc + cur)
        return go(start, emptyList())
    }

    /**
     * Sign (parity) of the permutation: 1 for even, -1 for odd.
     * Computed as (-1)^(n - number of cycles)
     */
    fun sign(): Int {
        val cycleCount = cycles().size
        val fixedPoints = domain.count { apply(it) == it }
        val totalCycles = cycleCount + fixedPoints
        return if ((domain.size - totalCycles) % 2 == 0) 1 else -1
    }

    /** Order of the permutation (least n > 0 such that pⁿ = id). */
    fun order(): Int = cycles().fold(1) { acc, c -> lcm(acc, c.size) }

    /** Exponentiation (repeated composition). */
    fun exp(power: Int): Permutation<A> {
        require(power >= 0) { "Power must be non-negative." }
        return (1..power).fold(identity(domain)) { acc, _ -> acc then this }
    }

    override fun toString(): String = mapping.entries.joinToString(
        prefix = "Permutation(",
        postfix = ")"
    ) { "${it.key}↦${it.value}" }

    companion object {
        /** Construct from a total bijective map. */
        fun <A> of(domain: FiniteSet<A>, mapping: Map<A, A>): Permutation<A> =
            Permutation(domain, mapping)

        /** Identity permutation on a finite set. */
        fun <A> identity(domain: FiniteSet<A>): Permutation<A> =
            Permutation(domain, domain.associateWith { it })
    }
}

/* -----------------------------------------------------------------------
 * FiniteSet extensions for creating common permutations.
 * --------------------------------------------------------------------- */

/** Identity permutation on this finite set. */
fun <A> FiniteSet<A>.identityPermutation(): Permutation<A> =
    Permutation.identity(this)

/**
 * Cyclic permutation with given shift.
 * Requires one of the following conditions:
 * 1. shift = 0, in which case, the identity permutation is returned;
 * 2. gcd(shift, n) = 1, which is necessary for the creation of a cyclic permutation.
 * */
fun <A> FiniteSet<A>.cyclicPermutation(shift: Int = 1): Permutation<A> {
    if (shift == 0) return Permutation.identity(this)

    val elements = toList()
    val n = elements.size
    require(gcd(shift.mod(n), n) == 1) { "Shift $shift must be coprime to $n." }
    val mapping = elements.mapIndexed { i, e ->
        e to elements[(i + shift).mod(n)]
    }.toMap()
    return Permutation.of(this, mapping)
}

/** Shift permutation (wrap-around). */
fun <A> FiniteSet<A>.shiftPermutation(shift: Int): Permutation<A> {
    val elems = toList()
    val mapping = elems.mapIndexed { i, e -> e to elems[(i + shift).mod(size)] }.toMap()
    return Permutation.of(this, mapping)
}
