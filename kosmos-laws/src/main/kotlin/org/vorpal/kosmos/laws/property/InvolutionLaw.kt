package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith


/**
 * Involution:
 *
 * A unary operation that is self-inverting, i.e. `∀x ∈ A`:
 *
 *     f(f(x)) = x
 */
class InvolutionLaw<A : Any>(
    private val op: Endo<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {
    override val name = "involution (${op.symbol})"

    /**
     * `f(f(x)) = x`
     */
    suspend fun involutionCheck() {
        lawCheckAll(arb, render = renderWith(pr)) { a ->
            val value = op(op(a))
            lawCheck(eq(a, value), involutionFailure(a, value))
        }
    }

    private fun involutionFailure(
        a: A, value: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sValue = pr(value)

            appendLine("Involution failed:")
            append("${op.symbol}(${op.symbol}($sa))")
            append(" = ")
            append(sValue)
            appendLine(" (expected: $sa)")
        }
    }

    override suspend fun test() =
        involutionCheck()
}
