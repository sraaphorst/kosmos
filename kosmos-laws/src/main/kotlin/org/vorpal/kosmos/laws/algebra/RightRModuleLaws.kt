package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.RightRModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.module.rightRModuleLaws
import org.vorpal.kosmos.laws.suiteName

/**
 * [RightRModule] laws (for a right module `M` over a ring `S`):
 * - [RingLaws] (full)
 * - [AbelianGroupLaws] (full)
 *
 *
 *     (x + y) ⊲ s = x ⊲ s + y ⊲ s
 *     x ⊲ (r + s) = x ⊲ r + x ⊲ s
 *     x ⊲ (r * s) = (x ⊲ r) ⊲ s
 *     x ⊲ 1_S = x
 */
class RightRModuleLaws<M : Any, S : Any>(
    module: RightRModule<M, S>,
    vectorArb: Arb<M>,
    scalarArb: Arb<S>,
    eqM: Eq<M> = Eq.default(),
    eqS: Eq<S> = Eq.default(),
    prM: Printable<M> = Printable.default(),
    prS: Printable<S> = Printable.default()
): LawSuite {
    private val moduleDescription = "M[${module.add.op.symbol}]"
    private val rightScalarDescription = "S[${module.rightScalars.add.op.symbol}${module.rightScalars.mul.op.symbol}]"
    override val name = suiteName(
        "RightRModule",
        moduleDescription,
        module.rightAction.symbol,
        rightScalarDescription
    )

    private val ringLaws: RingLaws<S> by lazy {
        RingLaws(module.rightScalars, scalarArb, eqS, prS)
    }
    private val groupLaws: AbelianGroupLaws<M> by lazy {
        AbelianGroupLaws(module.add, vectorArb, eqM, prM)
    }
    private val structureLaws: List<TestingLaw> =
        rightRModuleLaws(module, vectorArb, scalarArb, eqM, prM, prS)

    override fun laws(): List<TestingLaw> =
        structureLaws

    override fun fullLaws(): List<TestingLaw> =
        ringLaws.fullLaws() +
            groupLaws.fullLaws() +
            structureLaws
}
