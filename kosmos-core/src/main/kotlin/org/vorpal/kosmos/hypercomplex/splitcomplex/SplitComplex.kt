package org.vorpal.kosmos.hypercomplex.splitcomplex

import org.vorpal.kosmos.algebra.structures.CD

/**
 * A split-complex (hyperbolic) number `a + bj` over a base ring [R], with `j² = 1`.
 *
 * Ordinary complex numbers are treated in Kosmos as the concrete scalar type:
 * ```text
 * Complex = Real ⊕ Real·i, where i² = -1.
 * ```
 *
 * Split-complex numbers are better viewed as a generic construction over a base ring:
 * ```text
 * SplitComplex<R> = R[j] / (j² - 1), so j² = 1.
 * ```
 * Concretely:
 * ```text
 * a + b j, where a, b ∈ R.
 * ```
 *
 * Algebraically, this is the Cayley-Dickson doubling of `R` (with the trivial involution) at
 * `σ = -1`; see [org.vorpal.kosmos.algebra.structures.CayleyDickson.commutative]. The carrier is a
 * [CD] pair and all arithmetic is supplied by that construction through [SplitComplexAlgebras].
 *
 * Already over Real, split-complex numbers are not a field. They have zero divisors:
 * ```text
 * (1 + j)(1 - j) = 1 - j + j - j² = 0.
 * ```
 *
 * [SplitComplex] is a value class rather than a typealias of [CD] so that `SplitComplex<Real>` is a
 * distinct Kotlin type from `Complex = CD<Real>` and `Dual<Real>`: the three algebras share a carrier
 * but not a multiplication, and a complex number must not be accepted by the split-complex ring.
 *
 * This file defines only carrier-level naming and accessors. The actual algebraic
 * structure is supplied by [SplitComplexAlgebras].
 */
@JvmInline
value class SplitComplex<R : Any>(val cd: CD<R>) {
    constructor(re: R, hy: R) : this(CD(re, hy))

    /** The real part `a` of `a + bj`. */
    val re: R
        get() = cd.a

    /** The hyperbolic part `b` of `a + bj`. */
    val hy: R
        get() = cd.b

    operator fun component1(): R = re
    operator fun component2(): R = hy

    override fun toString(): String =
        "SplitComplex(re=$re, hy=$hy)"
}

fun <R : Any> splitComplex(
    re: R,
    hy: R
): SplitComplex<R> =
    SplitComplex(re, hy)
