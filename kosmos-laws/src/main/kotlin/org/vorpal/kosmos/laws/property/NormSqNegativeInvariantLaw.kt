package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Negation invariance of squared norm:
 *
 *     N(-a) = N(a).
 */
class NormSqNegationInvariantLaw<A : Any, N : Any>(
    private val neg: Endo<A>,
    private val normSq: UnaryOp<A, N>,
    private val arbA: Arb<A>,
    private val eqN: Eq<N> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prN: Printable<N> = Printable.default()
) : TestingLaw {

    override val name: String = "normSq negation-invariant: N(-a)=N(a)"

    override suspend fun test() {
        checkAll(arbA) { a ->
            val negA = neg(a)
            val left = normSq(negA)
            val right = normSq(a)

            withClue(
                buildString {
                    appendLine("NormSq negation invariance failed:")
                    appendLine("a     = ${prA(a)}")
                    appendLine("-a    = ${prA(negA)}")
                    appendLine("N(-a) = ${prN(left)}")
                    appendLine("N(a)  = ${prN(right)}")
                }
            ) {
                check(eqN(left, right))
            }
        }
    }
}
