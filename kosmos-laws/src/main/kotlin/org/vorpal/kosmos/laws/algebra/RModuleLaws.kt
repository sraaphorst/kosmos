package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.module.leftRModuleLaws
import org.vorpal.kosmos.laws.suiteName

/**
 * [RModule] laws (for a left module `M` over a commutative ring `R`):
 * - [CommutativeRingLaws] (full)
 * - [AbelianGroupLaws] (full)
 *
 *
 *     r ⊳ (x + y) = r ⊳ x + r ⊳ y
 *     (r + s) ⊳ x = r ⊳ x + s ⊳ x
 *     (r * s) ⊳ x = r ⊳ (s ⊳ x)
 *     1_R ⊳ x = x
 */
class RModuleLaws<R : Any, M : Any>(
    module: RModule<R, M>,
    scalarArb: Arb<R>,
    vectorArb: Arb<M>,
    eqR: Eq<R> = Eq.default(),
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default()
): LawSuite {
    private val leftScalarDescription = "R[${module.leftScalars.add.op.symbol}${module.leftScalars.mul.op.symbol}]"
    private val moduleDescription = "M[${module.add.op.symbol}]"
    override val name = suiteName(
        "RModule",
        leftScalarDescription,
        module.leftAction.symbol,
        moduleDescription
    )

    private val ringLaws: CommutativeRingLaws<R> by lazy {
        CommutativeRingLaws(module.scalars, scalarArb, eqR, prR)
    }
    private val groupLaws: AbelianGroupLaws<M> by lazy {
        AbelianGroupLaws(module.add, vectorArb, eqM, prM)
    }
    private val structureLaws: List<TestingLaw> =
        leftRModuleLaws(module, scalarArb, vectorArb, eqM, prR, prM)

    override fun laws(): List<TestingLaw> =
        structureLaws

    override fun fullLaws(): List<TestingLaw> =
        ringLaws.fullLaws() +
            groupLaws.fullLaws() +
            structureLaws

}
