package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.triple
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

private sealed interface DistributivityCore<A: Any> {
    val mul: BinOp<A>
    val add: BinOp<A>
    val tripleArb: Arb<Triple<A, A, A>>
    val eq: Eq<A>
    val pr: Printable<A>
    val mulSymbol: String
    val addSymbol: String

    private fun mulInfix(l: String, r: String) = "$l $mulSymbol $r"
    private fun addInfix(l: String, r: String) = "$l $addSymbol $r"

    suspend fun leftDistributivityCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val bcAdd = add(b, c)
            val left = mul(a, bcAdd)

            val abMul = mul(a, b)
            val acMul = mul(a, c)
            val right = add(abMul, acMul)

            withClue(leftFailureMessage(a, b, c, bcAdd, abMul, acMul, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun leftFailureMessage(
        a: A, b: A, c: A,
        bcAdd: A, abMul: A, acMul: A,
        left: A, right: A
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        val sbcAdd = pr.render(bcAdd)
        val sabMul = pr.render(abMul)
        val sacMul = pr.render(acMul)
        val sLeft = pr.render(left)
        val sRight = pr.render(right)

        buildString {
            appendLine("Left distributivity failed:")

            append(mulInfix(sa, "(" + addInfix(sb, sc) + ")"))
            append(" = ")
            append(mulInfix(sa, sbcAdd))
            append(" = ")
            append(sLeft)
            appendLine()

            append(mulInfix(sa, "(" + addInfix(sb, sc) + ")"))
            append(" = ")
            append(addInfix(mulInfix(sa, sb), mulInfix(sa, sc)))
            append(" = ")
            append(addInfix(sabMul, sacMul))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }

    suspend fun rightDistributivityCheck() {
        checkAll(tripleArb) { (a, b, c) ->
            val abAdd = add(a, b)
            val left = mul(abAdd, c)

            val acMul = mul(a, c)
            val bcMul = mul(b, c)
            val right = add(acMul, bcMul)
            withClue(rightFailureMessage(a, b, c, abAdd, acMul, bcMul, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun rightFailureMessage(
        a: A, b: A, c: A,
        abAdd: A, acMul: A, bcMul: A,
        left: A, right: A
    ): () -> String = {
        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        val sabAdd = pr.render(abAdd)
        val sacMul = pr.render(acMul)
        val sbcMul = pr.render(bcMul)
        val sLeft = pr.render(left)
        val sRight = pr.render(right)

        buildString {
            appendLine("Right distributivity failed:")

            append(mulInfix("(" + addInfix(sa, sb) + ")", sc))
            append(" = ")
            append(mulInfix(sabAdd, sc))
            append(" = ")
            append(sLeft)
            appendLine()

            append(mulInfix("(" + addInfix(sa, sb) + ")", sc))
            append(" = ")
            append(addInfix(mulInfix(sa, sc), mulInfix(sb, sc)))
            append(" = ")
            append(addInfix(sacMul, sbcMul))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}

class LeftDistributivityLaw<A: Any>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val mulSymbol: String = "*",
    override val addSymbol: String = "+"
) : TestingLaw, DistributivityCore<A> {

    constructor(
        mul: BinOp<A>,
        add: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        mulSymbol: String = "*",
        addSymbol: String = "+"
    ) : this(mul, add, Arb.triple(arb, arb, arb), eq, pr, mulSymbol, addSymbol)

    override val name = "distributivity (left: $mulSymbol over $addSymbol)"
    override suspend fun test() = leftDistributivityCheck()
}

class RightDistributivityLaw<A: Any>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val mulSymbol: String = "*",
    override val addSymbol: String = "+"
) : TestingLaw, DistributivityCore<A> {

    constructor(
        mul: BinOp<A>,
        add: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        mulSymbol: String = "*",
        addSymbol: String = "+"
    ) : this(mul, add, Arb.triple(arb, arb, arb), eq, pr, mulSymbol, addSymbol)

    override val name = "distributivity (right: $mulSymbol over $addSymbol)"
    override suspend fun test() = rightDistributivityCheck()
}

/** Distributivity Laws: the first operator, mul, distributes over the second, add.
 * This can be on the left, on the right, or both depending on the structure.
 * Note that we allow a Triple producing Arb so that we can impose constraints if necessary on the values produced,
 * e.g. that they all be distinct, or to avoid NaN / overflow for floating point types.
 * The checks are in an interface so that they can be reused across the three distributivity check variants. */
class DistributivityLaw<A: Any>(
    override val mul: BinOp<A>,
    override val add: BinOp<A>,
    override val tripleArb: Arb<Triple<A, A, A>>,
    override val eq: Eq<A>,
    override val pr: Printable<A> = Printable.default(),
    override val mulSymbol: String = "*",
    override val addSymbol: String = "+"
) : TestingLaw, DistributivityCore<A> {

    constructor(
        mul: BinOp<A>,
        add: BinOp<A>,
        arb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        mulSymbol: String = "*",
        addSymbol: String = "+"
    ) : this(mul, add, Arb.triple(arb, arb, arb), eq, pr, mulSymbol, addSymbol)

    override val name = "distributivity (both: $mulSymbol over $addSymbol)"
    override suspend fun test() {
        leftDistributivityCheck()
        rightDistributivityCheck()
    }
}
