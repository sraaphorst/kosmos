package org.vorpal.kosmos.analysis

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
    operator fun invoke(point: V): Covector<F, V>
}

/**
 * Apply a [CovectorField] ω to a [VectorField] X, producing a [ScalarField]:
 *     (ω(X))(p) = ω(p)(X(p))
 */
operator fun <F : Any, V : Any> CovectorField<F, V>.invoke(vf: VectorField<F, V>): ScalarField<F, V> =
    ScalarFields.of(space) { p -> this(p)(vf(p)) }

/**
 * Differential of a [ScalarField], yielding a [CovectorField]:
 *
 * (df)_p(v) = Df(p)[v]
 *
 * The [derivative] parameter determines how to compute directional derivatives.
 * For now, it can be a finite-difference placeholder until we plug in an AD system.
 */
fun <F : Any, V : Any> differential(f: ScalarField<F, V>, derivative: (V, (V) -> F) -> Covector<F, V>): CovectorField<F, V> =
    CovectorFields.of(f.space) { p -> derivative(p, f::invoke) }

/**
 * If you have an inner product ⟨·,·⟩, you can identify vectors and covectors via the musical isomorphisms
 * flat and sharp.
 */
fun <F : Any, V : Any> gradient(
    f: ScalarField<F, V>,
    metric: (V) -> (V, V) -> F
): VectorField<F, V> =
    VectorFields.of(f.space) { p ->
        metric(p)
        differential(f) { point, func ->
            // Build Covector<F, V> at point using the local derivative of f
            TODO("Implement derivative")
        }
        // Map covector to vector via metric inverse (sharp)
        TODO("Implement sharp isomorphism")
    }

/**
 * Factory and utility functions for constructing [CovectorField]s.
 */
object CovectorFields {
    /**
     * Create a [CovectorField] from a function mapping each point to a [Covector].
     */
    fun <F : Any, V : Any> of(space: VectorSpace<F, V>, f: (V) -> Covector<F, V>): CovectorField<F, V> =
        object : CovectorField<F, V> {
            override val space = space
            override fun invoke(point: V): Covector<F, V> = f(point)
        }

    /**
     * Create a constant [CovectorField] (same [Covector] everywhere).
     */
    fun <F : Any, V : Any> constant(space: VectorSpace<F, V>, covector: Covector<F, V>): CovectorField<F, V> =
        of(space) { covector }

    /**
     * The zero [CovectorField], mapping every point to the zero [Covector].
     */
    fun <F : Any, V : Any> zero(space: VectorSpace<F, V>): CovectorField<F, V> =
        of(space) { Covectors.zero(space) }
}
