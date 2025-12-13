package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface DistributivityCore<A : Any> {
    val mul: BinOp<A>
    val add: BinOp<A>
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun mulExpr(left: String, right: String) = "$left ${mul.symbol} $right"
    private fun addExpr(left: String, right: String) = "$left ${add.symbol} $right"

    suspend fun leftDistributivityCheck() {
        checkAll(TestingLaw.arbTriple(arb)) { (a, b, c) ->
            val bcAdd = add(b, c)
            val left = mul(a, bcAdd)

            val abMul = mul(a, b)
            val acMul = mul(a, c)
            val right = add(abMul, acMul)

            withClue(leftDistributivityFailure(a, b, c, bcAdd, abMul, acMul, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun leftDistributivityFailure(
        a: A, b: A, c: A,
        bcAdd: A, abMul: A, acMul: A,
        left: A, right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sbcAdd = pr(bcAdd)
            val sabMul = pr(abMul)
            val sacMul = pr(acMul)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Left distributivity failed:")

            append(mulExpr(sa, "(" + addExpr(sb, sc) + ")"))
            append(" = ")
            append(mulExpr(sa, sbcAdd))
            append(" = ")
            appendLine(sLeft)

            append(mulExpr(sa, "(" + addExpr(sb, sc) + ")"))
            append(" = ")
            append(addExpr(mulExpr(sa, sb), mulExpr(sa, sc)))
            append(" = ")
            append(addExpr(sabMul, sacMul))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }

    suspend fun rightDistributivityCheck() {
        checkAll(TestingLaw.arbTriple(arb)) { (a, b, c) ->
            val abAdd = add(a, b)
            val left = mul(abAdd, c)

            val acMul = mul(a, c)
            val bcMul = mul(b, c)
            val right = add(acMul, bcMul)
            withClue(rightFailureMessage(a, b, c, abAdd, acMul, bcMul, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun rightFailureMessage(
        a: A, b: A, c: A,
        abAdd: A, acMul: A, bcMul: A,
        left: A, right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sabAdd = pr(abAdd)
            val sacMul = pr(acMul)
            val sbcMul = pr(bcMul)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Right distributivity failed:")

            append(mulExpr("(" + addExpr(sa, sb) + ")", sc))
            append(" = ")
            append(mulExpr(sabAdd, sc))
            append(" = ")
            appendLine(sLeft)

            append(mulExpr("(" + addExpr(sa, sb) + ")", sc))
            append(" = ")
            append(addExpr(mulExpr(sa, sc), mulExpr(sb, sc)))
            append(" = ")
            append(addExpr(sacMul, sbcMul))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }
}

/**
 * Left distributivity law:
 *
 *     a * (b + c) = a * b + a * c
 *
 */
class LeftDistributivityLaw<A : Any>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, DistributivityCore<A> {
    override val name = "distributivity (left: ${mul.symbol} over ${add.symbol})"
    override suspend fun test() = leftDistributivityCheck()
}

/**
 * Right distributivity law:
 *
 *     (a + b) * c = a * c + b * c
 *
 */
class RightDistributivityLaw<A : Any>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, DistributivityCore<A> {
    override val name = "distributivity (right: ${mul.symbol} over ${add.symbol})"
    override suspend fun test() = rightDistributivityCheck()
}

/**
 * Distributivity Laws:
 *
 *     a * (b + c) = a * b + a * c
 *     (a + b) * c = a * c + b * c
 *
 */
class DistributivityLaw<A : Any>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, DistributivityCore<A> {
    override val name = "distributivity (both: ${mul.symbol} over ${add.symbol})"
    override suspend fun test() {
        leftDistributivityCheck()
        rightDistributivityCheck()
    }
}
