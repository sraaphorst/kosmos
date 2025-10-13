package org.vorpal.kosmos.categories

import org.vorpal.kosmos.combinatorics.FiniteSet

// TODO: This needs to be moved, probably to a testing location.
fun <A, B> isMonoSet(
    f: Morphism<A, B>,
    domain: FiniteSet<A>,
    eqB: (B, B) -> Boolean
): Boolean {
    val xs = domain.toList()
    val img = xs.map(f::apply)
    return xs.indices.none { i ->
        (i + 1 until xs.size).any { j -> eqB(img[i], img[j]) }
    }
}

fun <A, B> isEpiSet(f: Morphism<A, B>, domain: FiniteSet<A>, codomain: FiniteSet<B>, eqB: (B, B) -> Boolean): Boolean {
    val image = domain.toList().map(f::apply)
    return codomain.toList().all { b -> image.any { ib -> eqB(ib, b) } }
}

/** Small enumerator: use only for tiny FiniteSets. */
fun <A, B> allFunctions(domain: FiniteSet<A>, codomain: FiniteSet<B>) : Sequence<Morphism<A, B>> {
    val dom = domain.toList()
    val cod = codomain.toList()
    val idx = IntArray(dom.size) { 0 }
    var done = false
    return sequence {
        while (!done) {
            val table = dom.indices.associate { i -> dom[i] to cod[idx[i]] }
            yield(Morphism { a -> table.getValue(a) })
            var k = 0
            while (k < idx.size) {
                idx[k]++
                if (idx[k] < cod.size) break
                idx[k] = 0
                k++
            }
            if (k == idx.size) done = true
        }
    }
}