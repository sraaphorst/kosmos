package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.RealTolerances
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.homomorphism.preservesBinaryOpLaw
import org.vorpal.kosmos.laws.property.realPositiveDefiniteLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * [NormedDivisionAlgebra] laws:
 * - [NonAssociativeDivisionAlgebraLaws]
 * - PositiveDefiniteLaw via [realPositiveDefiniteLaw]
 * - [preservesBinaryOpLaw]
 * - Check that norm and normSq are consistent
 */
class NormedDivisionAlgebraLaws<A : Any>(
    algebra: NormedDivisionAlgebra<A>,
    arb: Arb<A>,
    eqA: Eq<A> = Eq.default(),
    tolerance: Real = RealTolerances.DEFAULT,
    eqReal: Eq<Real> = Eqs.realApprox(absTol = tolerance, relTol = tolerance),
    prA: Printable<A> = Printable.default(),
    prReal: Printable<Real> = Printable.default()
): LawSuite {

    private val normDesc = "N[${algebra.normSq.symbol}]"

    override val name = suiteName(
        "NormedDivisionAlgebra",
        algebra.add.op.symbol,
        algebra.mul.op.symbol,
        algebra.conj.symbol,
        algebra.reciprocal.symbol,
        normDesc
    )

    private val divAlgLaws = NonAssociativeDivisionAlgebraLaws(algebra, arb, eqA, prA)

    private val structureLaws: List<TestingLaw> = listOf(
        // Ensure positive definite laws:
        // N(a) >= 0
        // N(a) = 0 ⇔ a = 0
        realPositiveDefiniteLaw(
            inner = algebra.normSq,
            zeroVector = algebra.add.identity,
            vectorArb = arb,
            vectorEq = eqA,
            vectorPr = prA,
            tolerance = tolerance
        ),

        // Ensure N(ab) = N(a)N(b)
        preservesBinaryOpLaw(
            domainOp = algebra.mul.op,
            codomainOp = RealField.mul.op,
            hom = algebra.normSq::invoke,
            arbA = arb,
            eqB = eqReal,
            prA = prA,
            prB = prReal
        ),

        // Ensure norm matches normSq. This should happen by definition, but check in case user overrides.
         TestingLaw.named("norm consistency: N(a) = ||a||^2") {
             checkAll(arb) { a ->
                 val left = algebra.normSq(a)
                 val n = algebra.norm(a)
                 val right = RealField.mul.op(n, n)
                 check(eqReal(left, right)) {
                     "Expected N(a) = ||a||^2, got N(a)=$left, ||a||^2=$right for a=${prA(a)}"
                 }
             }
         }
    )

    override fun laws(): List<TestingLaw> =
        divAlgLaws.laws() + structureLaws

    override fun fullLaws(): List<TestingLaw> =
        divAlgLaws.fullLaws() + structureLaws
}
