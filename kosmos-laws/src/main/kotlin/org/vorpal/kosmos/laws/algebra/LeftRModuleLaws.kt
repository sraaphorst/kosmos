package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.LeftRModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.module.leftRModuleLaws
import org.vorpal.kosmos.laws.suiteName

/**
 * [LeftRModule] laws (for a left module `M` over a ring `R`):
 * - [RingLaws] (full)
 * - [AbelianGroupLaws] (full)
 *
 *
 *     r ⊳ (x + y) = r ⊳ x + r ⊳ y
 *     (r + s) ⊳ x = r ⊳ x + s ⊳ x
 *     (r * s) ⊳ x = r ⊳ (s ⊳ x)
 *     1_R ⊳ x = x
 */
class LeftRModuleLaws<R : Any, M : Any>(
    module: LeftRModule<R, M>,
    scalarArb: Arb<R>,
    vectorArb: Arb<M>,
    eqR: Eq<R> = Eq.default(),
    eqM: Eq<M> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
) : LawSuite {
    private val leftScalarDescription = "R[${module.leftScalars.add.op.symbol}${module.leftScalars.mul.op.symbol}]"
    private val moduleDescription = "M[${module.group.op.symbol}]"
    override val name = suiteName(
        "LeftRModule",
        leftScalarDescription,
        module.leftAction.symbol,
        moduleDescription
    )

    private val ringLaws: RingLaws<R> by lazy {
        RingLaws(module.leftScalars, scalarArb, eqR, prR)
    }
    private val groupLaws: AbelianGroupLaws<M> by lazy {
        AbelianGroupLaws(module.group, vectorArb, eqM, prM)
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
