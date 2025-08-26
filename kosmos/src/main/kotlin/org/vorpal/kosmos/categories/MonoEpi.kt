package org.vorpal.kosmos.categories

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

private infix fun Boolean.implies(q: Boolean) = (!this) || q

fun <X, A, B> monoLawHolds(
    f: Morphism<A, B>, Xs: FiniteSet<X>, As: FiniteSet<A>,
    eqA: (A, A) -> Boolean, eqB: (B, B) -> Boolean,
    samples: Sequence<Pair<Morphism<X, A>, Morphism<X, A>>>): Boolean {
    val xs = Xs.toList()
    val eqG = { g1: Morphism<X, A>, g2: Morphism<X, A> -> xs.all { x -> eqA(g1.apply(x), g2.apply(x)) } }
    val eqFg = { g1: Morphism<X, A>, g2: Morphism<X, A> -> xs.all { x -> eqB(f.apply(g1.apply(x)), f.apply(g2.apply(x))) } }
    return samples.all { (g1, g2) -> eqFg(g1, g2) implies eqG(g1, g2) }
}

fun <A, B, Y> epiLawHolds(
    f: Morphism<A, B>, As: FiniteSet<A>, Bs: FiniteSet<B>, Ys: FiniteSet<Y>,
    eqB: (B, B) -> Boolean, eqY: (Y, Y) -> Boolean,
    samples: Sequence<Pair<Morphism<B, Y>, Morphism<B, Y>>>): Boolean {
    val image = As.toList().map(f::apply).distinct()
    val eqHOnImage = { h1: Morphism<B, Y>, h2: Morphism<B, Y> -> image.all { b -> eqY(h1.apply(b), h2.apply(b)) } }
    val eqHf = { h1: Morphism<B, Y>, h2: Morphism<B, Y> -> As.toList().all { a -> eqY(h1.apply(f.apply(a)), h2.apply(f.apply(a))) } }
    return samples.all { (h1, h2) -> eqHf(h1, h2) implies eqHOnImage(h1, h2) }
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