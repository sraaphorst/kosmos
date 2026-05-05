package org.vorpal.kosmos.hypercomplex.splitcomplex

import org.vorpal.kosmos.algebra.structures.CD

/**
 * Dumb carrier alias for split-complex-like Cayley-Dickson values.
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
 * Already over Real, split-complex numbers are not a field. They have zero divisors:
 * ```text
 * (1 + j)(1 - j) = 1 - j + j - j² = 0.
 * ```
 *
 * Mental model:
 * ```text
 * Complex:
 *     Real[x] / (x² + 1)
 *     canonical enough to expose as a concrete Real-based type.
 *
 * SplitComplex<R>:
 *     R[x] / (x² - 1)
 *     generic over the chosen base ring.
 * ```
 *
 * This file defines only carrier-level naming and accessors. The actual algebraic
 * structure is supplied by SplitComplexAlgebras.
 */
typealias SplitComplex<R> = CD<R>

val <R : Any> SplitComplex<R>.re: R get() = a

val <R : Any> SplitComplex<R>.hy: R get() = b

fun <R : Any> splitComplex(
    re: R,
    hy: R
): SplitComplex<R> =
    CD(re, hy)
