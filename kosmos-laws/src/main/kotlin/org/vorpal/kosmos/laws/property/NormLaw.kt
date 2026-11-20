package org.vorpal.kosmos.laws.property

import io.kotest.property.Arb
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.lawCheck
import org.vorpal.kosmos.laws.lawCheckAll

/**
 * Norm law for a map N: A → ℝ that is supposed to be a "norm squared":
 *
 *  - Non-negativity: N(a) ≥ 0 for all a.
 *  - Definiteness:   N(a) = 0  ⇔  a = 0.
 *
 * Multiplicativity (e.g. N(ab) = N(a)N(b)) can be tested separately via homomorphism laws.
 */
class NormLaw<A : Any>(
    private val normSq: UnaryOp<A, Real>,
    private val zero: A,
    private val arb: Arb<A>,
    private val tolerance: Real = 1e-10,
    private val eqReal: Eq<Real> = Eqs.realApprox(absTol = tolerance, relTol = tolerance),
    private val eqA: Eq<A> = Eq.default(),
    private val prA: Printable<A> = Printable.default(),
) : TestingLaw {

    override val name: String = "norm (non-negativity & definiteness)"

    override suspend fun test() {
        lawCheckAll(arb) { a ->
            val n = normSq(a)

            // 1. Non-negativity: N(a) ≥ 0 (up to tolerance).
            lawCheck(n >= -tolerance) {
                buildString {
                    appendLine("Norm non-negativity failed:")
                    appendLine("a    = ${prA(a)}")
                    appendLine("N(a) = $n")
                    appendLine("tolerance = $tolerance")
                }
            }

            // 2. a = 0 ⇒ N(a) ≈ 0
            if (eqA(a, zero)) {
                lawCheck(eqReal(n, 0.0)) {
                    buildString {
                        appendLine("Norm definiteness failed (zero ⇒ N(a) = 0):")
                        appendLine("a    = ${prA(a)} (zero)")
                        appendLine("N(a) = $n")
                    }
                }
            }

            // 3. N(a) ≈ 0 ⇒ a = 0
            if (eqReal(n, 0.0)) {
                lawCheck(eqA(a, zero)) {
                    buildString {
                        appendLine("Norm definiteness failed (N(a) = 0 ⇒ zero):")
                        appendLine("a    = ${prA(a)}")
                        appendLine("N(a) = 0 (within tolerance)")
                    }
                }
            }
        }
    }
}