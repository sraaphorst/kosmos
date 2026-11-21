package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.constant
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.map
import java.math.BigInteger
import org.vorpal.kosmos.algebra.structures.instances.IntegerAlgebras
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.RModuleLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.linear.values.Vec0
import org.vorpal.kosmos.linear.values.Vec1
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.Vec3
import org.vorpal.kosmos.linear.values.Vec4
import org.vorpal.kosmos.linear.values.arbVec1
import org.vorpal.kosmos.linear.values.arbVec2
import org.vorpal.kosmos.linear.values.arbVec3
import org.vorpal.kosmos.linear.values.arbVec4
import org.vorpal.kosmos.testing.laws.lawTests
import org.vorpal.kosmos.testing.laws.local

/**
 * Algebraic-law coverage for the fixed-size vector structures in [FixedVecAlgebras]: the groups
 * and vector spaces over the rationals, and the modules over the integers. The [Vec0]..Vec4 types
 * are data classes, so the default structural equality is used.
 */
class FixedVecAlgebrasSpec : FunSpec({
    lawTests(lawGraph("fixed-size vector algebras") {
        val q = RationalAlgebras.RationalField
        val eqQ = RationalAlgebras.eqRational
        val prQ = RationalAlgebras.printableRationalPretty

        val arbVec2 = arbVec2(ArbRational.small)
        val arbVec3 = arbVec3(ArbRational.small)
        val arbVec4 = arbVec4(ArbRational.small)

        branch("dim0Group satisfies AbelianGroupLaws") {
            suite(AbelianGroupLaws(
                group = FixedVecAlgebras.dim0Group<Rational>(),
                arb = Arb.constant(Vec0<Rational>())
            ))
        }

        branch("dim2Group satisfies AbelianGroupLaws over the rationals") {
            suite(AbelianGroupLaws(
                group = FixedVecAlgebras.dim2Group(q.add),
                arb = arbVec2
            ))
        }

        branch("vec2Space satisfies VectorSpaceLaws over the rationals") {
            suite(VectorSpaceLaws(
                space = FixedVecAlgebras.vec2Space(q),
                scalarArb = ArbRational.small,
                vectorArb = arbVec2,
                eqF = eqQ,
                prF = prQ
            ))
        }

        branch("vec3Space satisfies VectorSpaceLaws over the rationals") {
            suite(VectorSpaceLaws(
                space = FixedVecAlgebras.vec3Space(q),
                scalarArb = ArbRational.small,
                vectorArb = arbVec3,
                eqF = eqQ,
                prF = prQ
            ))
        }

        branch("vec4Space satisfies VectorSpaceLaws over the rationals") {
            suite(VectorSpaceLaws(
                space = FixedVecAlgebras.vec4Space(q),
                scalarArb = ArbRational.small,
                vectorArb = arbVec4,
                eqF = eqQ,
                prF = prQ
            ))
        }

        branch("vec0Space and vec1Space satisfy VectorSpaceLaws over the rationals") {
            suite(VectorSpaceLaws(
                space = FixedVecAlgebras.vec0Space(q),
                scalarArb = ArbRational.small,
                vectorArb = Arb.constant(Vec0<Rational>()),
                eqF = eqQ,
                prF = prQ
            ))
            suite(VectorSpaceLaws(
                space = FixedVecAlgebras.vec1Space(q),
                scalarArb = ArbRational.small,
                vectorArb = arbVec1(ArbRational.small),
                eqF = eqQ,
                prF = prQ
            ))
        }

        branch("dim1Group, dim3Group and dim4Group satisfy AbelianGroupLaws over the rationals") {
            suite(AbelianGroupLaws(group = FixedVecAlgebras.dim1Group(q.add), arb = arbVec1(ArbRational.small)))
            suite(AbelianGroupLaws(group = FixedVecAlgebras.dim3Group(q.add), arb = arbVec3))
            suite(AbelianGroupLaws(group = FixedVecAlgebras.dim4Group(q.add), arb = arbVec4))
        }

        branch("vec0Module through vec4Module satisfy RModuleLaws over the integers") {
            val z = IntegerAlgebras.IntegerCommutativeRing
            val arbZ = Arb.int(-20..20).map(Int::toBigInteger)
            suite(RModuleLaws(FixedVecAlgebras.vec0Module(z), arbZ, Arb.constant(Vec0())))
            suite(RModuleLaws(FixedVecAlgebras.vec1Module(z), arbZ, arbVec1(arbZ)))
            suite(RModuleLaws(FixedVecAlgebras.vec2Module(z), arbZ, arbVec2(arbZ)))
            suite(RModuleLaws(FixedVecAlgebras.vec3Module(z), arbZ, arbVec3(arbZ)))
            suite(RModuleLaws(FixedVecAlgebras.vec4Module(z), arbZ, arbVec4(arbZ)))
        }

        local("eqVec1 through eqVec4 compare componentwise with the given entry equality") {
            val three = BigInteger.valueOf(3)
            val mod3 = Eq<BigInteger> { a, b -> (a - b).mod(three) == BigInteger.ZERO }
            fun n(i: Int) = i.toBigInteger()
            FixedVecAlgebras.eqVec1(mod3)(Vec1(n(1)), Vec1(n(4))) shouldBe true
            FixedVecAlgebras.eqVec1(mod3)(Vec1(n(1)), Vec1(n(2))) shouldBe false
            FixedVecAlgebras.eqVec2(mod3)(Vec2(n(1), n(2)), Vec2(n(4), n(-1))) shouldBe true
            FixedVecAlgebras.eqVec2(mod3)(Vec2(n(1), n(2)), Vec2(n(1), n(3))) shouldBe false
            FixedVecAlgebras.eqVec3(mod3)(Vec3(n(0), n(1), n(2)), Vec3(n(3), n(7), n(-4))) shouldBe true
            FixedVecAlgebras.eqVec3(mod3)(Vec3(n(0), n(1), n(2)), Vec3(n(0), n(1), n(0))) shouldBe false
            FixedVecAlgebras.eqVec4(mod3)(Vec4(n(0), n(1), n(2), n(3)), Vec4(n(3), n(4), n(5), n(0))) shouldBe true
            FixedVecAlgebras.eqVec4(mod3)(Vec4(n(0), n(1), n(2), n(3)), Vec4(n(0), n(1), n(2), n(4))) shouldBe false
        }
    })
})
