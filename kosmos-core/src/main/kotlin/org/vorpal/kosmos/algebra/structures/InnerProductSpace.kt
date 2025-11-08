package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.analysis.Covector
import org.vorpal.kosmos.analysis.Covectors

/**
 * Represents an **inner product space** (also called a *pre-Hilbert space*)
 * over a scalar field [F].
 *
 * Mathematically, an inner product space is a [VectorSpace] equipped with a
 * bilinear (or sesquilinear) form
 *
 * ```
 * ⟨·,·⟩ : V × V → F
 * ```
 *
 * that satisfies the following properties for all `u, v, w ∈ V`
 * and all scalars `a ∈ F`:
 *
 * 1. **Linearity in the first argument**
 *    ```
 *    ⟨a·u + v, w⟩ = a·⟨u, w⟩ + ⟨v, w⟩
 *    ```
 *
 * 2. **Conjugate symmetry** (for real fields, this is just symmetry)
 *    ```
 *    ⟨v, w⟩ = overline(⟨w, v⟩)
 *    ```
 *
 * 3. **Positive-definiteness**
 *    ```
 *    ⟨v, v⟩ ≥ 0  and  ⟨v, v⟩ = 0  ⇔  v = 0
 *    ```
 *
 * The inner product induces a **norm** and **metric** on the vector space:
 *
 * ```
 * ‖v‖ = sqrt(⟨v, v⟩)
 * d(u, v) = ‖u − v‖
 * ```
 *
 * Examples include Euclidean spaces (ℝⁿ with the standard dot product),
 * complex vector spaces with the Hermitian product,
 * and Hilbert spaces in functional analysis.
 *
 * ### In Kosmos
 * - [VectorSpace] defines the algebraic structure `(V, +, ⋅)`
 * - [InnerProductSpace] extends it by introducing `dot` and `norm`
 * - Specialized concrete implementations (e.g. `Vec2R`, `Vec3R`) provide
 *   efficient numeric realizations for real inner product spaces.
 *
 * @param F the scalar field type
 * @param V the concrete vector type, which must itself be an [InnerProductSpace]
 */
interface InnerProductSpace<F : Any, V : InnerProductSpace<F, V>> : VectorSpace<F, V> {
    /**
     * The inner (dot) product between two vectors.
     *
     * This operation defines geometric concepts such as
     * length, angle, and orthogonality.
     *
     * @param other the second vector
     * @return the scalar result of the inner product ⟨this, other⟩
     */
    infix fun dot(other: V): F

    /**
     * The induced norm (length) of this vector:
     * ‖v‖ = sqrt(⟨v, v⟩)
     */
    fun norm(): F =
        throw UnsupportedOperationException(
            "Norm not defined unless the field supports square roots."
        )

    /**
     * Convert this vector into its corresponding covector via the inner product:
     * v♭ : w ↦ ⟨v, w⟩
     */
    fun flat(): Covector<F, V> =
        Covectors.of(this) { w -> this dot w }

    /**
     * Convert a covector φ into the corresponding vector via the inner product inverse:
     * φ♯ : unique v such that φ(w) = ⟨v, w⟩ for all w
     *
     * (This requires that the inner product be non-degenerate.)
     */
    fun sharp(phi: Covector<F, V>): V =
        throw UnsupportedOperationException(
            "Default sharp not implemented. Override in concrete type."
        )
}