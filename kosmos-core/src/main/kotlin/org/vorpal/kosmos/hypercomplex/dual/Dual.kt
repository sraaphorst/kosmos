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
