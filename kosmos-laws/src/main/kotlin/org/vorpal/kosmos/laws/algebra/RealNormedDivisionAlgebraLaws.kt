package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.RealNormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.RealTolerances
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.realPositiveDefiniteLaw
import org.vorpal.kosmos.laws.suiteName
import kotlin.math.max

/**
 * Laws for [RealNormedDivisionAlgebra]:
 * - Reuses [NormedDivisionAlgebraLaws] with N = Real (multiplicativity + underlying division algebra laws)
 * - Positive-definiteness of normSq (Real-only)
 * - Consistency of norm with normSq: ||a||^2 = max(0, N(a))
 */
class RealNormedDivisionAlgebraLaws<A : Any>(
    private val algebra: RealNormedDivisionAlgebra<A>,
    private val arb: Arb<A>,
    private val eqA: Eq<A> = Eq.default(),
    tolerance: Real = RealTolerances.DEFAULT,
    private val eqReal: Eq<Real> = Eqs.realApprox(absTol = tolerance, relTol = tolerance),
    private val prA: Printable<A> = Printable.default(),
    private val prReal: Printable<Real> = Printable.default()
) : LawSuite {

    private val normDesc = "N[${algebra.normSq.symbol}]"

    override val name = suiteName(
        "RealNormedDivisionAlgebra",
        algebra.add.op.symbol,
        algebra.mul.op.symbol,
        algebra.conj.symbol,
        algebra.reciprocal.symbol,
        normDesc
    )

    // Reuse the generic laws:
    // - NonAssociativeDivisionAlgebraLaws
    // - N(xy) = N(x)N(y)
    //
    // Do NOT add generic “definiteness” here; realPositiveDefiniteLaw already checks it with tolerances.
    private val base: NormedDivisionAlgebraLaws<Real, A> =
        NormedDivisionAlgebraLaws(
            algebra = algebra,
            arbA = arb,
            mulN = RealField.mul.op,
            eqA = eqA,
            eqN = eqReal,
            prA = prA,
            prN = prReal,
            zeroN = null
        )

    private val realOnly: List<TestingLaw> = listOf(
        realPositiveDefiniteLaw(
            inner = algebra.normSq,
            zeroVector = algebra.zero,
            vectorArb = arb,
            vectorEq = eqA,
            vectorPr = prA,
            tolerance = tolerance
        ),

        TestingLaw.named("norm consistency: ||a||^2 = max(0, N(a))") {
            checkAll(arb) { a ->
                val nSq = algebra.normSq(a)
                val n = algebra.norm(a)
                val computed = RealField.mul.op(n, n)
                val expected = max(0.0, nSq)

                check(eqReal(computed, expected)) {
                    "Expected ||a||^2 = max(0, N(a)). " +
                        "N(a)=${prReal(nSq)}, ||a||^2=${prReal(computed)}, expected=${prReal(expected)} " +
                        "for a=${prA(a)}"
                }
            }
        }
    )

    override fun laws(): List<TestingLaw> =
        base.laws() + realOnly

    override fun fullLaws(): List<TestingLaw> =
        base.fullLaws() + realOnly
}