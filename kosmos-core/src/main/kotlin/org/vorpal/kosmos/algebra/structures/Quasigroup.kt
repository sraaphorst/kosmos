package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp

/** A quasigroup: for all a,b there exist unique x,y with a⋆x=b and y⋆a=b. */
interface Quasigroup<A: Any> : Magma<A> {
    /** Left division: the unique x with a ⋆ x = b. */
    fun leftDiv(a: A, b: A): A
    /** Right division: the unique y with y ⋆ a = b. */
    fun rightDiv(a: A, b: A): A
}

val <A: Any> Quasigroup<A>.leftDiv: BinOp<A>
    get() = BinOp(Symbols.SLASH) { a, b -> this.leftDiv(a, b) }
val <A: Any> Quasigroup<A>.rightDiv: BinOp<A>
    get() = BinOp(Symbols.BACKSLASH) { a, b -> this.rightDiv(a, b) }
