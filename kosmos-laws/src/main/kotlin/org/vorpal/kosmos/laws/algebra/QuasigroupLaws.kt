package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.arbitrary.pair
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Quasigroup
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.CancellativityLaw

/**
 * Laws for a Quasigroup:
 *
 *  1. Underlying magma is cancellative:
 *     - left cancellation:  c⋆a = c⋆b ⇒ a = b
 *     - right cancellation: a⋆c = b⋆c ⇒ a = b
 *
 *  2. Left division:
 *     - a ⋆ (a\b) = b
 *     - leftDiv(a, a⋆x) = x
 *
 *  3. Right division:
 *     - (b/a) ⋆ a = b
 *     - rightDiv(a⋆x, a) = x
 */
class QuasigroupLaws<A : Any>(
    private val quasigroup: Quasigroup<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = Printable.default(),
    private val symbol: String = "⋆"
) {

    fun laws(): List<TestingLaw> {
        val op = quasigroup.op

        val cancellative = CancellativityLaw(
            op = op,
            arb = arb,
            eq = eq,
            pr = pr,
            symbol = symbol
        )

        val leftDivForward = object : TestingLaw {
            override val name: String =
                "quasigroup left division compatibility: a $symbol (a\\\\b) = b"

            override suspend fun test() {
                checkAll(Arb.pair(arb, arb)) { (a, b) ->
                    val x = quasigroup.leftDiv(a, b)
                    val result = op(a, x)

                    withClue({
                        val sa = pr.render(a)
                        val sb = pr.render(b)
                        val sx = pr.render(x)
                        val sResult = pr.render(result)
                        buildString {
                            appendLine("Left-division compatibility failed:")
                            appendLine("$sa $symbol ( $sa \\\\ $sb ) = $sa $symbol $sx = $sResult")
                            appendLine("Expected: $sResult = $sb")
                        }
                    }) {
                        check(eq.eqv(result, b))
                    }
                }
            }
        }

        val leftDivBackward = object : TestingLaw {
            override val name: String =
                "quasigroup left division inversion: leftDiv(a, a $symbol x) = x"

            override suspend fun test() {
                checkAll(Arb.pair(arb, arb)) { (a, x) ->
                    val ax = op(a, x)
                    val recovered = quasigroup.leftDiv(a, ax)

                    withClue({
                        val sa = pr.render(a)
                        val sx = pr.render(x)
                        val sax = pr.render(ax)
                        val sRecovered = pr.render(recovered)
                        buildString {
                            appendLine("Left-division inversion failed:")
                            appendLine("leftDiv($sa, $sa $symbol $sx) = leftDiv($sa, $sax) = $sRecovered")
                            appendLine("Expected: $sRecovered = $sx")
                        }
                    }) {
                        check(eq.eqv(recovered, x))
                    }
                }
            }
        }

        val rightDivForward = object : TestingLaw {
            override val name: String =
                "quasigroup right division compatibility: (b/a) $symbol a = b"

            override suspend fun test() {
                checkAll(Arb.pair(arb, arb)) { (a, b) ->
                    val x = quasigroup.rightDiv(b, a)
                    val result = op(x, a)

                    withClue({
                        val sa = pr.render(a)
                        val sb = pr.render(b)
                        val sx = pr.render(x)
                        val sResult = pr.render(result)
                        buildString {
                            appendLine("Right-division compatibility failed:")
                            appendLine("( $sb / $sa ) $symbol $sa = $sx $symbol $sa = $sResult")
                            appendLine("Expected: $sResult = $sb")
                        }
                    }) {
                        check(eq.eqv(result, b))
                    }
                }
            }
        }

        val rightDivBackward = object : TestingLaw {
            override val name: String =
                "quasigroup right division inversion: rightDiv(a $symbol x, a) = x"

            override suspend fun test() {
                checkAll(Arb.pair(arb, arb)) { (a, x) ->
                    val ax = op(a, x)
                    val recovered = quasigroup.rightDiv(ax, a)

                    withClue({
                        val sa = pr.render(a)
                        val sx = pr.render(x)
                        val sax = pr.render(ax)
                        val sRecovered = pr.render(recovered)
                        buildString {
                            appendLine("Right-division inversion failed:")
                            appendLine("rightDiv($sa $symbol $sx, $sa) = rightDiv($sax, $sa) = $sRecovered")
                            appendLine("Expected: $sRecovered = $sx")
                        }
                    }) {
                        check(eq.eqv(recovered, x))
                    }
                }
            }
        }

        return listOf(
            cancellative,
            leftDivForward,
            leftDivBackward,
            rightDivForward,
            rightDivBackward
        )
    }
}