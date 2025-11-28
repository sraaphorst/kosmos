package org.vorpal.kosmos.laws.algebra

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw

/**
 * Laws for a monoid homomorphism f: (A, ⋆) → (B, ·):
 *
 *  - f(a ⋆ b) = f(a) · f(b)
 *  - f(1_A) = 1_B
 */
class MonoidHomLaws<A : Any, B : Any>(
    private val domain: Monoid<A>,
    private val codomain: Monoid<B>,
    private val f: (A) -> B,
    private val pairArb: Arb<Pair<A, A>>,
    private val eqB: Eq<B>,
    private val prA: Printable<A> = default(),
    private val prB: Printable<B> = default(),
    private val symbolDomain: String = "⋆",
    private val symbolCodomain: String = "·"
) {

    fun laws(): List<TestingLaw> = listOf(
        // f(a ⋆ b) = f(a) · f(b)
        object : TestingLaw {
            override val name: String =
                "monoid homomorphism: multiplicative ($symbolDomain → $symbolCodomain)"

            override suspend fun test() {
                checkAll(pairArb) { (a, b) ->
                    val left = f(domain.op(a, b))
                    val right = codomain.op(f(a), f(b))

                    withClue(
                        buildString {
                            appendLine("Monoid hom multiplicativity failed:")
                            appendLine("a = ${prA.render(a)}, b = ${prA.render(b)}")
                            appendLine("f(a $symbolDomain b) = ${prB.render(left)}")
                            appendLine("f(a) $symbolCodomain f(b) = ${prB.render(right)}")
                        }
                    ) {
                        check(eqB.eqv(left, right))
                    }
                }
            }
        },

        // f(1_A) = 1_B
        object : TestingLaw {
            override val name: String =
                "monoid homomorphism: preserves identity"

            override suspend fun test() {
                val left = f(domain.identity)
                val right = codomain.identity

                withClue(
                    buildString {
                        appendLine("Monoid hom identity preservation failed:")
                        appendLine("f(1_A) = ${prB.render(left)}")
                        appendLine("1_B    = ${prB.render(right)}")
                    }
                ) {
                    check(eqB.eqv(left, right))
                }
            }
        }
    )
}