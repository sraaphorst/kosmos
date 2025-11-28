package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
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
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val inDomain: (A) -> Boolean = { true },
    private val nameHint: String? = null
) : TestingLaw {

    private val fSymbol: String = f.symbol
    private val gSymbol: String = g.symbol

    override val name: String =
        nameHint ?: "endomorphisms commute: $fSymbol ∘ $gSymbol = $gSymbol ∘ $fSymbol"

    override suspend fun test() {
        checkAll(arb) { a ->
            if (!inDomain(a)) return@checkAll

            val fg = f(g(a))
            val gf = g(f(a))

            withClue(buildString {
                appendLine("Endomorphisms failed to commute on a:")
                appendLine("  a         = ${pr.render(a)}")
                appendLine("  $fSymbol($gSymbol(a)) = ${pr.render(fg)}")
                appendLine("  $gSymbol($fSymbol(a)) = ${pr.render(gf)}")
            }) {
                check(eq.eqv(fg, gf))
            }
        }
    }
}