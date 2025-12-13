package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

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
    override val name = "Involution (${op.symbol})"

    /**
     * `f(f(x)) = x`
     */
    suspend fun involutionCheck() {
        checkAll(arb) { a ->
            val value = op(op(a))
            withClue(involutionFailure(a, value)) {
                check(eq(a, value))
            }
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
