package org.vorpal.kosmos.hypercomplex.multi

import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.hypercomplex.complex.Complex
import org.vorpal.kosmos.hypercomplex.complex.ComplexAlgebras
import org.vorpal.kosmos.hypercomplex.complex.complex
import org.vorpal.kosmos.hypercomplex.complex.im
import org.vorpal.kosmos.hypercomplex.complex.re

@ConsistentCopyVisibility
data class Bicomplex private constructor(val alpha: Complex, val beta: Complex) {
    /**
     * Return the idempotent Complex components of the bicomplex number.
     */
    fun idempotent(): Pair<Complex, Complex> =
        alpha to beta

    /**
     * Return the standard Complex components of the bicomplex number.
     */
    fun standard(): Pair<Complex, Complex> =
        toZ1(alpha, beta) to toZ2(alpha, beta)

    /**
     * Returns the standard real coefficients `[a,b,c,d]`:
     * ```kotlin
     * z = (a + bi) + (c + di)j
     * ```
     */
    fun coefficients(): List<Real> {
        val (s1, s2) = standard()
        return listOf(s1.re, s1.im, s2.re, s2.im)
    }

    /**
     * A bicomplex number is a zero divisor iff at least one idempotent component is zero.
     */
    fun isZeroDivisor(): Boolean =
        (alpha == ComplexAlgebras.ComplexField.zero) || (beta == ComplexAlgebras.ComplexField.zero)

    /**
     * A bicomplex number is a unit (i.e. invertible, has a reciprocal) if it is not a zero divisor.
     */
    fun isUnit(): Boolean = !isZeroDivisor()

    val z1: Complex get() = toZ1(alpha, beta)
    val z2: Complex get() = toZ2(alpha, beta)
    val a: Real get() = z1.re
    val b: Real get() = z1.im
    val c: Real get() = z2.re
    val d: Real get() = z2.im

    companion object {
        private fun toAlpha(z1: Complex, z2: Complex): Complex =
            complex(z1.re - z2.im, z1.im + z2.re)
        private fun toBeta(z1: Complex, z2: Complex): Complex =
            complex(z1.re + z2.im, z1.im - z2.re)
        private fun toZ1(alpha: Complex, beta: Complex): Complex =
            complex((alpha.re + beta.re) / 2, (alpha.im + beta.im) / 2)
        private fun toZ2(alpha: Complex, beta: Complex): Complex =
            complex((alpha.im - beta.im) / 2, (beta.re - alpha.re) / 2)

        fun ofStandard(z1: Complex, z2: Complex) =
            ofIdempotent(toAlpha(z1, z2), toBeta(z1, z2))

        fun ofStandard(a: Real, b: Real, c: Real, d: Real) =
            ofStandard(complex(a, b), complex(c, d))

        fun ofIdempotent(alpha: Complex, beta: Complex) =
            Bicomplex(alpha, beta)

        val ZERO = ofIdempotent(ComplexAlgebras.ComplexField.zero, ComplexAlgebras.ComplexField.zero)
        val ONE = ofIdempotent(ComplexAlgebras.ComplexField.one, ComplexAlgebras.ComplexField.one)
    }
}
