package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.core.spec.style.FunSpec
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras.diffAtReal
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras.dualToRank2MatrixMonomorphism
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras.dualRing
import org.vorpal.kosmos.laws.homomorphism.RingHomomorphismLaws
import org.vorpal.kosmos.linear.instances.DenseMatAlgebras
import org.vorpal.kosmos.testutils.shouldBeApproximately
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

/**
 * Tests for [DualAlgebras] functions not covered by [DualCommutativeRingSpec]:
 * - [DualAlgebras.diffAt] / [DualAlgebras.diffAtReal]
 * - [DualAlgebras.dualToRank2MatrixMonomorphism]
 */
class DualAlgebrasSpec : FunSpec({

    val field = RealAlgebras.RealField
    val eqMat = DenseMatAlgebras.liftEq(RealAlgebras.eqRealApprox)
    val prMat = DenseMatAlgebras.liftPrintablePretty(RealAlgebras.printableRealPretty)
    val prDual = DualAlgebras.printableDualRealSigned

    val mono = dualToRank2MatrixMonomorphism(field)

    // Local operators so polynomial lambdas in diffAtReal tests compile.
    val dualRingForOps = RealAlgebras.RealField.dualRing()
    operator fun Dual<Real>.times(other: Dual<Real>): Dual<Real> = dualRingForOps.mul(this, other)
    operator fun Dual<Real>.plus(other: Dual<Real>): Dual<Real> = dualRingForOps.add(this, other)

    // ── diffAtReal ─────────────────────────────────────────────────────────────

    context("diffAtReal") {

        test("identity via log∘exp: f(x) = log(exp(x)), f'(x) = 1") {
            val (fVal, dfVal) = diffAtReal(
                f = { x -> DualRealFns.log(DualRealFns.exp(x)) },
                x = 1.5
            )
            fVal shouldBeApproximately 1.5
            dfVal shouldBeApproximately 1.0
        }

        test("quadratic: f(x) = x², f'(x) = 2x") {
            checkAll(arbDualReal) { x ->
                val (fVal, dfVal) = diffAtReal(
                    f = { d -> d * d },
                    x = x
                )
                fVal shouldBeApproximately (x * x)
                dfVal shouldBeApproximately (2.0 * x)
            }
        }

        test("cubic: f(x) = x³, f'(x) = 3x²") {
            checkAll(arbDualReal) { x ->
                val (fVal, dfVal) = diffAtReal(
                    f = { d -> d * d * d },
                    x = x
                )
                fVal shouldBeApproximately (x * x * x)
                dfVal shouldBeApproximately (3.0 * x * x)
            }
        }

        test("sin: f(x) = sin(x), f'(x) = cos(x)") {
            checkAll(arbDualReal) { x ->
                val (fVal, dfVal) = diffAtReal(
                    f = DualRealFns::sin,
                    x = x
                )
                fVal shouldBeApproximately sin(x)
                dfVal shouldBeApproximately cos(x)
            }
        }

        test("exp: f(x) = exp(x), f'(x) = exp(x)") {
            checkAll(arbDualReal) { x ->
                val (fVal, dfVal) = diffAtReal(
                    f = DualRealFns::exp,
                    x = x
                )
                fVal shouldBeApproximately exp(x)
                dfVal shouldBeApproximately exp(x)
            }
        }

        test("composition: f(x) = exp(sin(x)), f'(x) = cos(x)·exp(sin(x))") {
            checkAll(arbDualReal) { x ->
                val (fVal, dfVal) = diffAtReal(
                    f = { d -> DualRealFns.exp(DualRealFns.sin(d)) },
                    x = x
                )
                fVal shouldBeApproximately exp(sin(x))
                dfVal shouldBeApproximately (cos(x) * exp(sin(x)))
            }
        }

        test("returns pair (f(x), f'(x)) in the correct order") {
            // f(x) = x² at x=3: f=9, f'=6
            val result = diffAtReal(f = { d -> d * d }, x = 3.0)
            result.first shouldBeApproximately 9.0
            result.second shouldBeApproximately 6.0
        }
    }

    // ── dualToRank2MatrixMonomorphism ──────────────────────────────────────────

    context("dualToRank2MatrixMonomorphism") {

        context("satisfies Ring homomorphism laws") {
            test("preserves addition, additive identity, multiplication, and multiplicative identity") {
                RingHomomorphismLaws(
                    hom = { d: Dual<Real> -> mono.map(d) },
                    domain = field.dualRing(),
                    codomain = DenseMatAlgebras.DenseMatRing(field, 2),
                    arb = arbDual,
                    eqB = eqMat,
                    prA = prDual,
                    prB = prMat
                ).fullTest().throwIfFailed()
            }
        }

        context("matrix structure") {

            test("a + bε maps to [[a, b], [0, a]]") {
                checkAll(arbDualReal, arbDualReal) { a, b ->
                    val m = mono.map(Dual(a, b))
                    m[0, 0] shouldBeApproximately a
                    m[0, 1] shouldBeApproximately b
                    m[1, 0] shouldBeApproximately 0.0
                    m[1, 1] shouldBeApproximately a
                }
            }
        }

        context("monomorphism structural properties") {

            test("injective: φ(d1) = φ(d2) implies d1 = d2") {
                checkAll(arbDual, arbDual) { d1, d2 ->
                    val m1 = mono.map(d1)
                    val m2 = mono.map(d2)
                    if (eqMat(m1, m2)) {
                        d1.a shouldBeApproximately d2.a
                        d1.b shouldBeApproximately d2.b
                    }
                }
            }

            test("image of bε is nilpotent: φ(bε)² = 0") {
                val matRing = DenseMatAlgebras.DenseMatRing(field, 2)
                checkAll(arbDualReal) { b ->
                    val mEps = mono.map(Dual(field.zero, b))
                    val mEps2 = matRing.mul(mEps, mEps)

                    mEps2[0, 0] shouldBeApproximately 0.0
                    mEps2[0, 1] shouldBeApproximately 0.0
                    mEps2[1, 0] shouldBeApproximately 0.0
                    mEps2[1, 1] shouldBeApproximately 0.0
                }
            }
        }
    }
})
