package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
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
 * The medial law comes from semigroup / quasigroup literature.
 * It is strictly stronger than commutativity (i.e. it implies commutativity), but in an associative and
 * commutative magma (e.g. a commutative semigroup), the medial identity holds automatically:
 * (a + b) + (c + d) = a + b + c + d = a + c + b + d = (a + c) + (b + d).
 * This law should be applied to certify an entropic / medial magma (groupoid), e.g. in some quasigroup/quandle flavors
 * or specialized algebraic systems.
 * Note that it is generally not true for arbitrary idempotent / commutative quasigroups, so it is useful to distinguish
 * them from entropic ones.
 * Any finite entropic quasigroup is affine over an abelian group, i.e. you can represent the operation as:
 *     xy = (x) + (y)
 * where + is addition in an abelian group and
 */
class MedialLaw<A>(
    private val op: BinOp<A>,
    private val arb: Arb<Pair<Pair<A, A>, Pair<A, A>>>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) : TestingLaw {

    /** Convenience constructor from an element generator. */
    constructor(
        op: BinOp<A>,
        elemArb: Arb<A>,
        eq: Eq<A>,
        pr: Printable<A> = Printable.default(),
        symbol: String = "⋆"
    ) : this(op, Arb.pair(Arb.pair(elemArb, elemArb), Arb.pair(elemArb, elemArb)), eq, pr, symbol)

    override val name = "medial ($symbol)"

    override suspend fun test() {
        checkAll(arb) { (abPair, cdPair) ->
            val (a, b) = abPair
            val (c, d) = cdPair

            val ab = op.combine(a, b)
            val cd = op.combine(c, d)
            val left = op.combine(ab, cd)

            val ac = op.combine(a, c)
            val bd = op.combine(b, d)
            val right = op.combine(ac, bd)

            withClue(failureMessage(a, b, c, d, ab, cd, ac, bd, left, right)) {
                check(eq.eqv(left, right))
            }
        }
    }

    private fun failureMessage(
        a: A, b: A, c: A, d: A,
        ab: A, cd: A, ac: A, bd: A,
        left: A, right: A
    ): () -> String = {
        fun infix(l: String, r: String) = "$l $symbol $r"

        val sa = pr.render(a)
        val sb = pr.render(b)
        val sc = pr.render(c)
        val sd = pr.render(d)

        val sab = pr.render(ab)
        val scd = pr.render(cd)
        val sLeft = pr.render(left)

        val sac = pr.render(ac)
        val sbd = pr.render(bd)
        val sRight = pr.render(right)

        buildString {
            appendLine("Medial law failed:")

            append(infix("(" + infix(sa, sb) + ")", "(" + infix(sc, sd) + ")"))
            append(" = ")
            append(infix(sab, scd))
            append(" = ")
            append(sLeft)
            appendLine()

            append(infix("(" + infix(sa, sc) + ")", "(" + infix(sb, sd) + ")"))
            append(" = ")
            append(infix(sac, sbd))
            append(" = ")
            append(sRight)
            appendLine()

            append("Expected: $sLeft = $sRight")
            appendLine()
        }
    }
}
