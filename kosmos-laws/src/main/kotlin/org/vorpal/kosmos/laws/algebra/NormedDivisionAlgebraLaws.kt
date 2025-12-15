package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.Real
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.homomorphism.UnitalHomomorphismLaws
import org.vorpal.kosmos.laws.property.NormLaw

class NormedDivisionAlgebraLaws<A : Any>(
    private val algebra: NormedDivisionAlgebra<A>,
    private val arb: Arb<A>,
    private val eqA: Eq<A> = Eq.default(),
    private val eqReal: Eq<Real> = Eqs.realApprox(),
    private val prA: Printable<A> = Printable.default(),
): LawSuite {
    override val name = "NormedDivisionAlgebra"

    override fun laws(): List<TestingLaw> {
        val zero = algebra.zero

        val baseDivisionLaws = NonAssociativeDivisionAlgebraLaws(
            algebra = algebra,
            arb = arb,
            eq = eqA,
            pr = prA
        ).laws()

        val normHomLaws = UnitalHomomorphismLaws(
            domain = algebra.mul,
            codomain = RealAlgebras.RealField.mul,
            hom = { algebra.normSq(it) },
            arb = arb,
            eqB = eqReal,
            prA = prA
        ).laws()

        val normLaws = listOf(
            NormLaw(
                normSq = algebra.normSq,
                zero = zero,
                arb = arb,
                eqReal = eqReal,
                eqA = eqA,
                prA = prA
            )
        )

        return baseDivisionLaws + normHomLaws + normLaws
    }
}
