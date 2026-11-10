package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll

/**
 * This is not currently in use, but will pair with structures like:
 * - `InnerProductSpace<V, F>`
 * - `SymmetricBilinearForm<V, F>`
 * - `Metric<X, Real>`
 * - Kernels, covariance functions, "Gram"-like maps.
 */
class SymmetryLaw<A : Any, B : Any>(
    private val op: (A, A) -> B,
    private val arb: Arb<A>,
    private val eq: Eq<B> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prB: Printable<B> = Printable.default(),
    private val symbol: String = "⟨·,·⟩"
) : TestingLaw {

    override val name: String = "symmetry ($symbol)"

    override suspend fun test() {
        lawCheckAll(TestingLaw.arbPair(arb)) { (x, y) ->
            val left = op(x, y)
            val right = op(y, x)

            lawCheck(eq(left, right), failureMessage(x, y, left, right))
        }
    }

    private fun failureMessage(
        x: A,
        y: A,
        left: B,
        right: B
    ): () -> String = {
        buildString {
            val sx = prA(x)
            val sy = prA(y)
            val sl = prB(left)
            val sr = prB(right)
            appendLine("Symmetry law failed for $symbol:")
            appendLine("$symbol($sx, $sy) = $sl")
            appendLine("$symbol($sy, $sx) = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }
}