package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.math.RealTolerances
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Positive-definiteness of a quadratic form induced by an inner product.
 *
 * Given `inner : V -> F` representing `x ↦ ⟨x, x⟩`, we require:
 *
 *  1. Non-negativity:
 *
 *
 *        inner(x) ≥ 0    for all x
 *
 *  2. Non-degeneracy:
 *
 *
 *        inner(x) = 0  ⇔  x = 0
 *
 * The actual notion of "≥ 0" is provided by [isNonNegative].
 */
class PositiveDefiniteLaw<F : Any, V : Any>(
    private val inner: UnaryOp<V, F>,
    private val zeroVector: V,
    private val scalarZero: F,
    private val isNonNegative: (F) -> Boolean,
    private val vectorArb: Arb<V>,
    private val scalarEq: Eq<F> = Eq.default(),
    private val vectorEq: Eq<V> = Eq.default(),
    private val scalarPrintable: Printable<F> = Printable.default(),
    private val vectorPrintable: Printable<V> = Printable.default(),
) : TestingLaw {

    // Assert sanity for the isNonNegative to the best of our ability.
    init {
        require(isNonNegative(scalarZero)) {
            "PositiveDefiniteLaw: scalarZero ${scalarPrintable(scalarZero)} is not considered non-negative by isNonNegative"
        }
    }

    private fun expr(left: String, right: String): String = "⟨$left, $right⟩"
    override val name: String = "positive-definiteness ($SYMBOL)"

    private suspend fun nonNegativeCheck() {
        checkAll(vectorArb) { x ->
            val value = inner(x)

            withClue(nonNegativeFailure(x, value)) {
                check(isNonNegative(value))
            }
        }
    }

    private fun nonNegativeFailure(
        x: V,
        value: F
    ): () -> String = {
        buildString {
            val sx = vectorPrintable(x)
            val sv = scalarPrintable(value)
            appendLine("Positive-definiteness (non-negativity) failed for $SYMBOL:")
            appendLine("${expr(sx, sx)} = $sv is not ≥ 0")
        }
    }

    private suspend fun nonDegeneracyCheck() {
        checkAll(vectorArb) { x ->
            val value = inner(x)

            val isScalarZero = scalarEq(value, scalarZero)
            val isVectorZero = vectorEq(x, zeroVector)

            withClue(nonDegeneracyFailure(x, value, isScalarZero, isVectorZero)) {
                check(isScalarZero == isVectorZero)
            }
        }
    }

    private fun nonDegeneracyFailure(
        x: V,
        value: F,
        isScalarZero: Boolean,
        isVectorZero: Boolean
    ): () -> String = {
        buildString {
            val sx = vectorPrintable(x)
            val sv = scalarPrintable(value)
            appendLine("Positive-definiteness (non-degeneracy) failed for $SYMBOL:")
            appendLine("${expr(sx, sx)} = $sv")
            appendLine("scalarZero?  = $isScalarZero")
            appendLine("vectorZero?  = $isVectorZero")
            appendLine("Expected: (inner(x) == 0) ⇔ (x == 0)")
        }
    }

    override suspend fun test() {
        nonNegativeCheck()
        nonDegeneracyCheck()
    }

    companion object {
        const val SYMBOL = "⟨·,·⟩"
    }
}

/**
 * Convenience for Real, so we don't have to keep writing this repeatedly.
 */
fun <V: Any> realPositiveDefiniteLaw(
    inner: UnaryOp<V, Real>,
    zeroVector: V,
    vectorArb: Arb<V>,
    vectorEq: Eq<V> = Eq.default(),
    vectorPr: Printable<V> = Printable.default(),
    tolerance: Real = RealTolerances.DEFAULT
): PositiveDefiniteLaw<Real, V> =
    PositiveDefiniteLaw(
        inner = inner,
        zeroVector = zeroVector,
        scalarZero = 0.0,
        isNonNegative = { it >= -tolerance },
        vectorArb = vectorArb,
        scalarEq = Eqs.realApprox(absTol = tolerance, relTol = tolerance),
        vectorEq = vectorEq,
        scalarPrintable = Printable.default(),
        vectorPrintable = vectorPr
    )