package org.vorpal.kosmos.hypercomplex.sextonion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.linear.values.arbMat2
import org.vorpal.kosmos.linear.values.arbVec2
import java.math.BigInteger

/**
 * Arbitraries for [Sextonion]s.
 */
object ArbSextonion {

    /**
     * Sextonions built entrywise from an arbitrary base-ring generator: a random matrix part and
     * a random vector part.
     */
    fun <R : Any> sextonionOf(arbR: Arb<R>): Arb<Sextonion<R>> =
        Arb.bind(arbMat2(arbR), arbVec2(arbR)) { m, nu -> Sextonion(m, nu) }

    /**
     * Exact sextonions over ℚ with small entries, for laws that hold with strict equality.
     */
    val rationalSextonion: Arb<Sextonion<Rational>> =
        sextonionOf(ArbSplitQuaternion.smallRational)

    /**
     * Exact sextonions over ℚ with nonzero norm `det A`: exactly the units.
     */
    val unitRationalSextonion: Arb<Sextonion<Rational>> = run {
        val normSq = SextonionAlgebras.normSq(RationalAlgebras.RationalField)
        rationalSextonion.filter { x -> normSq(x) != Rational.ZERO }
    }

    /**
     * Sextonions over ℤ with small entries.
     */
    val integerSextonion: Arb<Sextonion<BigInteger>> =
        sextonionOf(Arb.int(-20..20).map(Int::toBigInteger))
}
