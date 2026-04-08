package org.vorpal.kosmos.core.rational

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.instances.ArbInteger
import org.vorpal.kosmos.testing.ArbProduct2
import java.math.BigInteger

object ArbRational {
    /**
     * Arbitrary rational number generator with small numerator and nonzero denominator.
     */
    val small: Arb<Rational> =
        ArbProduct2.of(ArbInteger.small, ArbInteger.nonZeroSmall, Rational::of)

    /**
     * Arbitrary rational number generator for rational representations of integers.
     */
    val integer: Arb<Rational> =
        ArbInteger.small.map(Rational::of)

    /**
     * Arbitrary rational number generator for positive rational numbers.
     */
    val positive: Arb<Rational> =
        ArbProduct2.of(
            ArbInteger.positiveSmall,
            ArbInteger.positiveSmall,
            Rational::of
        )

    /**
     * Arbitrary rational number generator for negative rational numbers.
     */
    val negative: Arb<Rational> =
        positive.map { Rational.of(it.n.negate(), it.d) }

    /**
     * Arbitrary rational number generator for non-zero rational numbers.
     */
    val nonZero: Arb<Rational> =
        ArbProduct2.pair(ArbInteger.nonZeroSmall)
            .map { (n, d) -> Rational.of(n, d) }

    /**
     * Arbitrary rational number generator for rational numbers with large numerator and denominator.
     */
    val wide: Arb<Rational> =
        ArbProduct2.of(
            Arb.bigInt(64),
            Arb.bigInt(64).filter { it != BigInteger.ZERO },
            Rational::of
        )

    /**
     * Arbitrary nonzero rational number generator for rational numbers with large numerator and denominator.
     */
    val nonZeroWide: Arb<Rational> =
        wide.filter { it.n != BigInteger.ZERO }
}
