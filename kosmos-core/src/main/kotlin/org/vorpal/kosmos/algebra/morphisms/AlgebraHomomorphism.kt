package org.vorpal.kosmos.algebra.morphisms

import org.vorpal.kosmos.categories.Morphism
import org.vorpal.kosmos.core.ops.UnaryOp

interface AlgebraHomomorphism<A : Any, B : Any> : Morphism<A, B> {
    val map: UnaryOp<A, B>
    override fun apply(a: A): B = map(a)
    operator fun invoke(a: A): B = map(a)
}
