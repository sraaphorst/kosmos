import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.renderWith

/**
 * Power-associativity:
 *
 * For each `x`, powers `x^n` are well-defined and satisfy:
 *
 *    (x^m)(x^n) = x^(m+n),  for all m, n ≥ 1.
 *
 * This is a very weak form of associativity in, e.g., the octonions.
 */
class PowerAssociativityLaw<A: Any>(
    private val op: BinOp<A>,
    private val arb: Arb<A>,
    private val maxExp: Int = 4,
    private val eq: Eq<A> = Eq.default(),
    private val pr: Printable<A> = Printable.default(),
) : TestingLaw {

    init {
        require(maxExp >= 1) { "PowerAssociativityLaw requires maxExp ≥ 1 (got $maxExp)" }
    }

    override val name = "power-associativity (${op.symbol})"

    override suspend fun test() {

        lawCheckAll(arb, render = renderWith(pr)) { x ->
            // Degree-3 witness
            // (xx)x == x(xx)
            val xx = op(x, x)
            val l3 = op(xx, x)
            val r3 = op(x, xx)
            lawCheck(eq(l3, r3), paFail("degree-3", x, l3, r3))

            // Degree-4 witness
            // ((xx)x)x
            val l4 = op(l3, x)
            // x(x(xx))
            val r4 = op(x, r3)
            lawCheck(eq(l4, r4), paFail("degree-4", x, l4, r4))
        }

        // (x^m)(x^n) == x^(m+n) for 1 ≤ m, n ≤ maxExp, with left-normed powers
        val exps = (1..maxExp).toList()
        lawCheckAll(arb, render = renderWith(pr)) { x ->
            for (m in exps)
                for (n in exps) {
                    val xm = powLeft(x, m)
                    val xn = powLeft(x, n)
                    val prod = op(xm, xn)
                    val xmn = powLeft(x, m + n)
                    lawCheck(eq(prod, xmn), paExpFail(x, m, n, prod, xmn))
                }
        }
    }

    private fun powLeft(x: A, n: Int): A =
        when {
            n < 1  -> error("powLeft expects n ≥ 1 (got $n)")
            else   -> (2..n).fold(x) { acc, _ -> op(acc, x) }
        }

    private fun paFail(tag: String, x: A, left: A, right: A): () -> String = {
        buildString {
            val sx = pr(x)
            val sl = pr(left)
            val sr = pr(right)
            appendLine("Power-associativity $tag failed:")
            appendLine("x = $sx")
            appendLine("LHS = $sl")
            appendLine("RHS = $sr")
            appendLine("Expected: LHS = RHS")
        }
    }

    private fun paExpFail(x: A, m: Int, n: Int, prod: A, xmn: A): () -> String = {
        buildString {
            val sx = pr(x)
            val sp = pr(prod)
            val sxmn = pr(xmn)
            appendLine("Power-associativity exponent law failed:")
            appendLine("(x^$m) ${op.symbol} (x^$n) vs x^${m+n}, with x=$sx")
            appendLine("LHS = $sp")
            appendLine("RHS = $sxmn")
            appendLine("Expected: LHS = RHS")
        }
    }
}