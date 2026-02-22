package org.vorpal.kosmos.geometry.lattices

import org.vorpal.kosmos.linear.values.DenseMat

interface EuclideanLattice<V : Any, S : Any> : ZLattice<V> {
    /**
     * Symmetric bilinear form ⟨·,·⟩ returning scalar S (often Real, Rational, etc).
     */
    val dot: (V, V) -> S

    /**
     * Derived Gram matrix G_ij = ⟨b_i, b_j⟩.
     */
    fun gram(): DenseMat<S> = DenseMat.tabulate(rank, rank) { i, j ->
        dot(basis[i], basis[j])
    }
}
