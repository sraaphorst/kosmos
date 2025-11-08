package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.FiniteSet
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

object Monoids {
    object StringMonoid: Monoid<String> {
        override val identity: String = ""
        override val op: BinOp<String> = BinOp(symbol = Symbols.PLUS) { a, b -> a + b}
    }

    fun <A> listMonoid(): Monoid<List<A>> = object : Monoid<List<A>> {
        override val identity: List<A> = emptyList()
        override val op: BinOp<List<A>> = BinOp(symbol = Symbols.PLUS) { a, b -> a + b }
    }

    fun <A> setUnionMonoid(): Monoid<FiniteSet<A>> = object : Monoid<FiniteSet<A>> {
        override val identity: FiniteSet<A> = FiniteSet.of()
        override val op: BinOp<FiniteSet<A>> = BinOp(symbol = Symbols.SET_UNION) { a, b -> a + b }
    }

    /**
     * For a finite set S, create the monoid (P(S), ∪) where the identity is S.
     */
    fun <A> setIntersectionMonoid(fullSet: FiniteSet<A>): Monoid<FiniteSet<A>> = object : Monoid<FiniteSet<A>> {
        override val identity: FiniteSet<A> = fullSet
        override val op: BinOp<FiniteSet<A>> = BinOp(symbol = Symbols.SET_INTERSECTION) { a, b ->
            check(a.isSubsetOf(fullSet)) { "Set $a is not a subset of $fullSet" }
            check(b.isSubsetOf(fullSet)) { "Set $b is not a subset of $fullSet" }
            a.intersect(b) }
    }
}
