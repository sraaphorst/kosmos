package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.instances.Real
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.NormLaw

class NormedDivisionAlgebraLaws<A : Any>(
    private val algebra: NormedDivisionAlgebra<A>,
    private val arb: Arb<A>,
    private val eqA: Eq<A>,
    private val eqReal: Eq<Real>,
    private val prA: Printable<A> = default(),
    private val symbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> {
        val zero = algebra.zero

        val baseDivisionLaws =
            NonAssociativeDivisionAlgebraLaws(
                algebra = algebra,
                arb = arb,
                eq = eqA,
                pr = prA,
                mulSymbol = symbol
            ).laws()

        val normHomLaws =
            MonoidHomLaws(
                domain = algebra.mul as Monoid<A>,
                codomain = RealAlgebras.RealField.mul,
                f = algebra::normSq,
                pairArb = Arb.pair(arb, arb),
                eqB = eqReal,
                prA = prA,
            ).laws()

        val normLaws = listOf(
            NormLaw(
                normSq = algebra::normSq,
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