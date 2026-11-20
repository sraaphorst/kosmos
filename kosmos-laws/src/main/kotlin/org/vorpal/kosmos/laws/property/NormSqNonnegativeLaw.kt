package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

/**
 * Non-negativity of a squared norm:
 *
 *     N(a) ≥ 0.
 *
 * The caller supplies the notion of "non-negative".
 *
 * Later, we can add convenience factories for:
 * - order + zero
 * - real tolerance
 */
class NormSqNonnegativeLaw<A : Any, N : Any>(
    private val normSq: UnaryOp<A, N>,
    private val arbA: Arb<A>,
    private val isNonnegative: (N) -> Boolean,
    private val prA: Printable<A> = Printable.default(),
    private val prN: Printable<N> = Printable.default()
) : TestingLaw {

    override val name: String = "normSq nonnegative: N(a) ≥ 0"

    override suspend fun test() {
        lawCheckAll(arbA, render = renderWith(prA)) { a ->
            val n = normSq(a)

            lawCheck(isNonnegative(n)) {
                buildString {
                    appendLine("NormSq non-negativity failed:")
                    appendLine("a    = ${prA(a)}")
                    appendLine("N(a) = ${prN(n)}")
                }
            }
        }
    }
}
