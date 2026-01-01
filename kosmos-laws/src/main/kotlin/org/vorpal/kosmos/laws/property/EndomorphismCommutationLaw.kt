package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Property: two endomorphisms commute on a given domain:
 *
 *   f(g(a)) = g(f(a))  for all a with inDomain(a) = true.
 */
class EndomorphismCommutationLaw<A : Any>(
    private val f: Endo<A>,
    private val g: Endo<A>,
    private val arb: Arb<A>,
    private val inDomain: (A) -> Boolean = { true },
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {

    private val fSymbol: String = f.symbol
    private val gSymbol: String = g.symbol

    override val name: String =
        "endomorphisms commute: $fSymbol ∘ $gSymbol = $gSymbol ∘ $fSymbol"

    private suspend fun endomorphismCommutationCheck() {
        checkAll(arb.filter(inDomain)) { a ->
            val fg = f(g(a))
            val gf = g(f(a))

            withClue(endomorphismCommutationFailure(a, fg, gf)) {
                check(eq(fg, gf))
            }
        }
    }

    private fun endomorphismCommutationFailure(a: A, fg: A, gf: A): () -> String = {
        buildString {
            val sa = pr(a)
            val sfg = pr(fg)
            val sgf = pr(gf)

            appendLine("Endomorphisms failed to commute on a = $sa:")
            appendLine("  $fSymbol($gSymbol(a)) = $sfg")
            appendLine("  $gSymbol($fSymbol(a)) = $sgf")
        }
    }

    override suspend fun test() =
        endomorphismCommutationCheck()
}
