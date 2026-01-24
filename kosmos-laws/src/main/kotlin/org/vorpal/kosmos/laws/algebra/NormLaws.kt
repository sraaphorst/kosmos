package org.vorpal.kosmos.laws.algebra

import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.math.Real

object NormLaws {

    /**
     * Multiplicativity of normSq:
     *   N(x*y) = N(x) * N(y)
     *
     * Requires an equality on N (exact or approximate) and a multiplication on N.
     *
     * Note: we don't assume N is ordered.
     */
    fun <N : Any, A : Any> normSqMultiplicative(
        alg: NormedDivisionAlgebra<N, A>,
        eqN: Eq<N>,
        mulN: (N, N) -> N,
        x: A,
        y: A
    ): Boolean {
        val left = alg.normSq(alg.mul(x, y))
        val right = mulN(alg.normSq(x), alg.normSq(y))
        return eqN(left, right)
    }

    /**
     * Definiteness (zero iff element is zero):
     *   N(x) = 0  <=>  x = 0
     *
     * This only needs an equality on N and an equality on A.
     */
    fun <N : Any, A : Any> normSqDefinite(
        alg: NormedDivisionAlgebra<N, A>,
        eqN: Eq<N>,
        zeroN: N,
        eqA: Eq<A>,
        x: A
    ): Boolean {
        val n0 = eqN(alg.normSq(x), zeroN)
        val x0 = eqA(x, alg.zero)
        return n0 == x0
    }

    /**
     * Real-only: nonnegativity N(x) >= 0.
     *
     * This assumes normSq returns Real and uses a tolerance.
     */
    fun <A : Any> realNormSqNonnegative(
        alg: RealNormedDivisionAlgebra<A>,
        x: A,
        eps: Real = 0.0
    ): Boolean =
        alg.normSq(x) >= -eps

    /**
     * Real-only: definiteness using approximate real equality.
     * This is often what you want numerically.
     */
    fun <A : Any> realNormSqDefiniteApprox(
        alg: RealNormedDivisionAlgebra<A>,
        eqA: Eq<A>,
        x: A,
        eps: Real = 1e-12
    ): Boolean {
        val eqR = Eqs.realApprox(eps)
        val n0 = eqR(alg.normSq(x), 0.0)
        val x0 = eqA(x, alg.zero)
        return n0 == x0
    }
}