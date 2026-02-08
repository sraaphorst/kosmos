package org.vorpal.kosmos.algebra.poly

import org.vorpal.kosmos.core.Eq

/**
 * Equality for polynomials in canonical form (no trailing zeros).
 * If non-canonical polys can exist, normalize before comparing or the correct definition
 * of equality may not be enforced.
 */
fun <A : Any> polyEq(
    eqA: Eq<A>
): Eq<Poly<A>> = Eq { p, q ->
    val pc = p.coeffs
    val qc = q.coeffs

    if (pc.size != qc.size) false
    else pc.indices.all { i -> eqA(pc[i], qc[i]) }
}
