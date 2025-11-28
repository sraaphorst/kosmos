package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for a *-algebra (StarAlgebra) over a commutative ring R.
 *
 * A StarAlgebra<R, A> is:
 *  - an Algebra<R, A>  (R-module + ring structure on A)
 *  - an InvolutiveRing<A> (ring with an involution a ↦ a*)
 *
 * StarAlgebraLaws composes:
 *  - AlgebraLaws for (R, A)
 *  - InvolutiveRingLaws for A
 *  - extra compatibility of the involution with the scalar action:
 *
 *      conj(r · a) = r · conj(a)
 *
 *   (We are implicitly assuming the scalar ring R has trivial involution.)
 */
class StarAlgebraLaws<R : Any, A : Any>(
    private val starAlgebra: StarAlgebra<R, A>,
    private val scalarArb: Arb<R>,
    private val elemArb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋆",
    private val scalarSymbol: String = "·",
    private val starSymbol: String = "*"
) {

    fun laws(): List<TestingLaw> =
        AlgebraLaws(
            algebra = starAlgebra,
            scalarArb = scalarArb,
            elemArb = elemArb,
            eq = eq,
            pr = pr,
            mulSymbol = mulSymbol,
            scalarSymbol = scalarSymbol,
            addSymbol = addSymbol
        ).laws() +
                InvolutiveRingLaws(
                    ring = starAlgebra,
                    arb = elemArb,
                    eq = eq,
                    pr = pr,
                    addSymbol = addSymbol,
                    mulSymbol = mulSymbol,
                    starSymbol = starSymbol
                ).laws() +
                listOf(
                    scalarCompatibleWithInvolution()
                )

    /**
     * Compatibility of * with scalar action:
     *
     *   conj(r · a) = r · conj(a)
     *
     * for all r in R, a in A.
     */
    private fun scalarCompatibleWithInvolution(): TestingLaw =
        object : TestingLaw {
            override val name: String =
                "star algebra: conj respects scalar action – (r $scalarSymbol a)$starSymbol = r $scalarSymbol (a$starSymbol)"

            override suspend fun test() {
                val act = starAlgebra.action
                val conj = starAlgebra.conj

                checkAll(scalarArb, elemArb) { r, a ->
                    val left = conj(act(r, a))
                    val right = act(r, conj(a))

                    withClue(buildString {
                        appendLine("Compatibility of scalar action with involution failed:")
                        appendLine("  (r $scalarSymbol a)$starSymbol  vs  r $scalarSymbol (a$starSymbol)")
                        appendLine("  r   = $r")
                        appendLine("  a   = ${pr.render(a)}")
                        appendLine("  LHS = ${pr.render(left)}")
                        appendLine("  RHS = ${pr.render(right)}")
                    }) {
                        check(eq.eqv(left, right))
                    }
                }
            }
        }
}