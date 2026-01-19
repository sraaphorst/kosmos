package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.ops.BilinearForm

/**
 * Inner product space (pre-Hilbert space) over a scalar field F with vectors V.
 *
 * This is a VectorSpace<F, V> equipped with an inner product
 *   ⟨·,·⟩ : V × V → F
 */
interface InnerProductSpace<F : Any, V : Any> : VectorSpace<F, V> {

    /**
     * Inner product ⟨v, w⟩.
     *
     * For real spaces: symmetric, bilinear, positive-definite.
     * For complex spaces: conjugate-symmetric, sesquilinear, positive-definite.
     */
    val inner: BilinearForm<V, F>

    /**
     * Squared norm ‖v‖² = ⟨v, v⟩.
     *
     * Note: In real inner product spaces, ⟨v,v⟩ lies in the scalar field ℝ.
     * In complex (Hermitian) inner product spaces, ⟨v,v⟩ is real and ≥ 0,
     * even though ⟨·,·⟩ : V×V → ℂ. Modeling that cleanly likely needs a
     * separate HermitianInnerProductSpace type (or a “RealPart” scalar view).
     * TODO: This needs to be revisited. For the complex case, this is sesquilinear, i.e.
     * TODO: conjugate-linear in one argument and linear in the other.
     */
    fun normSq(v: V): F = inner(v, v)

    /**
     * Norm ‖v‖ = sqrt(⟨v, v⟩).
     *
     * Only makes sense when the scalar field supports square roots, so by
     * default this is left unimplemented.
     */
    fun norm(v: V): F =
        throw UnsupportedOperationException(
            "Norm not defined unless the field supports square roots."
        )

    /**
     * "Flat" / musical isomorphism: v ↦ v♭, where
     * ```
     * v♭ : w ↦ ⟨v, w⟩
     * ```
     * Note: returned a Covector<F, V>, but we don't want a circular dependency with the analysis package.
     *
     */
    fun flat(v: V): (V) -> F =
        { w -> inner(v, w) }

    /**
     * "Sharp": φ ↦ φ♯, if the inner product is nondegenerate (Riesz isomorphism).
     *
     * Default is unimplemented; concrete spaces can override when they know
     * how to invert the Gram operator.
     *
     * Note: phi was a Covector<F, V>, but we don't want a circular dependency with the analysis package.
     */
    fun sharp(phi: (F) -> V): V =
        throw UnsupportedOperationException(
            "Default sharp not implemented; override in your concrete space."
        )
}