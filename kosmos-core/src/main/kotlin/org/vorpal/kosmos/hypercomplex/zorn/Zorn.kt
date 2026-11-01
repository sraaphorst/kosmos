package org.vorpal.kosmos.hypercomplex.zorn

import org.vorpal.kosmos.linear.values.Vec3

/**
 * A Zorn vector-matrix over a base ring [R]:
 * ```text
 *     ⎛ a  v ⎞
 * X = ⎜      ⎟,    a, d ∈ R,   v, w ∈ R³.
 *     ⎝ w  d ⎠
 * ```
 * As a module this is `R ⊕ R³ ⊕ R³ ⊕ R ≅ R⁸`.
 *
 * Despite the notation, a Zorn matrix is not a 2 × 2 matrix over `R`: the diagonal entries are
 * scalars and the off-diagonal entries are vectors. The product uses the dot and cross products
 * of `R³`:
 * ```text
 * ⎛ a₁ v₁ ⎞ ⎛ a₂ v₂ ⎞   ⎛ a₁a₂ + v₁·w₂              a₁v₂ + d₂v₁ + w₁ × w₂ ⎞
 * ⎜       ⎟ ⎜       ⎟ = ⎜                                                    ⎟,
 * ⎝ w₁ d₁ ⎠ ⎝ w₂ d₂ ⎠   ⎝ a₂w₁ + d₁w₂ − v₁ × v₂     d₁d₂ + w₁·v₂           ⎠
 * ```
 * with identity `(1, 0, 0, 1)` and conjugation `(a, v, w, d) ↦ (d, −v, −w, a)`. The norm is the
 * "determinant" `N(X) = ad − v·w`, and `X·conj(X) = conj(X)·X = N(X)·1`.
 *
 * Over any commutative ring this algebra is alternative but not associative, and it is a model of
 * the split-octonions that does not go through the Cayley-Dickson construction.
 *
 * This file defines only the carrier. The algebraic structure, the norm and the comparison with
 * [org.vorpal.kosmos.hypercomplex.splitoctonion.SplitOctonion] are supplied by [ZornAlgebras].
 *
 * @property a the upper-left scalar entry
 * @property v the upper-right vector entry
 * @property w the lower-left vector entry
 * @property d the lower-right scalar entry
 */
data class Zorn<R : Any>(
    val a: R,
    val v: Vec3<R>,
    val w: Vec3<R>,
    val d: R,
)

/**
 * The Zorn vector-matrix with diagonal entries [a], [d] and off-diagonal entries [v] (upper right)
 * and [w] (lower left).
 */
fun <R : Any> zorn(
    a: R,
    v: Vec3<R>,
    w: Vec3<R>,
    d: R
): Zorn<R> =
    Zorn(a, v, w, d)
