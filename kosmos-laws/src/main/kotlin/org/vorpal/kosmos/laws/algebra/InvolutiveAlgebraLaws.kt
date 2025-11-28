package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.InvolutiveAlgebra
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.property.InvolutionLaw

class InvolutiveAlgebraLaws<A : Any>(
    private val algebra: InvolutiveAlgebra<A>,
    private val arb: Arb<A>,
    private val eq: Eq<A>,
    private val pr: Printable<A> = default(),
    private val addSymbol: String = "+",
    private val mulSymbol: String = "⋅",
    private val starSymbol: String = "*",
) {

    fun laws(): List<TestingLaw> =
        listOf(
            // 1. Involution: (a*)* = a
            InvolutionLaw(
                op = algebra.conj,
                arb = arb,
                eq = eq,
                pr = pr,
                symbol = starSymbol
            ),

            // 2. Additivity: (a + b)* = a* + b*
            object : TestingLaw {
                override val name: String =
                    "conjugation is additive: (a $addSymbol b)$starSymbol = a$starSymbol $addSymbol b$starSymbol"

                override suspend fun test() {
                    checkAll(arb, arb) { a, b ->
                        val left  = algebra.conj(algebra.add.op(a, b))
                        val right = algebra.add.op(algebra.conj(a), algebra.conj(b))

                        withClue("conj(a $addSymbol b) vs conj(a) $addSymbol conj(b)") {
                            check(eq.eqv(left, right))
                        }
                    }
                }
            },

            // 3. Anti-multiplicativity: (ab)* = b* a*
            object : TestingLaw {
                override val name: String =
                    "conjugation is anti-multiplicative: (ab)$starSymbol = b$starSymbol $mulSymbol a$starSymbol"

                override suspend fun test() {
                    checkAll(arb, arb) { a, b ->
                        val left  = algebra.conj(algebra.mul.op(a, b))
                        val right = algebra.mul.op(algebra.conj(b), algebra.conj(a))

                        withClue("conj(a $mulSymbol b) vs conj(b) $mulSymbol conj(a)") {
                            check(eq.eqv(left, right))
                        }
                    }
                }
            },

            // 4. Unit fixed: 1* = 1
            object : TestingLaw {
                override val name: String =
                    "conjugation preserves unit: 1$starSymbol = 1"

                override suspend fun test() {
                    val left  = algebra.conj(algebra.mul.identity)
                    val right = algebra.mul.identity

                    withClue("conj(1) should equal 1") {
                        check(eq.eqv(left, right))
                    }
                }
            }
        )
}