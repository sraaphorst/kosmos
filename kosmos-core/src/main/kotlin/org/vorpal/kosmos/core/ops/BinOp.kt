package org.vorpal.kosmos.core.ops

/** A binary operation with no prescribed properties. */
fun interface BinOp<A> {
    fun combine(a: A, b: A): A
}

/**
 * Tag types are used on operators to identify them, e.g. to distinguish
 * additive vs multiplicative operations on the same structure such as a Ring.
 * */
sealed interface OpTag
object Add: OpTag
object Mul: OpTag
object Star: OpTag
