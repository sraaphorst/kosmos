package org.vorpal.kosmos.hypercomplex.dual

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.products.AbelianGroups
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import java.math.BigInteger

/**
 * The scalar extension `V ⊗_R Dual(R)` of a module, with the action `(a + bε)(x, y) = (ax, ay + bx)`.
 *
 * Checked exactly for `V = ℚ²` over ℚ, and for `V = ℤ` over ℤ to show that a field is not needed.
 */
class DualModulesSpec : StringSpec({
    val q = RationalAlgebras.RationalField

    /** ℚ² as a module over ℚ, with componentwise operations. */
    val plane: RModule<Rational, Pair<Rational, Rational>> = RModule.of(
        scalars = q,
        add = AbelianGroups.product(q.add, q.add),
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { r, (x, y) -> r * x to r * y }
    )

    val module = DualModules.scalarExtension(plane)
    val scalars = module.scalars

    val arbQ = ArbDual.rational
    val arbPoint: Arb<Pair<Rational, Rational>> = Arb.bind(arbQ, arbQ) { x, y -> x to y }
    val arbVector = ArbDual.dualVectorOf(arbPoint)
    val arbScalar = ArbDual.rationalDual

    val origin = Rational.ZERO to Rational.ZERO
    val eps = DualAlgebras.infinitesimalUnit(q)
    fun act(s: Dual<Rational>, v: DualVector<Pair<Rational, Rational>>) = module.leftAction(s, v)
    fun scale(r: Rational, p: Pair<Rational, Rational>) = plane.leftAction(r, p)

    "the scalar extension of ℚ² satisfies RModuleLaws over Dual<ℚ>" {
        RModuleLaws(module, arbScalar, arbVector).fullTest().throwIfFailed()
    }

    "its scalars are the dual ring over the scalars of the base module" {
        scalars.zero shouldBe dual(Rational.ZERO, Rational.ZERO)
        scalars.one shouldBe dual(Rational.ONE, Rational.ZERO)
        checkAll(arbScalar, arbScalar) { s, t ->
            scalars.mul(s, t) shouldBe DualAlgebras.dualRing(q).mul(s, t)
        }
    }

    "addition and negation are componentwise, with zero (0, 0)" {
        module.add.identity shouldBe DualVector(origin, origin)
        checkAll(arbVector, arbVector) { v, w ->
            module.add(v, w) shouldBe DualVector(plane.add(v.primal, w.primal), plane.add(v.tangent, w.tangent))
            module.add.inverse(v) shouldBe DualVector(plane.add.inverse(v.primal), plane.add.inverse(v.tangent))
        }
    }

    "action formula: (a + bε) ⊳ (x, y) = (ax, ay + bx)" {
        checkAll(arbScalar, arbVector) { s, v ->
            act(s, v) shouldBe DualVector(
                primal = scale(s.a, v.primal),
                tangent = plane.add(scale(s.a, v.tangent), scale(s.b, v.primal))
            )
        }
    }

    "constants scale both parts uniformly: a ⊳ (x, y) = (ax, ay)" {
        checkAll(arbQ, arbVector) { a, v ->
            act(DualAlgebras.constant(q, a), v) shouldBe DualVector(scale(a, v.primal), scale(a, v.tangent))
        }
    }

    "ε shifts the primal part into the tangent: ε ⊳ (x, y) = (0, x), and ε ⊳ (ε ⊳ v) = 0" {
        checkAll(arbVector) { v ->
            act(eps, v) shouldBe DualVector(origin, v.primal)
            act(eps, act(eps, v)) shouldBe module.add.identity
        }
    }

    "pure infinitesimals compose to zero: (bε) ⊳ ((cε) ⊳ v) = 0" {
        checkAll(arbQ, arbQ, arbVector) { b, c, v ->
            act(dual(Rational.ZERO, b), act(dual(Rational.ZERO, c), v)) shouldBe module.add.identity
        }
    }

    "(1 + ε) ⊳ (x, 0) = (x, x): the seed attaches a tangent copy of the point" {
        checkAll(arbPoint) { x ->
            act(DualAlgebras.variable(q, Rational.ONE), DualVector(x, origin)) shouldBe DualVector(x, x)
        }
    }

    "the zero scalar and the zero vector annihilate; the unit scalar is the identity" {
        checkAll(arbScalar, arbVector) { s, v ->
            act(scalars.zero, v) shouldBe module.add.identity
            act(s, module.add.identity) shouldBe module.add.identity
            act(scalars.one, v) shouldBe v
        }
    }

    "an affine map p ↦ λ·p + c, extended to dual vectors, carries the tangent to λ·v" {
        checkAll(arbQ, arbPoint, arbVector) { lambda, c, v ->
            val shifted = module.add(act(DualAlgebras.constant(q, lambda), v), DualVector(c, origin))
            shifted shouldBe DualVector(plane.add(scale(lambda, v.primal), c), scale(lambda, v.tangent))
        }
    }

    "a field is not needed: the scalar extension of ℤ over ℤ satisfies RModuleLaws" {
        val z = IntegerAlgebras.IntegerCommutativeRing
        val zModule: RModule<BigInteger, BigInteger> = RModule.of(
            scalars = z,
            add = z.add,
            leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { r, x -> r * x }
        )
        val arbZ = Arb.int(-30..30).map(Int::toBigInteger)
        RModuleLaws(
            module = DualModules.scalarExtension(zModule),
            scalarArb = ArbDual.integerDual,
            vectorArb = ArbDual.dualVectorOf(arbZ)
        ).fullTest().throwIfFailed()
    }

    "DualVector is a plain pair with Eq and Printable helpers" {
        val v = DualVector(primal = 1, tangent = 2)
        val (p, t) = v
        (p to t) shouldBe (1 to 2)
        eqDualVector<Int>(org.vorpal.kosmos.core.Eq.default())(v, DualVector(1, 2)) shouldBe true
        eqDualVector<Int>(org.vorpal.kosmos.core.Eq.default())(v, DualVector(1, 3)) shouldBe false
        printableDualVector<Int>(org.vorpal.kosmos.core.render.Printable.default())(v) shouldBe
            "DualVector(primal=1, tangent=2)"
    }
})
