package org.vorpal.kosmos.categories

import org.vorpal.kosmos.core.implies

object MonoEpi {
    fun <X, A, B> monoLawHolds(
        f: Morphism<A, B>, setX: FiniteSet<X>, setA: FiniteSet<A>,
        eqA: (A, A) -> Boolean, eqB: (B, B) -> Boolean,
        samples: Sequence<Pair<Morphism<X, A>, Morphism<X, A>>>): Boolean {
        val xs = setX.toList()
        val eqG = { g1: Morphism<X, A>, g2: Morphism<X, A> -> xs.all { x -> eqA(g1.apply(x), g2.apply(x)) } }
        val eqFg = { g1: Morphism<X, A>, g2: Morphism<X, A> -> xs.all { x -> eqB(f.apply(g1.apply(x)), f.apply(g2.apply(x))) } }
        return samples.all { (g1, g2) -> eqFg(g1, g2) implies eqG(g1, g2) }
    }

    fun <A, B, Y> epiLawHolds(
        f: Morphism<A, B>, setA: FiniteSet<A>, setB: FiniteSet<B>, setY: FiniteSet<Y>,
        eqB: (B, B) -> Boolean, eqY: (Y, Y) -> Boolean,
        samples: Sequence<Pair<Morphism<B, Y>, Morphism<B, Y>>>): Boolean {
        val image = setA.toList().map(f::apply).distinct()
        val eqHOnImage = { h1: Morphism<B, Y>, h2: Morphism<B, Y> -> image.all { b -> eqY(h1.apply(b), h2.apply(b)) } }
        val eqHf = { h1: Morphism<B, Y>, h2: Morphism<B, Y> -> setA.toList().all { a -> eqY(h1.apply(f.apply(a)), h2.apply(f.apply(a))) } }
        return samples.all { (h1, h2) -> eqHf(h1, h2) implies eqHOnImage(h1, h2) }
    }
}