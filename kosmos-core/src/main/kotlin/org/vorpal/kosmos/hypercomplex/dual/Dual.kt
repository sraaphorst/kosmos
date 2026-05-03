package org.vorpal.kosmos.hypercomplex.dual

/**
 * Dual carrier: a + bε.
 *
 * Values are intentionally dumb. Use [DualRing] for operations.
 */
data class Dual<F : Any>(
    val a: F,
    val b: F
)

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
