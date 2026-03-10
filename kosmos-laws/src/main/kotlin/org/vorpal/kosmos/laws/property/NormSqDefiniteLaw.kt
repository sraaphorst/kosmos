package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Definiteness of a squared norm:
 *
 *     N(a) = 0 ⇔ a = 0.
 */
class NormSqDefiniteLaw<A : Any, N : Any>(
    private val normSq: UnaryOp<A, N>,
    private val zeroA: A,
    private val zeroN: N,
    private val arbA: Arb<A>,
    private val eqA: Eq<A> = Eq.default(),
    private val eqN: Eq<N> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prN: Printable<N> = Printable.default()
) : TestingLaw {

    override val name: String = "normSq definite: N(a)=0 ⇔ a=0"

    override suspend fun test() {
        checkAll(arbA) { a ->
            val n = normSq(a)
            val isNormZero = eqN(n, zeroN)
            val isVecZero = eqA(a, zeroA)

            withClue(
                buildString {
                    appendLine("NormSq definiteness failed:")
                    appendLine("a          = ${prA(a)}")
                    appendLine("N(a)       = ${prN(n)}")
                    appendLine("zeroA?     = $isVecZero")
                    appendLine("N(a)=zero? = $isNormZero")
                }
            ) {
                check(isNormZero == isVecZero)
            }
        }
    }
}
