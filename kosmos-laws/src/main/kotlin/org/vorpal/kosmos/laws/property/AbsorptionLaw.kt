package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface AbsorptionCore<A : Any> {
    val absorb: BinOp<A>
    val over: BinOp<A>
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun absorbExpr(left: String, right: String): String = "$left ${absorb.symbol} $right"
    private fun overExpr(left: String, right: String): String = "$left ${over.symbol} $right"

    /**
     * `a ∧ (a ∨ b) = a``
     */
    suspend fun absorbOverCheck() {
        checkAll(TestingLaw.arbPair(arb)) { (a, b) ->
            val aOverB = over(a, b)
            val left   = absorb(a, aOverB)
            withClue(absorbOverFailure(a, b, aOverB, left)) {
                check(eq(left, a))
            }
        }
    }

    private fun absorbOverFailure(
        a: A, b: A, aOverB: A, left: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sAoverB = pr(aOverB)
            val sLeft = pr(left)
            appendLine("Absorption law failed (absorb over):")
            append(absorbExpr(sa, "(" + overExpr(sa, sb) + ")"))
            append(" = ")
            append(absorbExpr(sa, sAoverB))
            append(" = ")
            append(sLeft)
            appendLine(" (expected: $sa)")
        }
    }

    /**
     * `a ∨ (a ∧ b) = a`
     */
    suspend fun overAbsorbCheck() {
        checkAll(TestingLaw.arbPair(arb)) { (a, b) ->
            val aAbsorbB = absorb(a, b)
            val left     = over(a, aAbsorbB)
            withClue(overAbsorbFailure(a, b, aAbsorbB, left)) {
                check(eq(left, a))
            }
        }
    }

    private fun overAbsorbFailure(
        a: A, b: A, aAbsorbB: A, left: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sAabsorbB = pr(aAbsorbB)
            val sLeft = pr(left)
            appendLine("Absorption law failed (over absorb):")
            append(overExpr(sa, "(" + absorbExpr(sa, sb) + ")"))
            append(" = ")
            append(overExpr(sa, sAabsorbB))
            append(" = ")
            append(sLeft)
            appendLine(" (expected: $sa)")
        }
    }
}

/**
 * The absorb over check:
 *
 *     a ∧ (a ∨ b) = a
 */
class AbsorbOverLaw<A : Any>(
    override val absorb: BinOp<A>,
    override val over: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
): TestingLaw, AbsorptionCore<A> {
    override val name = "absorb-over: ${absorb.symbol} over ${over.symbol}"
    override suspend fun test() = absorbOverCheck()
}

/**
 * The over absorb check:
 *
 *     a ∨ (a ∧ b) = a
 */
class OverAbsorbLaw<A : Any>(
    override val absorb: BinOp<A>,
    override val over: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
): TestingLaw, AbsorptionCore<A> {
    override val name = "over-absorb: ${over.symbol} over ${absorb.symbol}"
    override suspend fun test() = overAbsorbCheck()
}

/**
 * The absorption law links two binary operations, say absorb (∧) and over (∨):
 * - `a ∧ (a ∨ b) = a`
 * - `a ∨ (a ∧ b) = a`
 *
 * These typically apply to lattices and logic.
 */
class AbsorptionLaw<A : Any>(
    override val absorb: BinOp<A>,
    override val over: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, AbsorptionCore<A> {
    override val name = "absorption: ${absorb.symbol} over ${over.symbol} (both directions)"

    override suspend fun test() {
        absorbOverCheck()
        overAbsorbCheck()
    }
}
