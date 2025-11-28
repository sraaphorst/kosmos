package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for a Boolean-style complementation operator ¬ on a bounded lattice:
 *
 *  - Double negation:     ¬(¬x) = x
 *  - Meet with complement: x ∧ ¬x = ⊥
 *  - Join with complement: x ∨ ¬x = ⊤
 *
 * This is intended for Boolean algebras, but is generic over:
 *  - meet:      ∧
 *  - join:      ∨
 *  - bottom:    ⊥
 *  - top:       ⊤
 *  - complement: ¬
 */
class ComplementationLaw<A : Any>(
    private val meet: BinOp<A>,
    private val join: BinOp<A>,
    private val bottom: A,
    private val top: A,
    private val complement: Endo<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val meetSymbol: String = "∧",
    private val joinSymbol: String = "∨",
    private val notSymbol: String = "¬"
) : TestingLaw {

    override val name: String =
        "boolean complementation ($notSymbol, $meetSymbol, $joinSymbol)"

    override suspend fun test() {
        checkAll(arb) { x ->
            val nx = complement(x)
            val nnx = complement(nx)

            val xAndNotX = meet(x, nx)
            val xOrNotX = join(x, nx)

            withClue("Double negation failed for $notSymbol, element ${pr.render(x)}") {
                check(eq.eqv(nnx, x))
            }

            withClue("x $meetSymbol $notSymbol x = ⊥ failed for element ${pr.render(x)}") {
                check(eq.eqv(xAndNotX, bottom))
            }

            withClue("x $joinSymbol $notSymbol x = ⊤ failed for element ${pr.render(x)}") {
                check(eq.eqv(xOrNotX, top))
            }
        }
    }
}