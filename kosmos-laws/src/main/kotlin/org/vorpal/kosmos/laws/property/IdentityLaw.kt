package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface IdentityCore<A : Any> {
    val op: BinOp<A>
    val identity: A
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun expr(left: String, right: String) = "$left ${op.symbol} $right"

    /**
     * `ea = a`
     */
    suspend fun leftIdentityCheck() {
        checkAll(arb) { a ->
            val value = op(identity, a)
            withClue(leftIdentityFail(a, value)) {
                check(eq(a, value))
            }
        }
    }

    private fun leftIdentityFail(
        a: A, value: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sValue = pr(value)
            val sid = pr(identity)

            appendLine("Left identity failed:")
            append(expr(sid, sa))
            append(" = ")
            append(sValue)
            appendLine(" (expected: $sa)")
        }
    }

    /**
     * `ae = a`
     */
    suspend fun rightIdentityCheck() {
        checkAll(arb) { a ->
            val value = op(a, identity)
            withClue(rightIdentityFail(a, value)) {
                check(eq(a, value))
            }
        }
    }

    private fun rightIdentityFail(
        a: A, value: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sValue = pr(value)
            val sid = pr(identity)

            appendLine("Right identity failed:")
            append(expr(sa, sid))
            append(" = ")
            append(sValue)
            appendLine(" (expected: $sa)")
        }
    }
}

/**
 * There is a left identity element `e` such that for all `a`:
 *
 *    ea = a
 */
class LeftIdentityLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, IdentityCore<A> {
    override val name = "left identity (${op.symbol})"
    override suspend fun test() =
        leftIdentityCheck()
}

/**
 * There is a right identity element `e` such that for all `a`:
 *
 *    ae = a
 */
class RightIdentityLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, IdentityCore<A> {
    override val name = "right identity (${op.symbol})"
    override suspend fun test() =
        rightIdentityCheck()
}

/**
 * There is an identity element `e` such that for all `a`:
 *
 *    ae = a
 *    ea = a
 */
class IdentityLaw<A : Any>(
    override val op: BinOp<A>,
    override val identity: A,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default(),
) : TestingLaw, IdentityCore<A> {
    override val name = "identity (${op.symbol})"
    override suspend fun test() {
        leftIdentityCheck()
        rightIdentityCheck()
    }
}
