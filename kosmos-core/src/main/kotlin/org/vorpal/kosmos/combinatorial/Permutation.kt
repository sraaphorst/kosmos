package org.vorpal.kosmos.combinatorial

import org.vorpal.kosmos.combinatorial.FiniteSet
import org.vorpal.kosmos.core.gcd
import org.vorpal.kosmos.core.lcm

data class Permutation<T>(
    val domain: FiniteSet<T>,
    val mapping: Map<T, T>
) {
    init {
        require(mapping.keys == domain) { "Mapping must be total. "}
        require(mapping.values.toSet() == domain) { "Mapping must be bijective." }
    }

    operator fun get(element: T): T =
        mapping[element] ?: error("Element $element not in domain.")

    fun inverse(): Permutation<T> =
        Permutation(domain, mapping.entries.associate { (k, v) -> v to k } )

    fun cycles(): List<List<T>> {
        tailrec fun aux(
            remaining: Set<T> = domain.toSet(),
            accumulator: List<List<T>> = emptyList(),
        ): List<List<T>> = when {
            remaining.isEmpty() -> accumulator
            else -> {
                val start = remaining.first()
                val cycle = generateCycle(start)
                val nextRemaining = remaining - cycle.toSet()
                val nextAccumulator = if (cycle.size > 1) accumulator + listOf(cycle) else accumulator
                aux(nextRemaining, nextAccumulator)
            }
        }

        return aux()
    }

    /**
     * The order of the permutation, i.e. the smallest positive integer i  such that for this permutation p,
     * p^i is the identity permutation.
     */
    fun order(): Int =
        cycles().fold(1) { acc, cycle -> lcm(acc, cycle.size) }

    /**
     * Compose permutations. This will be useful when we create permutation groups.
     */
    operator fun times(other: Permutation<T>): Permutation<T> {
        require(domain == other.domain) { "Permutation domains are not composable."}
        return Permutation(domain, domain.associateWith { this[other[it]] })
    }

    /**
     * Calculate this permutation to the specified power.
     * Note that for any power > 0, the smallest value of power such that this permutation will be the identity
     * on the specified set is order.
     */
    fun exp(power: Int): Permutation<T> {
        require(power >= 0) { "Power must be non-negative, but was $power" }
        return (1..power).fold(domain.identityPermutation()) { acc, _ -> acc * this }
    }

    private fun generateCycle(start: T): List<T> {
        tailrec fun aux(current: T = start, acc: List<T> = emptyList()): List<T> =
            if (current == start && acc.isNotEmpty()) acc
            else aux(this[current], acc + current)

        return aux()
    }
}

private fun <T> List<T>.shiftPermutation(fs: FiniteSet<T>, shift: Int): Permutation<T> {
    val mapping = mapIndexed { i, elem ->
        elem to this[(i + shift).mod(size)]
    }.toMap()
    return Permutation(fs, mapping)
}

fun <T> FiniteSet<T>.identityPermutation(): Permutation<T> =
    Permutation(this, this.associateWith { it })

/**
 * Make a cyclic permutation out of the elements in the finite set in the order they appear.
 * The parameter [shift] indicates how far each element shifts and should be relatively prime
 * with the size of the finite set: otherwise, we would end up with disjoint cycles.
 */
fun <T> FiniteSet<T>.cyclicPermutation(shift: Int = 1): Permutation<T> {
    val elements = this.toList()
    val n = elements.size
    require(gcd(shift.mod(n), n) == 1) {
        "Shift $shift must be coprime to $n for a cyclic permutation."
    }
    return elements.shiftPermutation(this, shift)
}

/**
 * Make a cyclic permutation out of the elements in the finite set in their sorted order.
 * The parameter [shift] indicates how far each element shifts and should be relatively prime
 * with the size of the finite set: otherwise, we would end up with disjoint cycles.
 */
fun <T : Comparable<T>> FiniteSet<T>.cyclicPermutationSorted(shift: Int = 1): Permutation<T> {
    val sorted = this.toList().sorted()
    val n = sorted.size
    require(gcd(shift.mod(n), n) == 1) {
        "Shift $shift must be coprime to $n for a cyclic permutation."
    }
    return sorted.shiftPermutation(this, shift)
}

fun <T> FiniteSet<T>.shiftPermutation(shift: Int): Permutation<T> =
    toList().shiftPermutation(this, shift)

fun <T : Comparable<T>> FiniteSet<T>.shiftPermutationSorted(shift: Int): Permutation<T> =
    sorted().shiftPermutation(this,shift)
