package org.vorpal.kosmos.geometry.lattices

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.ops.LeftAction
import java.math.BigInteger

/**
 * An interface for a lattice in a vector space over the integers.
 *
 * NOTE: This class has a validate method that should be called when an instance is created.
 */
interface ZLattice<V : Any> {
    val rank: Int

    /**
     * Basis vectors `b_1, ... ,b_k` in the ambient space `V.
     */
    val basis: List<V>

    /**
     * Additive structure on V (no scalars needed besides multiplying by integers).
     */
    val addV: AbelianGroup<V>

    /**
     * Scale a vector by an integer coefficient.
     */
    val scale: LeftAction<BigInteger, V>

    fun validate() {
        require(rank >= 0) { "rank must be nonnegative: $rank" }
        require(basis.size == rank) { "basis size ${basis.size} must equal rank $rank" }
    }

    /**
     * The linear map Φ_B: Z^rank → V.
     */
    fun embed(coeffs: List<BigInteger>): V {
        require(coeffs.size == rank) {
            "expected $rank coefficients, got ${coeffs.size}"
        }

        return coeffs
            .zip(basis)
            .fold(addV.identity) { acc, (z, b) ->
                addV(acc, scale(z, b))
            }
    }
}
