//package org.vorpal.kosmos.laws.property
//
//import io.kotest.assertions.withClue
//import io.kotest.property.Arb
//import io.kotest.property.arbitrary.list
//import io.kotest.property.checkAll
//import org.vorpal.kosmos.testing.existsSample
//import org.vorpal.kosmos.core.Eq
//import org.vorpal.kosmos.core.ops.BinOp
//import org.vorpal.kosmos.core.render.Printable
//import org.vorpal.kosmos.laws.TestingLaw
//
///**
// * An operation is said to be nilpotent of index n ≥ 2 if the following hold:
// * * For any product over n elements (repeats allowed), the product is zero.
// * * There is some product over n-1 elements (repeats allowed) where the product is nonzero.
// *
// * It is possible for this law to fail even for a nilpotent operation if no n-1 elements can be
// * found that satisfy the second identity. The number of attempts to find such a list of elements
// * is given by `nonZeroAttempts`.
// */
//class NilpotencyLaw<A>(
//    val op: BinOp<A>,
//    val n: Int,
//    val zero: A,
//    val arb: Arb<A>,
//    val eq: Eq<A>,
//    val nonzeroAttempts: Int = 1000,
//    val pr: Printable<A> = Printable.default(),
//    val symbol: String = "⋆"
//) : TestingLaw {
//
//    init {
//        if (n < 2)
//            error("Nilpotent law requires n ≥ 2, but got $n")
//        if (nonzeroAttempts <= 0)
//            error("Nilpotent law requires nonzeroAttempts to be positive, but got $nonzeroAttempts")
//    }
//
//    override val name = "nilpotent (n=$n, $symbol)"
//
//    /* Check that there is a product of n-1 elements that is nonzero. */
//    private fun nonzeroProductCheck() {
//        val m = n - 1
//        val listArb = Arb.list(arb, m..m)
//
//        withClue("Nilpotency failed: no expression over $m elements found that is nonzero (after $nonzeroAttempts attempts).") {
//            val witness = existsSample(listArb, nonzeroAttempts) { xs ->
//                val prod = xs.reduce(op::combine)
//                !eq.eqv(prod, zero)
//            }
//            check(witness != null)
//        }
//    }
//
//    /* Check that all products of n elements is zero. */
//    private suspend fun zeroProductCheck() {
//        val listArb = Arb.list(arb, n..n)
//        checkAll(listArb) { lst ->
//            val prod = lst.reduce(op::combine)
//            withClue(zeroProductCheckFailed(lst, prod)) {
//                check(eq.eqv(prod, zero))
//            }
//        }
//    }
//
//    private fun zeroProductCheckFailed(lst: List<A>, prod: A): () -> String = {
//        val expression = lst.joinToString(" $symbol ", transform = pr::render)
//        val sProd = pr.render(prod)
//        val sZero = pr.render(zero)
//
//        buildString {
//            appendLine("Nilpotent product check failed:")
//            appendLine("$expression = $sProd (expected: $sZero)")
//        }
//    }
//
//    override suspend fun test() {
//        nonzeroProductCheck()
//        zeroProductCheck()
//    }
//}

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
class NilpotentLaw<A>(
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
            ls.flatMap { l -> rs.map { r -> op.combine(l, r) } }
        }
    }