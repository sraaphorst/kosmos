package org.vorpal.kosmos.linear.instances

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.constant
import org.vorpal.kosmos.algebra.structures.instances.RationalAlgebras
import org.vorpal.kosmos.core.rational.ArbRational
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.laws.algebra.AbelianGroupLaws
import org.vorpal.kosmos.laws.algebra.VectorSpaceLaws
import org.vorpal.kosmos.linear.values.Vec0
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.Vec3
import org.vorpal.kosmos.linear.values.Vec4

/**
 * Algebraic-law coverage for the fixed-arity tuple structures in [FixedTupleAlgebras],
 * over the rationals. The [Vec0]..[Vec4] types are data classes, so the default structural
 * equality is used.
 */
class FixedTupleAlgebrasSpec : StringSpec({

    val q = RationalAlgebras.RationalField
    val eqQ = RationalAlgebras.eqRational
    val prQ = RationalAlgebras.printableRationalPretty

    val arbVec2 = Arb.bind(ArbRational.small, ArbRational.small) { x, y -> Vec2(x, y) }
    val arbVec3 = Arb.bind(ArbRational.small, ArbRational.small, ArbRational.small) { x, y, z -> Vec3(x, y, z) }
    val arbVec4 = Arb.bind(
        ArbRational.small, ArbRational.small, ArbRational.small, ArbRational.small
    ) { x, y, z, w -> Vec4(x, y, z, w) }

    "dim0Group satisfies AbelianGroupLaws" {
        AbelianGroupLaws(
            group = FixedTupleAlgebras.dim0Group<Rational>(),
            arb = Arb.constant(Vec0<Rational>())
        ).fullTest().throwIfFailed()
    }

    "dim2Group satisfies AbelianGroupLaws over the rationals" {
        AbelianGroupLaws(
            group = FixedTupleAlgebras.dim2Group(q.add),
            arb = arbVec2
        ).fullTest().throwIfFailed()
    }

    "vec2Space satisfies VectorSpaceLaws over the rationals" {
        VectorSpaceLaws(
            space = FixedTupleAlgebras.vec2Space(q),
            scalarArb = ArbRational.small,
            vectorArb = arbVec2,
            eqF = eqQ,
            prF = prQ
        ).fullTest().throwIfFailed()
    }

    "vec3Space satisfies VectorSpaceLaws over the rationals" {
        VectorSpaceLaws(
            space = FixedTupleAlgebras.vec3Space(q),
            scalarArb = ArbRational.small,
            vectorArb = arbVec3,
            eqF = eqQ,
            prF = prQ
        ).fullTest().throwIfFailed()
    }

    "vec4Space satisfies VectorSpaceLaws over the rationals" {
        VectorSpaceLaws(
            space = FixedTupleAlgebras.vec4Space(q),
            scalarArb = ArbRational.small,
            vectorArb = arbVec4,
            eqF = eqQ,
            prF = prQ
        ).fullTest().throwIfFailed()
    }
})
