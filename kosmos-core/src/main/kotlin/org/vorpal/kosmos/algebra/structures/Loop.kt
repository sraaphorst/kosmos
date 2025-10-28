package org.vorpal.kosmos.algebra.structures

/** A Loop is a Quasigroup with an identity element over the operation. */
interface Loop<A: Any> : Quasigroup<A> {
    val identity: A
}
