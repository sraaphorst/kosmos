package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith


/**
 * Anti-Homomorphism Law:
 *
 * `f` is a homomorphism from `(A, opA)` into the opposite semigroup of `(B, opB)` such that:
 *
 *    f(a * b) = f(b) * f(a)
 */
class AntiHomomorphismLaw<A : Any, B : Any>(
    private val f: UnaryOp<A, B>,
    private val opA: BinOp<A>,
    private val opB: BinOp<B>,
    private val arbA: Arb<A>,
    private val eqB: Eq<B> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
    private val prB: Printable<B> = Printable.default()
): TestingLaw {
    override val name = "anti-homomorphism (${f.symbol}, ${opA.symbol}, ${opB.symbol})"
    private fun exprF(param: String) = "${f.symbol}($param)"
    private fun exprA(left: String, right: String) = "$left ${opA.symbol} $right"
    private fun exprB(left: String, right: String) = "$left ${opB.symbol} $right"

    private suspend fun antiHomomorphismCheck() {
        lawCheckAll(TestingLaw.arbPair(arbA), render = renderWith(prA)) { (a, b) ->
            val ab = opA(a, b)
            val fab = f(ab)
            val fa = f(a)
            val fb = f(b)
            val fbfa = opB(fb, fa)
            lawCheck(eqB(fab, fbfa), antiHomomorphismFail(a, b, ab, fab, fa, fb, fbfa))
        }
    }

    private fun antiHomomorphismFail(
        a: A, b: A, ab: A,
        fab: B, fa: B, fb: B, fbfa: B
    ): () -> String = {
        buildString {
            val pa = prA(a)
            val pb = prA(b)
            val pab = prA(ab)
            val pfab = prB(fab)
            val pfa = prB(fa)
            val pfb = prB(fb)
            val pfbfa = prB(fbfa)

            appendLine("Anti-homomorphism law failed:")
            append(exprF(exprA(pa, pb)))
            append(" = ")
            append(exprF(pab))
            append(" = ")
            appendLine(pfab)

            append(exprB(exprF(pb), exprF(pa)))
            append(" = ")
            append(exprB(pfb, pfa))
            append(" = ")
            appendLine(pfbfa)

            appendLine("Expected: $pfab = $pfbfa")
        }
    }

    override suspend fun test() =
        antiHomomorphismCheck()
}
