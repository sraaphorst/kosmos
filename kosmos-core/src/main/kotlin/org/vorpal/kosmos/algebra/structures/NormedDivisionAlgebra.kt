package org.vorpal.kosmos.algebra.structures

import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import kotlin.math.max
import kotlin.math.sqrt

/**
 * A normed division *-algebra with squared norm landing in N.
 *
 * Typical cases:
 * - N = Real for ℝ, ℂ, ℍ, 𝕆 (composition algebras)
 * - N = Rational for ℚ(i), quaternion algebras over ℚ, etc.
 */
interface NormedDivisionAlgebra<N : Any, A : Any> : NonAssociativeDivisionAlgebra<A> {
    val normSq: UnaryOp<A, N>

    companion object {
        fun <N : Any, A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>,
            reciprocal: Endo<A>,
            conj: Endo<A>,
            normSq: UnaryOp<A, N>
        ): NormedDivisionAlgebra<N, A> = object : NormedDivisionAlgebra<N, A> {
            override val zero: A = add.identity
            override val add = add
            override val mul = mul
            override val reciprocal = reciprocal
            override val conj = conj
            override val normSq = normSq
        }
    }
}

/**
 * A real normed division *-algebra: squared norm lands in ℝ, so we can define `norm` via sqrt.
 *
 * Canonical examples: ℝ, ℂ, ℍ, 𝕆.
 */
interface RealNormedDivisionAlgebra<A : Any> : NormedDivisionAlgebra<Real, A> {
    fun norm(a: A): Real =
        sqrt(max(0.0, normSq(a)))

    companion object {
        fun <A : Any> of(
            add: AbelianGroup<A>,
            mul: NonAssociativeMonoid<A>,
            reciprocal: Endo<A>,
            conj: Endo<A>,
            normSq: UnaryOp<A, Real>
        ): RealNormedDivisionAlgebra<A> = object : RealNormedDivisionAlgebra<A> {
            override val zero: A = add.identity
            override val add = add
            override val mul = mul
            override val reciprocal = reciprocal
            override val conj = conj
            override val normSq = normSq
        }
    }
}