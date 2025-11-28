package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Positive-definiteness of a quadratic form induced by an inner product.
 *
 * Given inner : V -> F representing x ↦ ⟨x, x⟩, we require:
 *
 *  1. Non-negativity:
 *        inner(x) ≥ 0    for all x
 *
 *  2. Non-degeneracy:
 *        inner(x) = 0  ⇔  x = 0
 *
 * The actual notion of "≥ 0" is provided by [isNonNegative].
 */
class PositiveDefiniteLaw<F : Any, V : Any>(
    private val inner: (V) -> F,
    private val zeroVector: V,
    private val scalarZero: F,
    private val vectorArb: Arb<V>,
    private val scalarEq: Eq<F>,
    private val vectorEq: Eq<V>,
    private val isNonNegative: (F) -> Boolean,
    private val scalarPrintable: Printable<F> = Printable.default(),
    private val vectorPrintable: Printable<V> = Printable.default(),
    private val symbol: String = "⟨·,·⟩"
) : TestingLaw {

    override val name: String = "positive-definiteness ($symbol)"

    override suspend fun test() {
        checkAll(vectorArb) { x ->
            val value = inner(x)

            // 1. Non-negativity: inner(x) >= 0
            withClue(nonNegativeFailure(x, value)) {
                check(isNonNegative(value))
            }

            // 2. Non-degeneracy: inner(x) = 0  ⇔  x = 0
            val isScalarZero = scalarEq.eqv(value, scalarZero)
            val isVectorZero = vectorEq.eqv(x, zeroVector)

            withClue(nonDegenerateFailure(x, value, isScalarZero, isVectorZero)) {
                check(isScalarZero == isVectorZero)
            }
        }
    }

    private fun nonNegativeFailure(
        x: V,
        value: F
    ): () -> String = {
        val sx = vectorPrintable.render(x)
        val sv = scalarPrintable.render(value)

        buildString {
            appendLine("Positive-definiteness (non-negativity) failed for $symbol:")
            appendLine("$symbol($sx, $sx) = $sv is not ≥ 0")
        }
    }

    private fun nonDegenerateFailure(
        x: V,
        value: F,
        isScalarZero: Boolean,
        isVectorZero: Boolean
    ): () -> String = {
        val sx = vectorPrintable.render(x)
        val sv = scalarPrintable.render(value)

        buildString {
            appendLine("Positive-definiteness (non-degeneracy) failed for $symbol:")
            appendLine("$symbol($sx, $sx) = $sv")
            appendLine("scalarZero?  = $isScalarZero")
            appendLine("vectorZero?  = $isVectorZero")
            appendLine("Expected: (inner(x) == 0) ⇔ (x == 0)")
        }
    }
}