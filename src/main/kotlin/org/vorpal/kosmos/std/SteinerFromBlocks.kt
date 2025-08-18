package org.vorpal.kosmos.std

import org.vorpal.kosmos.algebra.ops.Star
import org.vorpal.kosmos.algebra.structures.CommutativeIdempotentQuasigroup
import org.vorpal.kosmos.categories.FiniteSet

/**
 * Build a Steiner (commutative idempotent) quasigroup on `points` from STS blocks (triples).
 * Requirements checked:
 *  - each block has 3 distinct elements, all in `points`
 *  - every unordered pair {x,y}, x != y, appears in exactly one block
 * Result satisfies:
 *  - x ⋆ x = x  (idempotent)
 *  - x ⋆ y = y ⋆ x (commutative)
 *  - left/right division coincide with ⋆ in a Steiner quasigroup
 */
fun <A> steinerFromBlocks(
    points: FiniteSet<A>,
    blocks: List<Triple<A, A, A>>
): CommutativeIdempotentQuasigroup<A, Star> {
    val xs = points.toList()
    val index = xs.withIndex().associate { it.value to it.index } // total order for keys

    fun key(a: A, b: A): Pair<Int, Int> {
        val ia = index[a] ?: error("Element $a not in points")
        val ib = index[b] ?: error("Element $b not in points")
        require(ia != ib) { "Pair key requires distinct elements: $a, $b" }
        return if (ia < ib) ia to ib else ib to ia
    }

    val third = HashMap<Pair<Int, Int>, A>(xs.size * (xs.size - 1) / 2)

    fun addBlock(a: A, b: A, c: A) {
        require(a != b && a != c && b != c) { "Block must contain 3 distinct elements: ($a,$b,$c)" }
        listOf(
            Triple(a, b, c),
            Triple(a, c, b),
            Triple(b, c, a)
        ).forEach { (u, v, w) ->
            val k = key(u, v)
            val prev = third.putIfAbsent(k, w)
            require(prev == null) { "Pair {$u,$v} appears in more than one block" }
        }
    }

    // validate blocks + fill map
    for ((a, b, c) in blocks) {
        require(a in points && b in points && c in points) {
            "Block ($a,$b,$c) contains element not in points"
        }
        addBlock(a, b, c)
    }

    // every unordered pair must be covered once
    val n = xs.size
    val expectedPairs = n * (n - 1) / 2
    require(third.size == expectedPairs) {
        "Incomplete coverage: have ${third.size} pairs, expected $expectedPairs"
    }

    // Build the quasigroup
    return object : CommutativeIdempotentQuasigroup<A, Star> {
        override fun combine(a: A, b: A): A =
            if (a == b) a
            else third[key(a, b)] ?: error("No block contains the pair {$a,$b}")

        override fun ldiv(a: A, b: A): A = combine(a, b)  // Steiner property
        override fun rdiv(b: A, a: A): A = combine(b, a)
    }
}