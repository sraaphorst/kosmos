package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Bilinearity of a map f : V × V → F in both arguments:
 *
 * 1. f(x + y, z) = f(x, z) + f(y, z)
 * 2. f(a · x, z) = a * f(x, z)
 * 3. f(x, y + z) = f(x, y) + f(x, z)
 * 4. f(x, b · z) = b * f(x, z)
 */
class BilinearityLaw<F : Any, V : Any>(
    private val f: (V, V) -> F,
    private val addF: BinOp<F>,
    private val mulF: BinOp<F>,
    private val addV: BinOp<V>,
    private val scalarAction: Action<F, V>, //(F, V) -> V,
    private val scalarArb: Arb<F>,
    private val vectorArb: Arb<V>,
    private val eq: Eq<F>,
    private val prF: Printable<F> = Printable.default(),
    private val prV: Printable<V> = Printable.default(),
    private val symbol: String = "⟨·,·⟩"
) : TestingLaw {

    override val name: String = "bilinearity ($symbol)"

    override suspend fun test() {
        // Left additivity: f(x + y, z) = f(x, z) + f(y, z)
        checkAll(vectorArb, vectorArb, vectorArb) { x, y, z ->
            val left = f(addV(x, y), z)
            val right = addF(f(x, z), f(y, z))

            withClue(leftAddFailure(x, y, z, left, right)) {
                check(eq.eqv(left, right))
            }
        }

        // Left homogeneity: f(a·x, z) = a * f(x, z)
        checkAll(scalarArb, vectorArb, vectorArb) { a, x, z ->
            val left = f(scalarAction(a, x), z)
            val right = mulF(a, f(x, z))

            withClue(leftHomFailure(a, x, z, left, right)) {
                check(eq.eqv(left, right))
            }
        }

        // Right additivity: f(x, y + z) = f(x, y) + f(x, z)
        checkAll(vectorArb, vectorArb, vectorArb) { x, y, z ->
            val left = f(x, addV(y, z))
            val right = addF(f(x, y), f(x, z))

            withClue(rightAddFailure(x, y, z, left, right)) {
                check(eq.eqv(left, right))
            }
        }

        // Right homogeneity: f(x, b·z) = b * f(x, z)
        checkAll(scalarArb, vectorArb, vectorArb) { b, x, z ->
            val left = f(x, scalarAction(b, z))
            val right = mulF(b, f(x, z))

            withClue(rightHomFailure(b, x, z, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun leftAddFailure(
        x: V, y: V, z: V,
        left: F, right: F
    ): () -> String = {
        val sx = prV.render(x)
        val sy = prV.render(y)
        val sz = prV.render(z)
        val sl = prF.render(left)
        val sr = prF.render(right)

        buildString {
            appendLine("Left additivity of $symbol failed:")
            appendLine("$symbol($sx + $sy, $sz) = $sl")
            appendLine("$symbol($sx, $sz) + $symbol($sy, $sz) = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    private fun leftHomFailure(
        a: F, x: V, z: V,
        left: F, right: F
    ): () -> String = {
        val sa = prF.render(a)
        val sx = prV.render(x)
        val sz = prV.render(z)
        val sl = prF.render(left)
        val sr = prF.render(right)

        buildString {
            appendLine("Left homogeneity of $symbol failed:")
            appendLine("$symbol($sa·$sx, $sz) = $sl")
            appendLine("$sa * $symbol($sx, $sz) = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    private fun rightAddFailure(
        x: V, y: V, z: V,
        left: F, right: F
    ): () -> String = {
        val sx = prV.render(x)
        val sy = prV.render(y)
        val sz = prV.render(z)
        val sl = prF.render(left)
        val sr = prF.render(right)

        buildString {
            appendLine("Right additivity of $symbol failed:")
            appendLine("$symbol($sx, $sy + $sz) = $sl")
            appendLine("$symbol($sx, $sy) + $symbol($sx, $sz) = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }

    private fun rightHomFailure(
        b: F, x: V, z: V,
        left: F, right: F
    ): () -> String = {
        val sb = prF.render(b)
        val sx = prV.render(x)
        val sz = prV.render(z)
        val sl = prF.render(left)
        val sr = prF.render(right)

        buildString {
            appendLine("Right homogeneity of $symbol failed:")
            appendLine("$symbol($sx, $sb·$sz) = $sl")
            appendLine("$sb * $symbol($sx, $sz) = $sr")
            appendLine("Expected: $sl = $sr")
        }
    }
}