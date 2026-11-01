package org.vorpal.kosmos.hypercomplex.zorn

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import org.vorpal.kosmos.algebra.structures.instances.ArbReal
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.hypercomplex.splitquaternion.ArbSplitQuaternion
import org.vorpal.kosmos.linear.values.arbVec3
import java.math.BigInteger

object ArbZorn {

    /**
     * Generic Zorn vector-matrices built entrywise from an arbitrary base-ring generator.
     */
    fun <R : Any> zornOf(arbR: Arb<R>): Arb<Zorn<R>> {
        val arbV = arbVec3(arbR)
        return Arb.bind(arbR, arbV, arbV, arbR) { a, v, w, d -> Zorn(a, v, w, d) }
    }

    /**
     * Exact Zorn matrices over ℚ with small entries, for laws that hold with strict equality.
     */
    val rationalZorn: Arb<Zorn<Rational>> =
        zornOf(ArbSplitQuaternion.smallRational)

    /**
     * Exact Zorn matrices over ℚ with nonzero norm `ad − v·w`: exactly the units.
     */
    val unitRationalZorn: Arb<Zorn<Rational>> = run {
        val normSq = ZornAlgebras.normSq(RationalAlgebras.RationalField)
        rationalZorn.filter { x -> normSq(x) != Rational.ZERO }
    }

    /**
     * Zorn matrices over ℤ with small entries.
     */
    val integerZorn: Arb<Zorn<BigInteger>> =
        zornOf(Arb.int(-20..20).map(Int::toBigInteger))

    /**
     * Smallish real Zorn matrices.
     */
    val smallZorn: Arb<Zorn<Real>> =
        zornOf(ArbReal.smallReal)

    /**
     * Real Zorn matrices safely off the null cone: `|N(X)|` in `[0.01, 10000]`.
     */
    val unitZorn: Arb<Zorn<Real>> =
        smallZorn.filter { x ->
            kotlin.math.abs(ZornAlgebras.RealZornAlgebra.normSq(x)) in 0.01..10_000.0
        }
}
