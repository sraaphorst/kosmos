package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.algebra.structures.instances.Real

/**
 * A real normed division *-algebra:
 *
 *  - an involutive algebra (`add`, `mul`, `conj`),
 *  - with multiplicative inverses for all nonzero elements,
 *  - equipped with a squared norm `N: A → ℝ` satisfying:
 *```
 *      N(a) ≥ 0
 *      N(a) = 0  ⇔  a = 0
 *      N(a * b) = N(a) * N(b)
 *```
 * In Kosmos, the canonical examples are:
 *   ℝ, ℂ, ℍ, 𝕆.
 */
interface NormedDivisionAlgebra<A : Any>: NonAssociativeDivisionAlgebra<A> {
    /**
     * Squared norm N(a) (so we avoid a sqrt).
     */
    fun normSq(a: A): Real

    /**
     * Convenience of actual norm if needed.
     */
    fun norm(a: A): Real = kotlin.math.sqrt(normSq(a))
}