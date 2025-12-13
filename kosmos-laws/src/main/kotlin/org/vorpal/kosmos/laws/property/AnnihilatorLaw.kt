package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface AnnihilatorCore<A : Any> {
    val op: BinOp<A>
    val zero: A
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun expr(left: String, right: String) = "$left ${op.symbol} $right"

    /**
     * `0a = 0`
     */
    suspend fun leftAnnihilationCheck() {
        checkAll(arb) { a ->
            val value = op(zero, a)
            withClue(leftAnnihilationFailure(a, value)) {
                check(eq(zero, value))
            }
        }
    }

    private fun leftAnnihilationFailure(
        a: A, value: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sValue = pr(value)
            val sZero = pr(zero)

            appendLine("Left annihilation failed:")
            append(expr(sZero, sa))
            append(" = ")
            append(sValue)
            appendLine(" (expected: $sZero)")
        }
    }

    /**
     * `a0 = 0`
     */
    suspend fun rightAnnihilationCheck() {
        checkAll(arb) { a ->
            val value = op(a, zero)
            withClue(rightAnnihilationFailure(a, value)) {
                check(eq(zero, value))
            }
        }
    }

    private fun rightAnnihilationFailure(
        a: A, value: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sValue = pr(value)
            val sZero = pr(zero)

            appendLine("Right annihilation failed:")
            append(expr(sa, sZero))
            append(" = ")
            append(sValue)
            appendLine(" (expected: $sZero)")
        }
    }
}

/**
 * Left annihilator law: `0a = 0`
 */
class LeftAnnihilatorLaw<A : Any>(
    override val op: BinOp<A>,
    override val zero: A,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, AnnihilatorCore<A> {
    override val name = "left annihilator (${op.symbol})"
    override suspend fun test() = leftAnnihilationCheck()
}

/**
 * Right annihilator law: `a0 = 0`
 */
class RightAnnihilatorLaw<A : Any>(
    override val op: BinOp<A>,
    override val zero: A,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, AnnihilatorCore<A> {
    override val name = "right annihilator (${op.symbol})"
    override suspend fun test() = rightAnnihilationCheck()
}

/**
 * Test an annihilator (zero) for left and / or right annihilation of elements.
 *
 * Let us denote the annihilator as 0. Then for any a, the following laws should hold:
 * - Left annihilation: `0a = 0`
 * - Right annihilation: `a0 = 0`
 */
class AnnihilatorLaw<A : Any>(
    override val op: BinOp<A>,
    override val zero: A,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, AnnihilatorCore<A> {
    override val name = "annihilator (${op.symbol})"

    override suspend fun test() {
        leftAnnihilationCheck()
        rightAnnihilationCheck()
    }
}
