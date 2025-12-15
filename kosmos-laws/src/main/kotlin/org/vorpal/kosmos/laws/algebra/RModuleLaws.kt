package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.LawSuite
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.module.actionAssociatesWithScalarMultiplicationLaw
import org.vorpal.kosmos.laws.module.actionDistributesOverAdditionLaw
import org.vorpal.kosmos.laws.module.additionDistributesOverActionLaw
import org.vorpal.kosmos.laws.module.unitActsAsIdentityLaw
import org.vorpal.kosmos.laws.suiteName

/**
 * Laws for an R-module M over a commutative ring R.
 *
 * We assume:
 *  - `module.ring` is a commutative ring (tested separately via CommutativeRingLaws if desired)
 *  - `module.group` is an abelian group (tested separately via AbelianGroupLaws)
 *
 * Here we only check the *interaction* axioms:
 *
 *  1. r · (x + y) = r · x + r · y
 *  2. (r + s) · x = r · x + s · x
 *  3. (r * s) · x = r · (s · x)
 *  4. 1_R · x = x
 */
class RModuleLaws<R : Any, M : Any>(
    private val module: RModule<R, M>,
    private val scalarArb: Arb<R>,
    private val vectorArb: Arb<M>,
    private val eqM: Eq<M> = Eq.default(),
    private val prR: Printable<R> = Printable.default(),
    private val prM: Printable<M> = Printable.default(),
) : LawSuite {

    override val name = suiteName(
        "RModule",
        module.scalars.add.op.symbol,
        module.scalars.mul.op.symbol,
        module.action.symbol,
        module.group.op.symbol
    )

    override fun laws(): List<TestingLaw> =
        listOf(
            actionDistributesOverAdditionLaw(
                addM = module.group.op,
                act = module.action,
                arbR = scalarArb,
                arbM = vectorArb,
                eqM = eqM,
                prR = prR,
                prM = prM
            ),
            additionDistributesOverActionLaw(
                addR = module.scalars.add.op,
                addM = module.group.op,
                act = module.action,
                arbR = scalarArb,
                arbM = vectorArb,
                eqM = eqM,
                prR = prR,
                prM = prM
            ),
            actionAssociatesWithScalarMultiplicationLaw(
                mulR = module.scalars.mul.op,
                act = module.action,
                arbR = scalarArb,
                arbM = vectorArb,
                eqM = eqM,
                prR = prR,
                prM = prM
            ),
            unitActsAsIdentityLaw(
                oneR = module.scalars.mul.identity,
                act = module.action,
                arbM = vectorArb,
                eqM = eqM,
                prR = prR,
                prM = prM
            )
        )
}