package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for a Boolean-style complementation operator ¬ on a bounded lattice:
 *
 *  - Double negation:     `¬(¬x) = x`
 *  - Meet with complement: `x ∧ ¬x = ⊥`
 *  - Join with complement: `x ∨ ¬x = ⊤`
 *
 * This is intended for Boolean algebras, but is generic over:
 *  - meet:      `∧`
 *  - join:      `∨`
 *  - bottom:    `⊥`
 *  - top:       `⊤`
 *  - complement: `¬`
 */
class ComplementationLaw<A : Any>(
    private val meet: BinOp<A>,
    private val join: BinOp<A>,
    private val bottom: A,
    private val top: A,
    private val complement: Endo<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {
    private val notSym = complement.symbol
    override val name: String =
        "Boolean complementation ($notSym, ${meet.symbol}, ${join.symbol})"

    /**
     * Not consistent with the other laws to have them all in here, but they are all fairly short,
     * so this format makes sense.
     */
    override suspend fun test() {
        checkAll(arb) { x ->
            val nx = complement(x)
            val nnx = complement(nx)

            val xAndNotX = meet(x, nx)
            val xOrNotX = join(x, nx)

            withClue("Double negation failed for $notSym, element ${pr(x)}") {
                check(eq(nnx, x))
            }

            withClue("x ${meet.symbol} $notSym x = ⊥ failed for element ${pr(x)}") {
                check(eq(xAndNotX, bottom))
            }

            withClue("x ${join.symbol} $notSym x = ⊤ failed for element ${pr(x)}") {
                check(eq(xOrNotX, top))
            }
        }
    }
}
