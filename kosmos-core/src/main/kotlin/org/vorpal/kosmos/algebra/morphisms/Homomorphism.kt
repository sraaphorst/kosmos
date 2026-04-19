package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.ops.UnaryOp

/**
 * A homomorphism from some algebra over [A] to another over [B].
 *
 * This extends the concept of a general [Morphism].
 */
interface Homomorphism<A : Any, B : Any> : Morphism<A, B> {
    val map: UnaryOp<A, B>
    override fun apply(a: A): B = map(a)
    operator fun invoke(a: A): B = map(a)
}
