package org.vorpal.kosmos.algebra.ops

// A SAM binary operation with no prescribed properties.
fun interface BinOp<A> {
    fun combine(a: A, b: A): A
}

// Tag types to distinguish additive vs multiplicative structure on the same structure.
sealed interface OpTag
object Add: OpTag
object Mul: OpTag
object Star: OpTag
