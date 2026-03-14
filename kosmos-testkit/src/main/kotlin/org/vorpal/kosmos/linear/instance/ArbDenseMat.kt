package org.vorpal.kosmos.linear.instance

import io.kotest.property.Arb
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.linear.values.DenseMat

/**
 * Given an arbitrary and a size n, create an Arb<DenseMat<F>> to generate nxn matrices where all elements are the same.
 */
fun <F : Any> arbConstMat(
    arbF: Arb<F>,
    n: Int,
): Arb<DenseMat<F>> {
    require(n > 0) { "Matrix dimensions must be positive, got $n" }
    return arbF.map { f -> DenseMat.tabulate(n, n) { _, _ -> f} }
}
