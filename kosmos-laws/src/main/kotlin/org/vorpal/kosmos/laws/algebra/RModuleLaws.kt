package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for an R-module M over a commutative ring R.
 *
 * We assume:
 *  - `module.ring` is a commutative ring (tested separately via CommutativeRingLaws if desired)
 *  - `module.group` is an abelian group (tested separately via AbelianGroupLaws)
 *
 * Here we only check the *interaction* axioms:
 *
 *  1. r · (x + y) = r · x + r · y
 *  2. (r + s) · x = r · x + s · x
 *  3. (r * s) · x = r · (s · x)
 *  4. 1_R · x = x
 */
class RModuleLaws<R : Any, M : Any>(
    private val module: RModule<R, M>,
    private val scalarArb: Arb<R>,
    private val vectorArb: Arb<M>,
    private val eq: Eq<M>,
    private val pr: Printable<M> = default(),
    private val scalarSymbol: String = "⋅",
    private val addSymbol: String = "+"
) {

    fun laws(): List<TestingLaw> =
        listOf(
            scalarDistributesOverVectorAddition(),
            scalarAdditionDistributesOverVector(),
            scalarMultiplicationAssociativity(),
            unitActsAsIdentity()
        )

    /**
     * 1. r · (x + y) = r · x + r · y
     */
    private fun scalarDistributesOverVectorAddition(): TestingLaw =
        object : TestingLaw {
            override val name: String =
                "module: scalar distributes over vector addition"

            override suspend fun test() {
                val add = module.group.op
                val act = module.action

                checkAll(scalarArb, Arb.pair(vectorArb, vectorArb)) { r, (x, y) ->
                    val left = act(r, add(x, y))
                    val right = add(act(r, x), act(r, y))

                    withClue(buildString {
                        appendLine("Scalar distributivity over vector addition failed:")
                        appendLine("  r $scalarSymbol (x $addSymbol y)  vs  r $scalarSymbol x  $addSymbol  r $scalarSymbol y")
                        appendLine("  r  = $r")
                        appendLine("  x  = ${pr.render(x)}")
                        appendLine("  y  = ${pr.render(y)}")
                        appendLine("  LHS = ${pr.render(left)}")
                        appendLine("  RHS = ${pr.render(right)}")
                    }) {
                        check(eq.eqv(left, right))
                    }
                }
            }
        }

    /**
     * 2. (r + s) · x = r · x + s · x
     */
    private fun scalarAdditionDistributesOverVector(): TestingLaw =
        object : TestingLaw {
            override val name: String =
                "module: scalar addition distributes over scalar action"

            override suspend fun test() {
                val addR = module.ring.add.op
                val addM = module.group.op
                val act = module.action

                checkAll(Arb.pair(scalarArb, scalarArb), vectorArb) { (r, s), x ->
                    val left = act(addR(r, s), x)
                    val right = addM(act(r, x), act(s, x))

                    withClue(buildString {
                        appendLine("Scalar addition distributivity failed:")
                        appendLine("  (r + s) $scalarSymbol x  vs  r $scalarSymbol x  $addSymbol  s $scalarSymbol x")
                        appendLine("  r  = $r")
                        appendLine("  s  = $s")
                        appendLine("  x  = ${pr.render(x)}")
                        appendLine("  LHS = ${pr.render(left)}")
                        appendLine("  RHS = ${pr.render(right)}")
                    }) {
                        check(eq.eqv(left, right))
                    }
                }
            }
        }

    /**
     * 3. (r * s) · x = r · (s · x)
     */
    private fun scalarMultiplicationAssociativity(): TestingLaw =
        object : TestingLaw {
            override val name: String =
                "module: associativity with ring multiplication"

            override suspend fun test() {
                val mulR = module.ring.mul.op
                val act = module.action

                checkAll(Arb.pair(scalarArb, scalarArb), vectorArb) { (r, s), x ->
                    val left = act(mulR(r, s), x)
                    val right = act(r, act(s, x))

                    withClue(buildString {
                        appendLine("Module associativity with ring multiplication failed:")
                        appendLine("  (r * s) $scalarSymbol x  vs  r $scalarSymbol (s $scalarSymbol x)")
                        appendLine("  r  = $r")
                        appendLine("  s  = $s")
                        appendLine("  x  = ${pr.render(x)}")
                        appendLine("  LHS = ${pr.render(left)}")
                        appendLine("  RHS = ${pr.render(right)}")
                    }) {
                        check(eq.eqv(left, right))
                    }
                }
            }
        }

    /**
     * 4. 1_R · x = x
     */
    private fun unitActsAsIdentity(): TestingLaw =
        object : TestingLaw {
            override val name: String =
                "module: unit of ring acts as identity"

            override suspend fun test() {
                val oneR = module.ring.mul.identity
                val act = module.action

                checkAll(vectorArb) { x ->
                    val result = act(oneR, x)

                    withClue(buildString {
                        appendLine("Module unit law failed:")
                        appendLine("  1_R $scalarSymbol x = x")
                        appendLine("  x      = ${pr.render(x)}")
                        appendLine("  result = ${pr.render(result)}")
                    }) {
                        check(eq.eqv(result, x))
                    }
                }
            }
        }
}