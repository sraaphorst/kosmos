package org.vorpal.kosmos.algebra.structures

/** A quasigroup: for all a,b there exist unique x,y with a⋆x=b and y⋆a=b. */
interface Quasigroup<A: Any> : Magma<A> {
    /** Left division: the unique x with a ⋆ x = b. */
    fun leftDiv(a: A, b: A): A
    /** Right division: the unique y with y ⋆ a = b. */
    fun rightDiv(b: A, a: A): A
}
