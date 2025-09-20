import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw

class PowerAssociativityLaw<A>(
    private val op: BinOp<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆",
    private val maxExp: Int = 4
) : TestingLaw {

    override val name = "power-associativity ($symbol)"

    override suspend fun test() {

        checkAll(arb) { x ->
            // Degree-3 witness
            // (xx)x == x(xx)
            val xx = op.combine(x, x)
            val l3 = op.combine(xx, x)
            val r3 = op.combine(x, xx)
            withClue(paFail("degree-3", x, l3, r3)) {
                check(eq.eqv(l3, r3))
            }

            // Degree-4 witness
            // ((xx)x)x
            val l4 = op.combine(l3, x)
            // x(x(xx))
            val r4 = op.combine(x, r3)
            withClue(paFail("degree-4", x, l4, r4)) {
                check(eq.eqv(l4, r4))
            }
        }

        // (x^m)(x^n) == x^(m+n) for 1 ≤ m, n ≤ maxExp, with left-normed powers
        val exps = (1..maxExp).toList()
        checkAll(arb) { x ->
            for (m in exps) for (n in exps) {
                val xm   = powLeft(op, x, m)
                val xn   = powLeft(op, x, n)
                val prod = op.combine(xm, xn)
                val xmn  = powLeft(op, x, m + n)
                withClue(paExpFail(x, m, n, prod, xmn)) {
                    check(eq.eqv(prod, xmn))
                }
            }
        }
    }

    private fun <A> powLeft(op: BinOp<A>, x: A, n: Int): A =
        when {
            n < 1  -> error("powLeft expects n ≥ 1 (got $n)")
            else   -> (2..n).fold(x) { acc, _ -> op.combine(acc, x) }
        }

    private fun paFail(tag: String, x: A, left: A, right: A): () -> String = {
        val sx = pr.render(x); val sl = pr.render(left); val sr = pr.render(right)
        buildString {
            appendLine("Power-associativity $tag failed:")
            appendLine("x = $sx")
            appendLine("LHS = $sl")
            appendLine("RHS = $sr")
            appendLine("Expected: LHS = RHS")
        }
    }

    private fun paExpFail(x: A, m: Int, n: Int, prod: A, xmn: A): () -> String = {
        val sx = pr.render(x); val sp = pr.render(prod); val sxmn = pr.render(xmn)
        buildString {
            appendLine("Power-associativity exponent law failed:")
            appendLine("(x^$m) $symbol (x^$n) vs x^${m+n}, with x=$sx")
            appendLine("LHS = $sp")
            appendLine("RHS = $sxmn")
            appendLine("Expected: LHS = RHS")
        }
    }
}