package org.vorpal.kosmos.linear.ops

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.linear.instances.FixedVecAlgebras
import org.vorpal.kosmos.linear.values.Vec3
import org.vorpal.kosmos.linear.values.arbVec3
import java.math.BigInteger

/**
 * The cross product on `R³`, checked exactly over ℤ and ℚ.
 */
class FixedVecOpsSpec : StringSpec({
    val z = IntegerAlgebras.IntegerCommutativeRing
    val q = RationalAlgebras.RationalField
    val arbInt = Arb.int(-20..20).map(Int::toBigInteger)
    val arbZ3 = arbVec3(arbInt)
    val arbQ3 = arbVec3(ArbRational.small)
    val z3 = FixedVecAlgebras.vec3Module(z)

    fun cross(v: Vec3<BigInteger>, w: Vec3<BigInteger>) = FixedVecOps.cross(z, v, w)
    fun dot(v: Vec3<BigInteger>, w: Vec3<BigInteger>) = DenseVecOps.dot(z, v, w)
    val zeroZ = Vec3(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO)

    "e₁ × e₂ = e₃, e₂ × e₃ = e₁, e₃ × e₁ = e₂" {
        val (o, l) = BigInteger.ZERO to BigInteger.ONE
        val e1 = Vec3(l, o, o)
        val e2 = Vec3(o, l, o)
        val e3 = Vec3(o, o, l)
        cross(e1, e2) shouldBe e3
        cross(e2, e3) shouldBe e1
        cross(e3, e1) shouldBe e2
    }

    "the cross product is antisymmetric, and u × u = 0" {
        checkAll(arbZ3, arbZ3) { u, v ->
            cross(u, v) shouldBe z3.add.inverse(cross(v, u))
            cross(u, u) shouldBe zeroZ
        }
    }

    "u × v is orthogonal to u and v" {
        checkAll(arbZ3, arbZ3) { u, v ->
            dot(u, cross(u, v)) shouldBe BigInteger.ZERO
            dot(v, cross(u, v)) shouldBe BigInteger.ZERO
        }
    }

    "the Jacobi identity: u × (v × w) + v × (w × u) + w × (u × v) = 0" {
        checkAll(arbZ3, arbZ3, arbZ3) { u, v, w ->
            z3.add(z3.add(cross(u, cross(v, w)), cross(v, cross(w, u))), cross(w, cross(u, v))) shouldBe zeroZ
        }
    }

    "Lagrange's identity over ℚ: |u × v|² = |u|²|v|² − (u·v)²" {
        checkAll(arbQ3, arbQ3) { u, v ->
            val c = FixedVecOps.cross(q, u, v)
            DenseVecOps.normSq(q, c) shouldBe
                DenseVecOps.normSq(q, u) * DenseVecOps.normSq(q, v) - DenseVecOps.dot(q, u, v).let { it * it }
        }
    }

    "vec3Module over ℤ satisfies RModuleLaws" {
        RModuleLaws(module = z3, scalarArb = arbInt, vectorArb = arbZ3).fullTest().throwIfFailed()
    }
})
