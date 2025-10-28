package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A [Covector] is a linear functional defined on a [VectorSpace] `V` over a [Field] `𝔽`:
 *
 *    `ω : V → 𝔽`
 *
 * where `𝔽` is the underlying field of scalars for `V`.
 *
 * Mathematically: `ω ∈ V*`, where `V* = Hom(V, 𝔽)`,
 *
 * Covectors form the **dual space** `V*` of linear maps from `V` to its field.
 *
 * Each [Covector] is a function that takes a vector and returns a scalar
 * from the same field underlying the [VectorSpace].
 *
 * Example: the dot product becomes a covector if you fix one of its vectors, e.g. <v, .>
 */
interface Covector<F : Any, V : Any> {
    val space: VectorSpace<F, V>
    operator fun invoke(v: V): F
}

/**
 * Factory and utility functions for constructing [Covector]s.
 */
object Covectors {
    /**
     * Creates a [Covector] from a function V → F.
     */
    fun <F : Any, V : Any> of(
        space: VectorSpace<F, V>,
        f: (V) -> F
    ): Covector<F, V> = object : Covector<F, V> {
        override val space = space
        override fun invoke(v: V): F = f(v)
    }

    /**
     * Creates a [Covector] that returns a constant scalar for all inputs.
     */
    fun <F: Any, V: Any> constant(space: VectorSpace<F, V>, f: F): Covector<F, V> =
        of(space) { f }

    /**
     * The zero [Covector], which maps every vector to the additive identity (0) of the field.
     */
    fun <F: Any, V: Any> zero(space: VectorSpace<F, V>): Covector<F, V> =
        constant(space, space.field.add.identity)
}
