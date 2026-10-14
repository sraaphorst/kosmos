package org.vorpal.kosmos.hypercomplex.quaternion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.choice
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.core.rational.Rational
import java.math.BigInteger

object ArbHurwitzQuaternion {

    private val two: BigInteger = BigInteger.TWO

    /**
     * The half-integer `(2n + 1)/2`.
     */
    private fun halfOdd(n: BigInteger): Rational =
        Rational.of(two * n + BigInteger.ONE, two)

    /**
     * Hurwitz quaternions in the Lipschitz coset: all coordinates in `ℤ`.
     */
    val integralHurwitzQuaternion: Arb<HurwitzQuaternion> =
        ArbLipschitzQuaternion.lipschitzQuaternion.map { lq ->
            LipschitzQuaternionAlgebras.LipschitzToHurwitzQuaternionMonomorphism(lq)
        }

    /**
     * Hurwitz quaternions in the half-integer coset: all coordinates in `ℤ + 1/2`.
     */
    val halfIntegralHurwitzQuaternion: Arb<HurwitzQuaternion> =
        Arb.bind(
            ArbInteger.small,
            ArbInteger.small,
            ArbInteger.small,
            ArbInteger.small
        ) { a, b, c, d ->
            HurwitzQuaternion(halfOdd(a), halfOdd(b), halfOdd(c), halfOdd(d))
        }

    /**
     * Hurwitz quaternions drawn evenly from both cosets of `ℤ⟨i, j, k⟩` in `𝓗`.
     */
    val hurwitzQuaternion: Arb<HurwitzQuaternion> =
        Arb.choice(integralHurwitzQuaternion, halfIntegralHurwitzQuaternion)

    /**
     * The 24 units of `𝓗`.
     */
    val hurwitzUnit: Arb<HurwitzQuaternion> =
        Arb.element(HurwitzQuaternionAlgebras.HurwitzQuaternionRing.units)

    /**
     * Rational quaternions that are *not* in `𝓗`: at least one coordinate has a denominator
     * other than 1 or 2, or the coordinates mix integers and half-integers.
     */
    val nonHurwitzRationalQuaternion: Arb<RationalQuaternion> =
        ArbRationalQuaternion.rationalQuaternion.filter { q -> !HurwitzQuaternion.isHurwitz(q) }
}
