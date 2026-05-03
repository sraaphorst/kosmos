package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.render.Printables
import org.vorpal.kosmos.hypercomplex.dual.DualAlgebras.dualRing
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.testutils.shouldBeApproximately

/**
 * Property tests for [DualModules.scalarExtension].
 *
 * Test base: `R` viewed as a 1-dimensional [VectorSpace] over itself.
 * The resulting module is `Dual(R) ⊳ DualVector(R)`, the canonical
 * setting for autodiff with dual numbers — `(p, v)` represents `p + εv`,
 * with `p` the value and `v` the tangent direction.
 *
 * The structural module axioms (left distributivity over vector addition,
 * compatibility with scalar addition and multiplication, unit action) are
 * delegated to [RModuleLaws]. The full law suite additionally verifies the
 * scalar CommutativeRing and the underlying AbelianGroup on vectors.
 *
 * The remaining tests cover:
 *  - the [DualVector] carrier (construction, destructuring, identities);
 *  - component-wise addition and negation;
 *  - the explicit action formula `(a + bε) ⊳ (x, y) = (ax, ay + bx)`;
 *  - special-case actions (lifted scalars, pure infinitesimals, ε itself);
 *  - module-level epsilon nilpotency: `ε ⊳ (ε ⊳ v) = 0`;
 *  - the primal/tangent embeddings and how ε and `(1 + ε)` move between them.
 */
class DualModulesSpec : FunSpec({

    // ── Test base: R as a 1-D vector space over itself ─────────────────────

    val realField = RealAlgebras.RealField

    val realVS: VectorSpace<Real, Real> = VectorSpace.of(
        scalars = realField,
        add = realField.add,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { r, x -> r * x }
    )

    val dualScalars = realField.dualRing()
    val module = DualModules.scalarExtension(realVS, dualScalars)

    val zeroVec = module.add.identity
    val oneScalar = module.scalars.mul.identity
    val zeroScalar = module.scalars.add.identity
    val arbVec = arbDualVector(arbDualReal)
    val eqVec = eqDualVector(RealAlgebras.eqRealApprox)
    val prVec = printableDualVector(Printables.real)

    val eqDual = DualAlgebras.eqDualReal
    val prDual = DualAlgebras.printableDualRealSigned

    // ── Convenience constructors and operators ─────────────────────────────

    fun dv(p: Real, t: Real): DualVector<Real> = DualVector(p, t)
    fun act(s: Dual<Real>, v: DualVector<Real>): DualVector<Real> = module.leftAction(s, v)

    // ── Structural module axioms via the law suite ─────────────────────────
    //
    // RModuleLaws.fullLaws() covers:
    //  - CommutativeRingLaws on the scalars (Dual<Real>)
    //  - AbelianGroupLaws on the vectors (DualVector<Real>)
    //  - the four module axioms:
    //      r ⊳ (x + y) = r ⊳ x + r ⊳ y
    //      (r + s) ⊳ x = r ⊳ x + s ⊳ x
    //      (r * s) ⊳ x = r ⊳ (s ⊳ x)
    //      1 ⊳ x = x

    context("R-module laws") {
        test("scalarExtension(R, Dual(R)) satisfies RModuleLaws") {
            RModuleLaws(
                module = module,
                scalarArb = arbDual,
                vectorArb = arbVec,
                eqR = eqDual,
                eqM = eqVec,
                prR = prDual,
                prM = prVec
            ).fullTest().throwIfFailed()
        }
    }

    // ── DualVector carrier ─────────────────────────────────────────────────

    context("DualVector construction") {

        test("DualVector stores components correctly") {
            checkAll(arbDualReal, arbDualReal) { p, t ->
                val v = dv(p, t)
                v.primal shouldBe p
                v.tangent shouldBe t
            }
        }

        test("component destructuring works") {
            checkAll(arbDualReal, arbDualReal) { p, t ->
                val v = dv(p, t)
                val (a, b) = v
                a shouldBe p
                b shouldBe t
            }
        }

        test("module zero is (0, 0)") {
            zeroVec.primal shouldBe 0.0
            zeroVec.tangent shouldBe 0.0
        }
    }

    // ── Component-wise addition and negation ───────────────────────────────

    context("Module addition is component-wise") {

        test("(p1, t1) + (p2, t2) = (p1+p2, t1+t2)") {
            checkAll(arbDualReal, arbDualReal, arbDualReal, arbDualReal) { p1, t1, p2, t2 ->
                val sum = module.add(dv(p1, t1), dv(p2, t2))
                sum.primal shouldBeApproximately (p1 + p2)
                sum.tangent shouldBeApproximately (t1 + t2)
            }
        }

        test("-(p, t) = (-p, -t)") {
            checkAll(arbDualReal, arbDualReal) { p, t ->
                val neg = module.add.inverse(dv(p, t))
                neg.primal shouldBeApproximately -p
                neg.tangent shouldBeApproximately -t
            }
        }
    }

    // ── The action formula (heart of the construction) ─────────────────────
    //
    //   (a + bε) ⊳ (x, y) = (ax, ay + bx)
    //
    // (x, y) corresponds to x + εy in R[ε]/(ε²), and
    //   (a + bε)(x + εy) = ax + ε(ay + bx)
    // since ε² = 0.

    context("Scalar action formula") {

        test("(a + bε) ⊳ (x, y) = (ax, ay + bx)") {
            checkAll(arbDualReal, arbDualReal, arbDualReal, arbDualReal) { a, b, x, y ->
                val result = act(dual(a, b), dv(x, y))
                result.primal shouldBeApproximately (a * x)
                result.tangent shouldBeApproximately (a * y + b * x)
            }
        }

        test("lift(a) ⊳ (x, y) = (ax, ay) — lifted scalars scale uniformly") {
            checkAll(arbDualReal, arbDualReal, arbDualReal) { a, x, y ->
                val result = act(dualScalars.lift(a), dv(x, y))
                result.primal shouldBeApproximately (a * x)
                result.tangent shouldBeApproximately (a * y)
            }
        }

        test("(bε) ⊳ (x, y) = (0, bx) — pure infinitesimals slide primal into tangent") {
            checkAll(arbDualReal, arbDualReal, arbDualReal) { b, x, y ->
                val result = act(dualScalars.eps(b), dv(x, y))
                result.primal shouldBeApproximately 0.0
                result.tangent shouldBeApproximately (b * x)
            }
        }

        test("ε ⊳ (x, y) = (0, x) — the canonical infinitesimal shifts primal to tangent") {
            checkAll(arbDualReal, arbDualReal) { x, y ->
                val result = act(dualScalars.epsOne, dv(x, y))
                result.primal shouldBeApproximately 0.0
                result.tangent shouldBeApproximately x
            }
        }

        test("0 ⊳ v = 0") {
            checkAll(arbVec) { v ->
                val result = act(zeroScalar, v)
                result.primal shouldBeApproximately 0.0
                result.tangent shouldBeApproximately 0.0
            }
        }

        test("1 ⊳ v = v") {
            checkAll(arbVec) { v ->
                val result = act(oneScalar, v)
                result.primal shouldBeApproximately v.primal
                result.tangent shouldBeApproximately v.tangent
            }
        }
    }

    // ── ε² = 0 at the module level ─────────────────────────────────────────
    //
    // The ring identity ε² = 0 has a module-level shadow: applying ε twice
    // annihilates any vector.

    context("Epsilon nilpotency at the module level") {

        test("ε ⊳ (ε ⊳ v) = 0 for every v") {
            checkAll(arbVec) { v ->
                val once = act(dualScalars.epsOne, v)
                val twice = act(dualScalars.epsOne, once)
                twice.primal shouldBeApproximately 0.0
                twice.tangent shouldBeApproximately 0.0
            }
        }

        test("(bε) ⊳ ((cε) ⊳ v) = 0 for every b, c, v") {
            checkAll(arbDualReal, arbDualReal, arbVec) { b, c, v ->
                val inner = act(dualScalars.eps(c), v)
                val outer = act(dualScalars.eps(b), inner)
                outer.primal shouldBeApproximately 0.0
                outer.tangent shouldBeApproximately 0.0
            }
        }
    }

    // ── Primal/tangent embeddings ──────────────────────────────────────────
    //
    // Two natural embeddings of V into the module:
    //   primalEmb(v) = (v, 0)
    //   tangentEmb(v) = (0, v)
    //
    // The interesting structure is how lifted scalars and ε move between them.

    context("Primal/tangent embeddings interact correctly with the action") {

        test("lift(a) ⊳ (v, 0) = (av, 0) — primal embedding is preserved by lifted scalars") {
            checkAll(arbDualReal, arbDualReal) { a, v ->
                val result = act(dualScalars.lift(a), dv(v, 0.0))
                result.primal shouldBeApproximately (a * v)
                result.tangent shouldBeApproximately 0.0
            }
        }

        test("ε ⊳ (v, 0) = (0, v) — ε rotates primal into tangent") {
            checkAll(arbDualReal) { v ->
                val result = act(dualScalars.epsOne, dv(v, 0.0))
                result.primal shouldBeApproximately 0.0
                result.tangent shouldBeApproximately v
            }
        }

        test("ε ⊳ (0, v) = (0, 0) — ε kills the tangent-only component") {
            checkAll(arbDualReal) { v ->
                val result = act(dualScalars.epsOne, dv(0.0, v))
                result.primal shouldBeApproximately 0.0
                result.tangent shouldBeApproximately 0.0
            }
        }

        test("(1 + ε) ⊳ (v, 0) = (v, v) — adds a tangent copy of the primal") {
            checkAll(arbDualReal) { v ->
                val onePlusEps = dualScalars.add(dualScalars.lift(1.0), dualScalars.epsOne)
                val result = act(onePlusEps, dv(v, 0.0))
                result.primal shouldBeApproximately v
                result.tangent shouldBeApproximately v
            }
        }
    }

    // ── Edge cases ─────────────────────────────────────────────────────────

    context("Edge cases") {

        test("any scalar acting on the zero vector gives the zero vector") {
            checkAll(arbDual) { s ->
                val result = act(s, zeroVec)
                result.primal shouldBeApproximately 0.0
                result.tangent shouldBeApproximately 0.0
            }
        }

        test("the zero scalar acts as the constant zero map") {
            checkAll(arbVec) { v ->
                val result = act(zeroScalar, v)
                result.primal shouldBeApproximately 0.0
                result.tangent shouldBeApproximately 0.0
            }
        }
    }
})
