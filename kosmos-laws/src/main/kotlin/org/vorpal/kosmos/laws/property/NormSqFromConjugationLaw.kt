package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Compatibility of squared norm with conjugation:
 * ```
 * embed(N(a)) = a * conj(a).
 * ```
 * This handles the common case where [normSq] lands in a scalar codomain
 * that embeds canonically into the carrier.
 */
class NormSqFromConjugationLaw<A : Any, N : Any>(
    private val normSq: UnaryOp<A, N>,
    private val embed: UnaryOp<N, A>,
    private val mulA: BinOp<A>,
    private val conj: Endo<A>,
    private val arbA: Arb<A>,
    private val eqA: Eq<A> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prN: Printable<N> = Printable.default()
) : TestingLaw {

    override val name: String = "normSq from conjugation: embed(N(a)) = a*conj(a)"

    override suspend fun test() {
        checkAll(arbA) { a ->
            val left = embed(normSq(a))
            val right = mulA(a, conj(a))

            withClue(
                buildString {
                    appendLine("NormSq/conjugation compatibility failed:")
                    appendLine("a           = ${prA(a)}")
                    appendLine("N(a)        = ${prN(normSq(a))}")
                    appendLine("embed(N(a)) = ${prA(left)}")
                    appendLine("a*conj(a)   = ${prA(right)}")
                }
            ) {
                check(eqA(left, right))
            }
        }
    }
}
