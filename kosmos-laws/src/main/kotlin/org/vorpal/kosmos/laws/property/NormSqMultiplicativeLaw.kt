package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Multiplicativity of squared norm:
 *
 *     N(x * y) = N(x) * N(y).
 */
class NormSqMultiplicativeLaw<A : Any, N : Any>(
    private val mulA: BinOp<A>,
    private val normSq: UnaryOp<A, N>,
    private val mulN: BinOp<N>,
    private val arbA: Arb<A>,
    private val eqN: Eq<N> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prN: Printable<N> = Printable.default()
) : TestingLaw {

    override val name: String = "normSq multiplicative: N(xy)=N(x)N(y)"

    override suspend fun test() {
        checkAll(arbA, arbA) { x, y ->
            val xy = mulA(x, y)
            val left = normSq(xy)
            val right = mulN(normSq(x), normSq(y))

            withClue(
                buildString {
                    appendLine("NormSq multiplicativity failed:")
                    appendLine("x        = ${prA(x)}")
                    appendLine("y        = ${prA(y)}")
                    appendLine("x*y      = ${prA(xy)}")
                    appendLine("N(x*y)   = ${prN(left)}")
                    appendLine("N(x)N(y) = ${prN(right)}")
                }
            ) {
                check(eqN(left, right))
            }
        }
    }
}
