package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.BooleanAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.ComplementationLaw

/**
 * Laws for Boolean algebras:
 *
 *  - Underlying distributive bounded lattice laws
 *  - Complementation:
 *      ¬(¬x) = x
 *      x ∧ ¬x = ⊥
 *      x ∨ ¬x = ⊤
 */
class BooleanAlgebraLaws<A : Any>(
    private val booleanAlgebra: BooleanAlgebra<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val meetSymbol: String = "∧",
    private val joinSymbol: String = "∨",
    private val notSymbol: String = "¬"
) {

    fun laws(): List<TestingLaw> {
        val latticeLaws =
            DistributiveLatticeLaws(
                lattice = booleanAlgebra,
                arb = arb,
                eq = eq,
                pr = pr,
                meetSymbol = meetSymbol,
                joinSymbol = joinSymbol
            ).laws()

        val complementLaw =
            ComplementationLaw(
                meet = booleanAlgebra.meet,
                join = booleanAlgebra.join,
                bottom = booleanAlgebra.bottom,
                top = booleanAlgebra.top,
                complement = booleanAlgebra.not,
                arb = arb,
                eq = eq,
                pr = pr,
                meetSymbol = meetSymbol,
                joinSymbol = joinSymbol,
                notSymbol = notSymbol
            )

        return latticeLaws + complementLaw
    }
}