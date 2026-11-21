package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.algebra.structures.pow
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import org.vorpal.kosmos.laws.lawCheckAll
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * The algebraic functions on dual numbers over ℚ, checked exactly.
 *
 * Each closed-form function is compared with the same thing computed by the *ring*: `inv` with the
 * ring reciprocal, `pow` with repeated multiplication, a Möbius map with `(ax + b)·(cx + d)⁻¹`. The
 * closed forms are derivative formulas; the ring knows nothing about derivatives, so agreement is a
 * genuine check.
 */
class DualRationalFnsSpec : FunSpec({
    lawTests(lawGraph("dual rational functions") {
        val q = RationalAlgebras.RationalField
        val ring = DualAlgebras.dualRingWithReciprocal(q)
        val arb = ArbDual.rationalDual
        val arbUnit = ArbDual.unitRationalDual
        val arbQ = ArbDual.rational

        fun const(r: Rational): Dual<Rational> = DualAlgebras.constant(q, r)

        // ── inv ──────────────────────────────────────────────────────────────────

        local("inv implements (a + bε)⁻¹ = 1/a - (b/a²)ε, and is the ring reciprocal") {
            lawCheckAll(arbUnit) { x ->
                DualRationalFns.inv(x) shouldBe dual(x.f.reciprocal(), -x.df / (x.f * x.f))
                DualRationalFns.inv(x) shouldBe ring.reciprocal(x)
                ring.mul(x, DualRationalFns.inv(x)) shouldBe ring.one
                DualRationalFns.inv(DualRationalFns.inv(x)) shouldBe x
            }
        }

        local("inv rejects a zero primal part with ArithmeticException, whatever the tangent") {
            lawCheckAll(arbQ) { b ->
                shouldThrow<ArithmeticException> { DualRationalFns.inv(dual(Rational.ZERO, b)) }
            }
        }

        // ── pow ──────────────────────────────────────────────────────────────────

        local("pow with exponent 0 is one, for every x including 0") {
            lawCheckAll(arb) { x ->
                DualRationalFns.pow(x, 0) shouldBe ring.one
            }
            DualRationalFns.pow(ring.zero, 0) shouldBe ring.one
        }

        local("pow with a positive exponent is repeated multiplication in the ring") {
            lawCheckAll(arb, Arb.int(1..9)) { x, n ->
                DualRationalFns.pow(x, n) shouldBe ring.mul.pow(x, n)
                DualRationalFns.pow(x, n) shouldBe dual(x.f.pow(n), n.toRational() * x.df * x.f.pow(n - 1))
            }
        }

        local("pow with a negative exponent is the inverse of the positive power") {
            lawCheckAll(arbUnit, Arb.int(1..7)) { x, n ->
                DualRationalFns.pow(x, -n) shouldBe ring.reciprocal(ring.mul.pow(x, n))
                ring.mul(DualRationalFns.pow(x, -n), DualRationalFns.pow(x, n)) shouldBe ring.one
            }
            lawCheckAll(arbUnit) { x ->
                DualRationalFns.pow(x, -1) shouldBe DualRationalFns.inv(x)
            }
        }

        local("pow obeys the laws of exponents: xᵐ·xⁿ = xᵐ⁺ⁿ and (xᵐ)ⁿ = xᵐⁿ") {
            lawCheckAll(arbUnit, Arb.int(-4..4), Arb.int(-4..4)) { x, m, n ->
                ring.mul(DualRationalFns.pow(x, m), DualRationalFns.pow(x, n)) shouldBe DualRationalFns.pow(x, m + n)
                DualRationalFns.pow(DualRationalFns.pow(x, m), n) shouldBe DualRationalFns.pow(x, m * n)
            }
        }

        local("pow with a negative exponent rejects a zero primal part with ArithmeticException") {
            lawCheckAll(arbQ, Arb.int(1..5)) { b, n ->
                shouldThrow<ArithmeticException> { DualRationalFns.pow(dual(Rational.ZERO, b), -n) }
            }
        }

        local("pow of a nilpotent: (bε)¹ = bε and (bε)ⁿ = 0 for n ≥ 2") {
            lawCheckAll(arbQ, Arb.int(2..6)) { b, n ->
                val x = dual(Rational.ZERO, b)
                DualRationalFns.pow(x, 1) shouldBe x
                DualRationalFns.pow(x, n) shouldBe ring.zero
            }
        }

        local("pow rejects Int.MIN_VALUE, which cannot be negated") {
            shouldThrow<IllegalArgumentException> { DualRationalFns.pow(ring.one, Int.MIN_VALUE) }
        }

        // ── square and cube ──────────────────────────────────────────────────────

        local("square and cube are the ring products x·x and x·x·x") {
            lawCheckAll(arb) { x ->
                DualRationalFns.square(x) shouldBe ring.mul(x, x)
                DualRationalFns.square(x) shouldBe dual(x.f * x.f, Rational.of(2) * x.f * x.df)
                DualRationalFns.square(x) shouldBe DualRationalFns.pow(x, 2)

                DualRationalFns.cube(x) shouldBe ring.mul(x, ring.mul(x, x))
                DualRationalFns.cube(x) shouldBe dual(x.f * x.f * x.f, Rational.of(3) * x.f * x.f * x.df)
                DualRationalFns.cube(x) shouldBe DualRationalFns.pow(x, 3)
            }
        }

        // ── Möbius transformations ───────────────────────────────────────────────

        local("mobius is (ax + b)·(cx + d)⁻¹ computed in the ring") {
            lawCheckAll(arb, arbQ, arbQ, arbQ, arbQ) { x, a, b, c, d ->
                val denominator = ring.add(ring.mul(const(c), x), const(d))
                if (ring.hasReciprocal(denominator)) {
                    val numerator = ring.add(ring.mul(const(a), x), const(b))
                    DualRationalFns.mobius(x, a, b, c, d) shouldBe ring.mul(numerator, ring.reciprocal(denominator))
                } else {
                    shouldThrow<ArithmeticException> { DualRationalFns.mobius(x, a, b, c, d) }
                }
            }
        }

        local("mobius carries the derivative (ad - bc)/(cu + d)²") {
            lawCheckAll(arb, arbQ, arbQ, arbQ, arbQ) { x, a, b, c, d ->
                val denominator = c * x.f + d
                if (denominator != Rational.ZERO) {
                    DualRationalFns.mobius(x, a, b, c, d) shouldBe dual(
                        (a * x.f + b) / denominator,
                        x.df * (a * d - b * c) / (denominator * denominator)
                    )
                }
            }
        }

        local("special Möbius maps: the identity, the affine maps, and inversion") {
            val (zero, one) = Rational.ZERO to Rational.ONE
            lawCheckAll(arb, arbQ, arbQ) { x, a, b ->
                DualRationalFns.mobius(x, one, zero, zero, one) shouldBe x
                DualRationalFns.mobius(x, a, b, zero, one) shouldBe ring.add(ring.mul(const(a), x), const(b))
            }
            lawCheckAll(arbUnit) { x ->
                DualRationalFns.mobius(x, zero, one, one, zero) shouldBe DualRationalFns.inv(x)
            }
        }

        local("composing Möbius maps multiplies their matrices") {
            lawCheckAll(arb, arbQ, arbQ, arbQ, arbQ) { x, a, b, c, d ->
                // M₁ = [[a, b], [c, d]] and M₂ = [[2, 1], [1, 3]]; M₂ ∘ M₁ has matrix M₂·M₁.
                val (a2, b2, c2, d2) = listOf(2, 1, 1, 3).map(Rational::of)
                val inner = c * x.f + d
                if (inner != Rational.ZERO) {
                    val y = DualRationalFns.mobius(x, a, b, c, d)
                    val outer = c2 * y.f + d2
                    val composedDenominator = (c2 * a + d2 * c) * x.f + (c2 * b + d2 * d)
                    if (outer != Rational.ZERO && composedDenominator != Rational.ZERO) {
                        DualRationalFns.mobius(y, a2, b2, c2, d2) shouldBe DualRationalFns.mobius(
                            x,
                            a2 * a + b2 * c, a2 * b + b2 * d,
                            c2 * a + d2 * c, c2 * b + d2 * d
                        )
                    }
                }
            }
        }

        local("mobius rejects a zero denominator with ArithmeticException") {
            lawCheckAll(arb) { x ->
                shouldThrow<ArithmeticException> {
                    DualRationalFns.mobius(x, Rational.ONE, Rational.ZERO, Rational.ONE, -x.f)
                }
            }
        }
    })
})
