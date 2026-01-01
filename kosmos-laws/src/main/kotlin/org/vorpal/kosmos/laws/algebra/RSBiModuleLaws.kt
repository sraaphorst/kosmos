package org.vorpal.kosmos.laws.algebra

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.structures.RSBiModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.module.bimoduleCompatibilityLaw
import org.vorpal.kosmos.laws.module.leftRModuleLaws
import org.vorpal.kosmos.laws.module.rightRModuleLaws
import org.vorpal.kosmos.laws.suiteName

/**
 * [RSBiModule] laws (for `M`, a left module over a ring `R` and a right module over a ring `S`):
 * - [RingLaws] (full)
 * - [AbelianGroupLaws] (full)
 *
 *
 *     r ⊳ (x + y) = r ⊳ x + r ⊳ y
 *     (r + s) ⊳ x = r ⊳ x + s ⊳ x
 *     (r * s) ⊳ x = r ⊳ (s ⊳ x)
 *     1_R ⊳ x = x
 *
 *     (x + y) ⊲ s = x ⊲ s + y ⊲ s
 *     x ⊲ (r + s) = x ⊲ r + x ⊲ s
 *     x ⊲ (r * s) = (x ⊲ r) ⊲ s
 *     x ⊲ 1_S = x
 *
 *     (r ⊳ m) ⊲ s = r ⊳ (m ⊲ s)
 */
class RSBiModuleLaws<R : Any, M : Any, S : Any>(
    module: RSBiModule<R, M, S>,
    leftScalarArb: Arb<R>,
    vectorArb: Arb<M>,
    rightScalarArb: Arb<S>,
    eqR: Eq<R> = Eq.default(),
    eqM: Eq<M> = Eq.default(),
    eqS: Eq<S> = Eq.default(),
    prR: Printable<R> = Printable.default(),
    prM: Printable<M> = Printable.default(),
    prS: Printable<S> = Printable.default()
): LawSuite {
    private val leftScalarDescription = "R[${module.leftScalars.add.op.symbol}${module.leftScalars.mul.op.symbol}]"
    private val moduleDescription = "M[${module.group.op.symbol}]"
    private val rightScalarDescription = "S[${module.rightScalars.add.op.symbol}${module.rightScalars.mul.op.symbol}]"
    override val name = suiteName("RSBiModule",
        leftScalarDescription,
        module.leftAction.symbol,
        moduleDescription,
        module.rightAction.symbol,
        rightScalarDescription
    )

    // We redefine these here since doing full law testing using LeftRModuleLaws and RightRModuleLaws would test
    // the group twice, which is undesirable.
    private val leftRingLaws: RingLaws<R> by lazy {
        RingLaws(module.leftScalars, leftScalarArb, eqR, prR)
    }
    private val rightRingLaws: RingLaws<S> by lazy {
        RingLaws(module.rightScalars, rightScalarArb, eqS, prS)
    }
    private val groupLaws: AbelianGroupLaws<M> by lazy {
        AbelianGroupLaws(module.group, vectorArb, eqM, prM)
    }

    private val structureLaws: List<TestingLaw> =
        leftRModuleLaws(module, leftScalarArb, vectorArb, eqM, prR, prM) +
            rightRModuleLaws(module, vectorArb, rightScalarArb, eqM, prM, prS) +
            listOf(
                bimoduleCompatibilityLaw(
                    leftAct = module.leftAction,
                    rightAct = module.rightAction,
                    arbR = leftScalarArb,
                    arbM = vectorArb,
                    arbS = rightScalarArb,
                    eqM = eqM,
                    prR = prR,
                    prM = prM,
                    prS = prS
                )
            )


    override fun laws(): List<TestingLaw> =
        structureLaws

    override fun fullLaws(): List<TestingLaw> =
        leftRingLaws.fullLaws() +
            rightRingLaws.fullLaws() +
            groupLaws.fullLaws() +
            structureLaws
}
