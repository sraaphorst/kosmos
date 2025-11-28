package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Algebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for an R-algebra A over a commutative ring R.
 *
 * An Algebra<R, A> is simultaneously:
 *  - a Ring<A> (ring structure on A)
 *  - an RModule<R, A> (module structure of A over the commutative ring R)
 *
 * AlgebraLaws composes:
 *  - RingLaws for the ring structure on A
 *  - RModuleLaws for the module structure (R ⊲ A)
 *  - extra compatibility axioms between scalar action and multiplication:
 *
 *    1. r · (a * b) = (r · a) * b
 *    2. r · (a * b) = a * (r · b)
 */
class AlgebraLaws<R : Any, A : Any>(
    private val algebra: Algebra<R, A>,
    private val scalarArb: Arb<R>,
    private val elemArb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val mulSymbol: String = "⋆",
    private val scalarSymbol: String = "·",
    private val addSymbol: String = "+"
) {

    fun laws(): List<TestingLaw> =
        RingLaws(
            ring = algebra,
            arb = elemArb,
            eq = eq,
            pr = pr,
            addSymbol = addSymbol,
            mulSymbol = mulSymbol
        ).laws() +
                RModuleLaws(
                    module = algebra,
                    scalarArb = scalarArb,
                    vectorArb = elemArb,
                    eq = eq,
                    pr = pr,
                    scalarSymbol = scalarSymbol,
                    addSymbol = addSymbol
                ).laws() +
                listOf(
                    scalarDistributesOverProductLeft(),
                    scalarDistributesOverProductRight()
                )

    /**
     * 1. r · (a * b) = (r · a) * b
     */
    private fun scalarDistributesOverProductLeft(): TestingLaw =
        object : TestingLaw {
            override val name: String =
                "algebra: scalar distributes over product (left) – r $scalarSymbol (a $mulSymbol b) = (r $scalarSymbol a) $mulSymbol b"

            override suspend fun test() {
                val mul = algebra.mul.op
                val act = algebra.action

                checkAll(scalarArb, Arb.pair(elemArb, elemArb)) { r, (a, b) ->
                    val left = act(r, mul(a, b))
                    val right = mul(act(r, a), b)

                    withClue(buildString {
                        appendLine("Scalar distributivity over product (left) failed:")
                        appendLine("  r $scalarSymbol (a $mulSymbol b)  vs  (r $scalarSymbol a) $mulSymbol b")
                        appendLine("  r  = $r")
                        appendLine("  a  = ${pr.render(a)}")
                        appendLine("  b  = ${pr.render(b)}")
                        appendLine("  LHS = ${pr.render(left)}")
                        appendLine("  RHS = ${pr.render(right)}")
                    }) {
                        check(eq.eqv(left, right))
                    }
                }
            }
        }

    /**
     * 2. r · (a * b) = a * (r · b)
     */
    private fun scalarDistributesOverProductRight(): TestingLaw =
        object : TestingLaw {
            override val name: String =
                "algebra: scalar distributes over product (right) – r $scalarSymbol (a $mulSymbol b) = a $mulSymbol (r $scalarSymbol b)"

            override suspend fun test() {
                val mul = algebra.mul.op
                val act = algebra.action

                checkAll(scalarArb, Arb.pair(elemArb, elemArb)) { r, (a, b) ->
                    val left = act(r, mul(a, b))
                    val right = mul(a, act(r, b))

                    withClue(buildString {
                        appendLine("Scalar distributivity over product (right) failed:")
                        appendLine("  r $scalarSymbol (a $mulSymbol b)  vs  a $mulSymbol (r $scalarSymbol b)")
                        appendLine("  r  = $r")
                        appendLine("  a  = ${pr.render(a)}")
                        appendLine("  b  = ${pr.render(b)}")
                        appendLine("  LHS = ${pr.render(left)}")
                        appendLine("  RHS = ${pr.render(right)}")
                    }) {
                        check(eq.eqv(left, right))
                    }
                }
            }
        }
}