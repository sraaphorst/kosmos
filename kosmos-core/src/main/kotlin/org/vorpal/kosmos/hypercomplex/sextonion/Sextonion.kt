package org.vorpal.kosmos.hypercomplex.sextonion

import org.vorpal.kosmos.linear.values.Mat2
import org.vorpal.kosmos.linear.values.Vec2

/**
 * A sextonion over a commutative base ring [R], stored as a pair
 * ```text
 * (m, ν) ∈ M₂(R) ⊕ R²,
 * ```
 * where `m` is a 2 × 2 matrix and `ν` is a row vector. As a module this is `R⁶`.
 *
 * The product, with identity `(I, 0)`, is
 * ```text
 * (A, ν)(B, μ) = (AB, μA + ν·adj(B)),
 * ```
 * where `adj` is the adjugate. Conjugation is `(A, ν) ↦ (adj A, −ν)` and the norm is
 * `N(A, ν) = det A`, so `x·conj(x) = conj(x)·x = N(x)·1`.
 *
 * Over any commutative ring this algebra is alternative but not associative. It is the
 * six-dimensional subalgebra of the split-octonions sitting between the split-quaternions
 * `M₂(R)` and the split-octonions:
 * - the pairs `(A, 0)` form a copy of `M₂(R)`;
 * - the pairs `(0, ν)` form an ideal `V` with `V·V = 0`, the radical of the (degenerate) norm;
 * - `S / V ≅ M₂(R)`.
 *
 * Because the norm vanishes on `V`, the sextonions are not a composition algebra.
 *
 * We do not store a sextonion as a [org.vorpal.kosmos.hypercomplex.zorn.Zorn] vector-matrix:
 * Zorn matrices are 8-dimensional, and the sextonions would need two entries forced to zero.
 * Every `(m, ν)` is a sextonion, so there is nothing to police.
 *
 * This file defines only the carrier. The algebraic structure is supplied by [SextonionAlgebras].
 *
 * @property m the matrix part, the image in `S / V ≅ M₂(R)`
 * @property nu the row vector `ν`, the component in the radical `V`
 */
data class Sextonion<R : Any>(
    val m: Mat2<R>,
    val nu: Vec2<R>,
) {
    /**
     * The six coefficients `(m.a, m.b, m.c, m.d, ν.x, ν.y)`, in the basis
     * `E₁₁, E₁₂, E₂₁, E₂₂, n₁, n₂` where the `Eᵢⱼ` are the matrix units `(Eᵢⱼ, 0)` and
     * `n₁ = (0, (1, 0))`, `n₂ = (0, (0, 1))`.
     */
    fun coefficients(): List<R> =
        listOf(m.a, m.b, m.c, m.d, nu.x, nu.y)
}
