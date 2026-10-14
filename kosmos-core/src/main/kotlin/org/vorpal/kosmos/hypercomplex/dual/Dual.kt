package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.algebra.structures.CD

/**
 * A dual number `a + bε` over a base ring [R], with `ε² = 0`.
 *
 * Algebraically, the dual numbers are the Cayley-Dickson doubling of a commutative base ring
 * (with the trivial involution) at `σ = 0`:
 * ```text
 * Dual<R> = R[ε] / (ε²)
 * ```
 * so the carrier is a [CD] pair `(a, b)` and all arithmetic is supplied by the Cayley-Dickson
 * construction through [DualAlgebras]; see
 * [org.vorpal.kosmos.algebra.structures.CayleyDickson.commutative].
 *
 * The dual numbers are never a field, even over ℝ: `ε` is nilpotent, so every `bε` is a zero divisor.
 * An element is invertible exactly when its primal part `a` is.
 *
 * [Dual] is a value class rather than a typealias of [CD] so that `Dual<R>` is a distinct Kotlin
 * type from `Complex<R>` (`i² = -1`) and `SplitComplex<R>` (`j² = +1`): the three algebras share a
 * carrier but not a multiplication.
 *
 * This file defines only carrier-level naming and accessors. The algebraic structure is supplied
 * by [DualAlgebras].
 */
@JvmInline
value class Dual<R : Any>(val cd: CD<R>) {
    constructor(a: R, b: R) : this(CD(a, b))

    /** The primal (real) part `a` of `a + bε`. */
    val a: R
        get() = cd.a

    /** The tangent (infinitesimal) part `b` of `a + bε`. */
    val b: R
        get() = cd.b

    operator fun component1(): R = a
    operator fun component2(): R = b

    override fun toString(): String =
        "Dual(a=$a, b=$b)"
}

/**
 * The primal part, under its automatic-differentiation name: if `x = a + bε` is the image of a
 * point under a function, `x.f` is the function's value there.
 */
val <R : Any> Dual<R>.f: R
    get() = a

/**
 * The tangent part, under its automatic-differentiation name: the derivative carried along with the
 * value [f].
 */
val <R : Any> Dual<R>.df: R
    get() = b

/**
 * The dual number `f + df·ε`.
 */
fun <R : Any> dual(f: R, df: R): Dual<R> =
    Dual(f, df)
