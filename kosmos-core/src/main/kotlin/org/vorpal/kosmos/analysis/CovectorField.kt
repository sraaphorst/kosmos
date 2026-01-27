package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A [CovectorField] assigns to each point in a [VectorSpace] a [Covector] on that space:
 *     ω : V → V*
 *
 * Each ω(p) is a covector (linear functional) acting on tangent vectors at point p.
 * This is the coordinate-free way to represent differential 1-forms.
 *
 * That is, for each point p in V, the field assigns a [Covector] on V:
 *
 *    `Covector<F, V> ≡ (V) -> F`
 *
 * Hence, a covector field is effectively:
 *
 *     `(V) -> ((V) -> F)`
 *
 * Example: The differential of a [ScalarField] f is a [CovectorField]:
 *
 *     `(df)_p(v) = Df(p)[v]` = directional derivative of f at p along v.
 */
interface CovectorField<F: Any, V: Any> {
    val space: VectorSpace<F, V>
    val field: Field<F>
        get() = space.field

    operator fun invoke(point: V): Covector<F, V>

    companion object {
        /**
         * Create a [CovectorField] from a function mapping each point to a [Covector].
         */
        fun <F : Any, V : Any> of(
            space: VectorSpace<F, V>,
            f: (V) -> Covector<F, V>
        ): CovectorField<F, V> = object : CovectorField<F, V> {
            override val space = space
            override fun invoke(point: V): Covector<F, V> = f(point)
        }

        /**
         * Create a constant [CovectorField] (same [Covector] everywhere).
         */
        fun <F : Any, V : Any> constant(
            space: VectorSpace<F, V>,
            covector: Covector<F, V>
        ): CovectorField<F, V> {
            requireSameSpace(space, covector.space)
            return of(space) { covector }
        }

        /**
         * The zero [CovectorField], mapping every point to the zero [Covector].
         */
        fun <F : Any, V : Any> zero(space: VectorSpace<F, V>): CovectorField<F, V> =
            of(space) { Covector.zero(space) }
    }
}

private fun <F : Any, V : Any> requireSameSpace(
    actual: VectorSpace<F, V>,
    expected: VectorSpace<F, V>,
) {
    require(actual === expected) { "Covectors must be over the same VectorSpace instance." }
}

/**
 * Apply a [CovectorField] ω to a [VectorField] X, producing a [ScalarField]:
 *     (ω(X))(p) = ω(p)(X(p))
 */
operator fun <F : Any, V : Any> CovectorField<F, V>.invoke(vf: VectorField<F, V>): ScalarField<F, V> {
    requireSameSpace(vf.space, space)
    return ScalarField.of(space) { p -> this(p)(vf(p)) }
}
