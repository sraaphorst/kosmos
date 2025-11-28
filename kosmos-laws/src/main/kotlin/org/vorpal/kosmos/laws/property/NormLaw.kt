package org.vorpal.kosmos.laws.property

import io.kotest.assertions.withClue
import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.core.render.Printable.Companion.default
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.algebra.structures.instances.Real

/**
 * Norm law for a map N: A → ℝ that is supposed to be a "norm squared":
 *
 *  - Non-negativity: N(a) ≥ 0 for all a.
 *  - Definiteness:   N(a) = 0  ⇔  a = 0.
 *
 * You can think of this as the "order + definiteness" part;
 * multiplicativity is tested separately via MonoidHomLaws.
 *
 * Note: `Real` is typealias `Double`.
 */
class NormLaw<A : Any>(
    private val normSq: (A) -> Real,
    private val zero: A,
    private val arb: Arb<A>,
    private val eqReal: Eq<Real>,
    private val eqA: Eq<A>,
    private val prA: Printable<A> = default(),
    private val tolerance: Real = 1e-10
) : TestingLaw {

    override val name: String = "norm (non-negativity & definiteness)"

    override suspend fun test() {
        checkAll(arb) { a ->
            val n = normSq(a)

            // 1. Non-negativity: N(a) ≥ 0 (up to tolerance).
            withClue(
                buildString {
                    appendLine("Norm non-negativity failed:")
                    appendLine("a  = ${prA.render(a)}")
                    appendLine("N(a) = $n")
                }
            ) {
                check(n >= -tolerance)
            }

            // 2. a = 0 ⇒ N(a) = 0
            if (eqA.eqv(a, zero)) {
                withClue(
                    buildString {
                        appendLine("Norm definiteness failed (zero ⇒ N(a) = 0):")
                        appendLine("a  = ${prA.render(a)} (zero)")
                        appendLine("N(a) = $n")
                    }
                ) {
                    check(eqReal.eqv(n, 0.0))
                }
            }

            // 3. N(a) = 0 ⇒ a = 0
            if (eqReal.eqv(n, 0.0)) {
                withClue(
                    buildString {
                        appendLine("Norm definiteness failed (N(a) = 0 ⇒ zero):")
                        appendLine("a  = ${prA.render(a)}")
                        appendLine("N(a) = 0 (within tolerance)")
                    }
                ) {
                    check(eqA.eqv(a, zero))
                }
            }
        }
    }
}