package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import kotlin.math.sqrt

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
    val normSq: UnaryOp<A, Real>

    /**
     * Convenience of actual norm if needed.
     */
    fun norm(a: A): Real = sqrt(normSq(a))

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>,
            reciprocal: Endo<A>,
            conj: Endo<A>,
            normSq: UnaryOp<A, Real>
        ): NormedDivisionAlgebra<A> = object : NormedDivisionAlgebra<A> {
            override val zero: A = add.identity
            override val add = add
            override val mul = mul
            override val reciprocal = reciprocal
            override val conj = conj
            override val normSq = normSq
        }
    }
}
