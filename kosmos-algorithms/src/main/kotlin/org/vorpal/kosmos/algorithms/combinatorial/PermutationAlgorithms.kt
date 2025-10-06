package org.vorpal.kosmos.algorithms.combinatorial

import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.combinatorial.FiniteSet
import org.vorpal.kosmos.combinatorial.Permutation
import org.vorpal.kosmos.core.bigFactorial
import java.math.BigInteger
import java.util.Random

/**
 * ============================
 *  Permutation Algorithms
 * ============================
 *
 * Utilities for generating, ranking, unranking, and randomizing permutations
 * on finite ordered sets.  All algorithms assume the domain is a
 * [FiniteSet.Ordered] to ensure a stable lexicographic order.
 */

/**
 * Algorithms frequently assemble a mapping, so we use a local helper to create a Permutation
 * by wrapping the map in morphisms:
 */
private fun <A> permutationFromMap(
    base: FiniteSet<A>,
    mapping: Map<A, A>
): Permutation<A> {
    require(mapping.keys == base.toSet()) { "Mapping must be total over domain." }
    require(mapping.values.toSet() == base.toSet()) { "Mapping must be bijective over domain." }
    val inv = mapping.entries.associate { (a, b) -> b to a }
    val f = Morphism<A, A> { mapping.getValue(it) }
    val g = Morphism<A, A> { inv.getValue(it) }
    return Permutation(base, f, g)
}

/**
 * Generate all permutations of a finite ordered set lazily as a [Sequence].
 * Lexicographic order is respected.
 */
fun <A> permutations(base: FiniteSet.Ordered<A>): Sequence<Permutation<A>> = sequence {
    val n = base.size
    val idx = IntArray(n) { it }

    fun build(): Permutation<A> {
        val mapping = base.order.indices.associate { k -> base[k] to base[idx[k]] }
        return permutationFromMap(base, mapping)
    }

    yield(build()) // start with identity mapping

    fun next(): Boolean {
        var i = n - 2
        while (i >= 0 && idx[i] >= idx[i + 1]) i--
        if (i < 0) return false

        var j = n - 1
        while (idx[j] <= idx[i]) j--

        idx[i] = idx[j].also { idx[j] = idx[i] }

        var l = i + 1
        var r = n - 1
        while (l < r) {
            idx[l] = idx[r].also { idx[r] = idx[l] }
            l++; r--
        }
        return true
    }

    while (next()) yield(build())
}


/**
 * Convert a permutation to its Lehmer code (factoradic representation).
 * Each position i gives the number of smaller elements to its right.
 */
fun <A> lehmerCode(p: Permutation<A>, base: FiniteSet.Ordered<A>): IntArray {
    require(p.domain.toSet() == base.toSet()) { "Permutation/domain mismatch with the given base." }

    val n = base.size
    val indexOf = base.order.withIndex().associate { it.value to it.index }
    // pos[i] = index (in base) of the image of base[i]
    val pos = IntArray(n) { i -> indexOf.getValue(p[base[i]]) }

    val code = IntArray(n)
    for (i in 0 until n) {
        val pi = pos[i]
        var c = 0
        for (j in i + 1 until n) if (pos[j] < pi) c++
        code[i] = c
    }
    return code
}

/**
 * Rank a permutation in lexicographic order using Lehmer code.
 */
fun <A> rankPermutation(p: Permutation<A>, base: FiniteSet.Ordered<A>): BigInteger {
    val n = base.size
    val code = lehmerCode(p, base)
    return code.withIndex().fold(BigInteger.ZERO) { acc, (i, ci) ->
        acc + ci.toBigInteger() * (n - 1 - i).bigFactorial()
    }
}

/**
 * Unrank a permutation from its lexicographic rank [r].
 * Uses the inverse of the Lehmer encoding.
 */
fun <A> unrankPermutation(base: FiniteSet.Ordered<A>, rank: BigInteger): Permutation<A> {
    val n = base.size
    require(rank >= BigInteger.ZERO && rank < n.bigFactorial()) {
        "Rank out of bounds for base of size $n."
    }

    var r = rank
    val digits = IntArray(n)
    for (i in 0 until n) {
        val fact = (n - 1 - i).bigFactorial()
        val (q, rem) = r.divideAndRemainder(fact)
        digits[i] = q.toInt()
        r = rem
    }

    val remaining = base.order.toMutableList()
    val permList = MutableList(n) { i -> remaining.removeAt(digits[i]) }
    val mapping  = base.order.indices.associate { i -> base[i] to permList[i] }
    return permutationFromMap(base, mapping)
}

/**
 * Lexicographic successor of a permutation. Returns null if none exists.
 */
fun <A> lexicographicSuccessor(
    p: Permutation<A>,
    base: FiniteSet.Ordered<A>
): Permutation<A>? {
    require(p.domain.toSet() == base.toSet()) { "Permutation/domain mismatch." }

    val n = base.size
    // current permutation as indices: i ↦ idx[i] where base[i] ↦ base[idx[i]]
    val idx = IntArray(n) { i -> base.indexOf(p[base[i]]) }

    // standard next_permutation on IntArray
    var i = n - 2
    while (i >= 0 && idx[i] >= idx[i + 1]) i--
    if (i < 0) return null

    var j = n - 1
    while (idx[j] <= idx[i]) j--

    // swap i, j
    idx[i] = idx[j].also { idx[j] = idx[i] }

    // reverse suffix [i+1, n)
    var l = i + 1
    var r = n - 1
    while (l < r) {
        idx[l] = idx[r].also { idx[r] = idx[l] }
        l++; r--
    }

    val mapping = base.order.indices.associate { k -> base[k] to base[idx[k]] }
    return permutationFromMap(base, mapping)
}

/**
 * Generate a random permutation using the Fisher–Yates shuffle.
 * This is an in-place O(n) algorithm with uniform distribution.
 */
fun <A> randomPermutation(base: FiniteSet.Ordered<A>, rng: Random = Random()): Permutation<A> {
    val elements = base.toList().toMutableList()
    for (i in elements.indices.reversed()) {
        val j = rng.nextInt(i + 1)
        if (i != j) elements[i] = elements[j].also { elements[j] = elements[i] }
    }
    val mapping = base.zip(elements).toMap()
    return permutationFromMap(base, mapping)
}