package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.TernOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface TernaryDistributivityCore<A : Any> {
    val mul: BinOp<A>
    val add: TernOp<A>
    val arb: Arb<A>
    val eq: Eq<A>
    val pr: Printable<A>

    private fun mulExpr(left: String, right: String): String =
        "$left ${mul.symbol} $right"

    private fun addExpr(first: String, second: String, third: String): String =
        "${add.symbol}($first, $second, $third)"

    suspend fun leftTernaryDistributivityCheck() {
        checkAll(arb, arb, arb, arb) { a, b, c, d ->
            val bcdAdd = add(b, c, d)
            val left = mul(a, bcdAdd)

            val abMul = mul(a, b)
            val acMul = mul(a, c)
            val adMul = mul(a, d)
            val right = add(abMul, acMul, adMul)

            withClue(leftTernaryDistributivityFailure(a, b, c, d, bcdAdd, abMul, acMul, adMul, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun leftTernaryDistributivityFailure(
        a: A,
        b: A,
        c: A,
        d: A,
        bcdAdd: A,
        abMul: A,
        acMul: A,
        adMul: A,
        left: A,
        right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sd = pr(d)
            val sbcdAdd = pr(bcdAdd)
            val sabMul = pr(abMul)
            val sacMul = pr(acMul)
            val sadMul = pr(adMul)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Left ternary distributivity failed:")

            append(mulExpr(sa, addExpr(sb, sc, sd)))
            append(" = ")
            append(mulExpr(sa, sbcdAdd))
            append(" = ")
            appendLine(sLeft)

            append(mulExpr(sa, addExpr(sb, sc, sd)))
            append(" = ")
            append(addExpr(mulExpr(sa, sb), mulExpr(sa, sc), mulExpr(sa, sd)))
            append(" = ")
            append(addExpr(sabMul, sacMul, sadMul))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }

    suspend fun rightTernaryDistributivityCheck() {
        checkAll(arb, arb, arb, arb) { a, b, c, d ->
            val abcAdd = add(a, b, c)
            val left = mul(abcAdd, d)

            val adMul = mul(a, d)
            val bdMul = mul(b, d)
            val cdMul = mul(c, d)
            val right = add(adMul, bdMul, cdMul)

            withClue(rightTernaryDistributivityFailure(a, b, c, d, abcAdd, adMul, bdMul, cdMul, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun rightTernaryDistributivityFailure(
        a: A,
        b: A,
        c: A,
        d: A,
        abcAdd: A,
        adMul: A,
        bdMul: A,
        cdMul: A,
        left: A,
        right: A
    ): () -> String = {
        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sd = pr(d)
            val sabcAdd = pr(abcAdd)
            val sadMul = pr(adMul)
            val sbdMul = pr(bdMul)
            val scdMul = pr(cdMul)
            val sLeft = pr(left)
            val sRight = pr(right)

            appendLine("Right ternary distributivity failed:")

            append(mulExpr(addExpr(sa, sb, sc), sd))
            append(" = ")
            append(mulExpr(sabcAdd, sd))
            append(" = ")
            appendLine(sLeft)

            append(mulExpr(addExpr(sa, sb, sc), sd))
            append(" = ")
            append(addExpr(mulExpr(sa, sd), mulExpr(sb, sd), mulExpr(sc, sd)))
            append(" = ")
            append(addExpr(sadMul, sbdMul, scdMul))
            append(" = ")
            appendLine(sRight)

            appendLine("Expected: $sLeft = $sRight")
        }
    }
}

/**
 * Left ternary distributivity law:
 *
 *     a * [b, c, d] = [a * b, a * c, a * d]
 *
 */
class LeftTernaryDistributivityLaw<A : Any>(
    override val mul: BinOp<A>,
    override val add: TernOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, TernaryDistributivityCore<A> {
    override val name =
        "ternary distributivity (left: ${mul.symbol} over ${add.symbol})"

    override suspend fun test() =
        leftTernaryDistributivityCheck()
}

/**
 * Right ternary distributivity law:
 *
 *     [a, b, c] * d = [a * d, b * d, c * d]
 *
 */
class RightTernaryDistributivityLaw<A : Any>(
    override val mul: BinOp<A>,
    override val add: TernOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, TernaryDistributivityCore<A> {
    override val name =
        "ternary distributivity (right: ${mul.symbol} over ${add.symbol})"

    override suspend fun test() =
        rightTernaryDistributivityCheck()
}

/**
 * Ternary distributivity laws:
 *
 *     a * [b, c, d] = [a * b, a * c, a * d]
 *     [a, b, c] * d = [a * d, b * d, c * d]
 *
 */
class TernaryDistributivityLaw<A : Any>(
    override val mul: BinOp<A>,
    override val add: TernOp<A>,
    override val arb: Arb<A>,
    override val eq: Eq<A> = Eq.default(),
    override val pr: Printable<A> = Printable.default()
) : TestingLaw, TernaryDistributivityCore<A> {
    override val name =
        "ternary distributivity (both: ${mul.symbol} over ${add.symbol})"

    override suspend fun test() {
        leftTernaryDistributivityCheck()
        rightTernaryDistributivityCheck()
    }
}
