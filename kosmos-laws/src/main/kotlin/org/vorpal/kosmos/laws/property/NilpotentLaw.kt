package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import org.vorpal.kosmos.testing.existsSample
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Nilpotent of index n ≥ 2:
 * * Every product of n elements is zero.
 * * There exists a product of (n-1) elements that is nonzero.
 *
 * If `checkAllBracketings=true`, (1) is checked for *all* parenthesizations (nonassociative definition).
 */
class NilpotentLaw<A: Any>(
    private val op: BinOp<A>,
    private val n: Int,
    private val zero: A,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val nonzeroAttempts: Int = 1000,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆",
    private val checkAllBracketings: Boolean = false
) : TestingLaw {

    init {
        require(n >= 2) { "Nilpotent law requires n ≥ 2, but got $n" }
        require(nonzeroAttempts > 0) { "nonzeroAttempts must be positive, but got $nonzeroAttempts" }
    }

    override val name =
        if (checkAllBracketings) "nilpotent (n=$n, $symbol, all bracketings)"
        else "nilpotent (n=$n, $symbol)"

    override suspend fun test() {
        nonzeroProductExists()
        allLengthNProductsAreZero()
    }

    /** 2) There exists a product of (n-1) elements that is nonzero. */
    private fun nonzeroProductExists() {
        val m = n - 1
        val listArb = Arb.list(arb, m..m)
        withClue("Nilpotency failed: no nonzero product over $m elements was found.") {
            val witness = existsSample(listArb, nonzeroAttempts) { xs ->
                val v = if (checkAllBracketings) {
                    allParenthesizations(xs, op).any { !eq.eqv(it, zero) }
                } else {
                    xs.reduce(op::invoke).let { !eq.eqv(it, zero) }
                }
                v
            }
            check(witness != null)
        }
    }

    /** 1) Every product of n elements is zero (for all bracketings if requested). */
    private suspend fun allLengthNProductsAreZero() {
        val listArb = Arb.list(arb, n..n)
        checkAll(listArb) { xs ->
            if (checkAllBracketings) {
                val prods = allParenthesizations(xs, op)
                withClue(zeroFailureMsgAll(xs, prods)) {
                    check(prods.all { eq.eqv(it, zero) })
                }
            } else {
                val prod = xs.reduce(op::invoke)
                withClue(zeroFailureMsg(xs, prod)) {
                    check(eq.eqv(prod, zero))
                }
            }
        }
    }

    // ---- pretty messages ----
    private fun expr(xs: List<A>) = xs.joinToString(" $symbol ") { pr.render(it) }

    private fun zeroFailureMsg(xs: List<A>, prod: A): () -> String = {
        buildString {
            appendLine("Nilpotency failed (single bracketing):")
            appendLine("${expr(xs)} = ${pr.render(prod)} (expected: ${pr.render(zero)})")
        }
    }

    private fun zeroFailureMsgAll(xs: List<A>, prods: List<A>): () -> String = {
        buildString {
            appendLine("Nilpotency failed (some bracketing nonzero):")
            appendLine("operands: ${expr(xs)}")
            val nonzeros = prods.filterNot { eq.eqv(it, zero) }
            nonzeros.take(5).forEachIndexed { i, v ->
                appendLine("  witness[$i] = ${pr.render(v)} ≠ ${pr.render(zero)}")
            }
            if (nonzeros.size > 5) appendLine("  ...and ${nonzeros.size - 5} more.")
        }
    }
}

/** Enumerate all parenthesizations’ results of combining a list with a binary op. */
private fun <A> allParenthesizations(xs: List<A>, op: BinOp<A>): List<A> =
    when (xs.size) {
        0 -> emptyList()
        1 -> listOf(xs[0])
        else -> (1 until xs.size).flatMap { i ->
            val ls = allParenthesizations(xs.subList(0, i), op)
            val rs = allParenthesizations(xs.subList(i, xs.size), op)
            ls.flatMap { l -> rs.map { r -> op(l, r) } }
        }
    }