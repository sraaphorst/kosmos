package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.finiteset.FiniteSet
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
}
