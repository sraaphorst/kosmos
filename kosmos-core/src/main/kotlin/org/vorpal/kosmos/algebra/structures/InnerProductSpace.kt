package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.analysis.Covector
import org.vorpal.kosmos.analysis.Covectors

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
    fun inner(v: V, w: V): F

    /**
     * Squared norm ‖v‖² = ⟨v, v⟩.
     */
    fun normSq(v: V): F =
        inner(v, v)

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
     *   v♭ : w ↦ ⟨v, w⟩.
     */
    fun flat(v: V): Covector<F, V> =
        Covectors.of(this) { w -> inner(v, w) }

    /**
     * "Sharp": φ ↦ φ♯, if the inner product is nondegenerate (Riesz isomorphism).
     *
     * Default is unimplemented; concrete spaces can override when they know
     * how to invert the Gram operator.
     */
    fun sharp(phi: Covector<F, V>): V =
        throw UnsupportedOperationException(
            "Default sharp not implemented; override in your concrete space."
        )
}