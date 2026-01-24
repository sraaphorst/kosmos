package org.vorpal.kosmos.core.finiteset.combinatorics

import org.vorpal.kosmos.core.finiteset.FiniteSet

/**
 * Build a subset (as a list of elements) from a mask over [pool].
 *
 * Returns the chosen elements in pool-order (stable).
 */
private fun <A> subsetElementsFromMask(pool: List<A>, mask: Int): List<A> {
    val n = pool.size
    val out = ArrayList<A>(Integer.bitCount(mask))
    var i = 0
    while (i < n) {
        if (((mask ushr i) and 1) == 1) out.add(pool[i])
        i += 1
    }
    return out
}

/**
 * Generic powerset builder:
 * - uses [pool] as the stable element ordering for mask interpretation
 * - [graded] chooses output ordering policy (by size then mask, or just mask)
 * - [subsetFactory] decides Ordered vs Unordered subsets
 * - [outerFactory] decides Ordered vs Unordered collection-of-subsets
 */
private fun <A, S, P> powerSetCore(
    pool: List<A>,
    graded: Boolean,
    subsetFactory: (List<A>) -> S,
    outerFactory: (List<S>) -> P,
): P {
    val n = pool.size
    require(n <= 30) { "Set size $n is too large for power set generation." }

    val total = 1 shl n
    val out = ArrayList<S>(total)

    fun emit(mask: Int) {
        val elems = subsetElementsFromMask(pool, mask)
        out.add(subsetFactory(elems))
    }

    if (!graded) {
        var mask = 0
        while (mask < total) {
            emit(mask)
            mask += 1
        }
        return outerFactory(out)
    }

    // graded: bucket masks by popcount
    val buckets = Array(n + 1) { IntArray(total) }
    val sizes = IntArray(n + 1)

    var mask = 0
    while (mask < total) {
        val k = Integer.bitCount(mask)
        val idx = sizes[k]
        buckets[k][idx] = mask
        sizes[k] = idx + 1
        mask += 1
    }

    var k = 0
    while (k <= n) {
        var i = 0
        while (i < sizes[k]) {
            emit(buckets[k][i])
            i += 1
        }
        k += 1
    }

    return outerFactory(out)
}

/**
 * Unordered powerset: returns an unordered finite set of unordered subsets.
 *
 * The element pool is stabilized via toOrdered().order so results are repeatable.
 */
fun <A> FiniteSet.Unordered<A>.powerSetUnordered(): FiniteSet.Unordered<FiniteSet.Unordered<A>> {
    val pool = this.toOrdered().order
    return powerSetCore(
        pool = pool,
        graded = false,
        subsetFactory = { elems -> FiniteSet.unordered(elems) },
        outerFactory = { subsets -> FiniteSet.unordered(subsets) }
    )
}

/**
 * Ordered powerset in graded order (by subset size, then mask):
 * returns an ordered finite set of ordered subsets.
 */
fun <A> FiniteSet.Ordered<A>.powerSetOrderedGraded(): FiniteSet.Ordered<FiniteSet.Ordered<A>> {
    val pool = this.order
    return powerSetCore(
        pool = pool,
        graded = true,
        subsetFactory = { elems -> FiniteSet.ordered(elems) },
        outerFactory = { subsets -> FiniteSet.ordered(subsets) }
    )
}

fun <A> FiniteSet.Ordered<A>.powerSet(): FiniteSet.Ordered<FiniteSet.Ordered<A>> =
    powerSetCore(
        pool = this.order,
        graded = true,
        subsetFactory = { elems -> FiniteSet.ordered(elems) },
        outerFactory = { subsets -> FiniteSet.ordered(subsets) }
    )

fun <A> FiniteSet.Unordered<A>.powerSet(): FiniteSet.Unordered<FiniteSet.Unordered<A>> =
    powerSetCore(
        pool = this.toOrdered().order,
        graded = false,
        subsetFactory = { elems -> FiniteSet.unordered(elems) },
        outerFactory = { subsets -> FiniteSet.unordered(subsets) }
    )

fun <A: Any> recsize(st: FiniteSet.Ordered<A>) {
    println("*** SIZE: ${st.size} ***")
    st.forEach { println(it.toString()) }
    if (st.size >= 10)
        return
    recsize(st.powerSet())
}

fun main() {
//    recsize(FiniteSet.ordered<Unit>())
    val s = FiniteSet.unordered(1, 2, 3, 4)
    val ps = s.powerSetUnordered()
    ps.forEach { println(it.toList()) }
}