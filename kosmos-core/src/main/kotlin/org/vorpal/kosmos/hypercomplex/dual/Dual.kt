package org.vorpal.kosmos.hypercomplex.dual

import org.vorpal.kosmos.algebra.structures.CD

/**
 * A dual number `a + bε` over a base [F], with `ε² = 0`.
 *
 * Algebraically, the dual numbers are the Cayley-Dickson doubling of a commutative base ring
 * (with the trivial involution) at `σ = 0`:
 * ```text
 * Dual(F) = F[ε] / (ε²)
 * ```
 * so the carrier is represented by a [CD] pair `(a, b)` and all arithmetic is supplied by the
 * Cayley-Dickson construction; see [DualAlgebras].
 *
 * [Dual] is a value class rather than a typealias of [CD] so that `Dual<Real>` is a distinct
 * Kotlin type from `Complex = CD<Real>` and `SplitComplex<Real> = CD<Real>`: the three algebras
 * share a carrier but not a multiplication.
 *
 * Values are intentionally dumb. Use [DualAlgebras.DualCommutativeRing] for operations.
 */
@JvmInline
value class Dual<F : Any>(val cd: CD<F>) {
    constructor(a: F, b: F) : this(CD(a, b))

    /** The primal / real part `a` of `a + bε`. */
    val a: F
        get() = cd.a

    /** The tangent / infinitesimal part `b` of `a + bε`. */
    val b: F
        get() = cd.b

    operator fun component1(): F = a
    operator fun component2(): F = b

    override fun toString(): String =
        "Dual(a=$a, b=$b)"
}

val <F : Any> Dual<F>.f: F
    get() = a

val <F : Any> Dual<F>.df: F
    get() = b

val <F : Any> Dual<F>.real: F
    get() = a

val <F : Any> Dual<F>.eps: F
    get() = b

fun <F : Any> dual(f: F, df: F): Dual<F> =
    Dual(f, df)
