package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll

import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

/** In universal algebras, entropic algebras have basic operations that mutually commute.
 * When there is only one binary operation, this reduces to the medial law. */
typealias EntropicLaw<A> = MedialLaw<A>

/** The bisymmetric law is a synonym for the same single-op-law. */
typealias BisymmetricLaw<A> = MedialLaw<A>

/**
 * Medial (entropic) law:
 *
 *    (a ∘ b) ∘ (c ∘ d) = (a ∘ c) ∘ (b ∘ d)
 *
 * This law should be applied to certify an entropic / medial magma (groupoid), e.g. in some
 * quasigroup / quandle flavors or specialized algebraic systems.
 *
 * Note that it is generally not true for arbitrary idempotent / commutative quasigroups, so it is
 * useful to distinguish them from entropic ones.
 *
 * Any medial quasigroup is affine over an abelian group (Toyoda–Bruck theorem): there exists an
 * abelian group `(A, +)`, commuting automorphisms `α, β ∈ Aut(A)`, and a constant `c ∈ A` such that
 *
 *     x ∘ y = α(x) + β(y) + c
 *
 * where `+` is the abelian group operation.
 * 
 * In particular, mediality forces very strong linear/affine behaviour.
 */
class MedialLaw<A : Any>(
    private val op: BinOp<A>,
    arb: Arb<A>,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {
    private val arb = TestingLaw.arbPair(TestingLaw.arbPair(arb)) 
    override val name = "medial (${op.symbol})"

    override suspend fun test() {
        checkAll(arb) { (abPair, cdPair) ->
            val (a, b) = abPair
            val (c, d) = cdPair

            val ab = op(a, b)
            val cd = op(c, d)
            val left = op(ab, cd)

            val ac = op(a, c)
            val bd = op(b, d)
            val right = op(ac, bd)

            withClue(failureMessage(a, b, c, d, ab, cd, ac, bd, left, right)) {
                check(eq(left, right))
            }
        }
    }

    private fun failureMessage(
        a: A, b: A, c: A, d: A,
        ab: A, cd: A, ac: A, bd: A,
        left: A, right: A
    ): () -> String = {
        fun expr(left: String, right: String) = "$left ${op.symbol} $right"

        buildString {
            val sa = pr(a)
            val sb = pr(b)
            val sc = pr(c)
            val sd = pr(d)

            val sab = pr(ab)
            val scd = pr(cd)
            val sLeft = pr(left)

            val sac = pr(ac)
            val sbd = pr(bd)
            val sRight = pr(right)
            
            appendLine("Medial law failed:")

            append(expr("(" + expr(sa, sb) + ")", "(" + expr(sc, sd) + ")"))
            append(" = ")
            append(expr(sab, scd))
            append(" = ")
            append(sLeft)
            appendLine()

            append(expr("(" + expr(sa, sc) + ")", "(" + expr(sb, sd) + ")"))
            append(" = ")
            append(expr(sac, sbd))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}
