package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.ComplexField
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.ComplexModule
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.InvolutiveAlgebra
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.LeftRModule
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.normSq
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger
import kotlin.isFinite

typealias Quaternion = CD<Complex>

/**
 * The scalar component of the quaternion.
 */
val Quaternion.w: Real get() = a.re

/**
 * The coefficient of the i term of the quaternion.
 */
val Quaternion.x: Real get() = a.im

/**
 * The coefficient of the j term of the quaternion.
 */
val Quaternion.y: Real get() = -b.re

/**
 * The coefficient of the k term of the quaternion.
 */
val Quaternion.z: Real get() = b.im

/**
 * Convenience constructor for a quaternion:
 *
 * `complex(w + x i_c), complex(-y + z i_c)`
 */
fun quaternion(
    w: Real,
    x: Real,
    y: Real,
    z: Real
): Quaternion {
    val a = complex(w, x)
    val b = complex(-y, z)
    return Quaternion(a, b)
}

object QuaternionAlgebras {

    fun Quaternion.normSq(): Real =
        a.normSq() + b.normSq()

    object QuaternionDivisionRing :
        DivisionRing<Quaternion>,
        InvolutiveRing<Quaternion>,
        NormedDivisionAlgebra<Quaternion> {

        private val base: InvolutiveAlgebra<Quaternion> =
            CayleyDickson(ComplexField)

        override val add = base.add

        override val mul: Monoid<Quaternion> = Monoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override val reciprocal: Endo<Quaternion> = Endo(Symbols.SLASH) { q ->
            val n2 = q.normSq()
            // TODO: We probably want a tolerance check here.
            require(n2 != 0.0 && n2.isFinite()) { "Zero has no multiplicative inverse in ${Symbols.BB_H}." }

            val qc = conj(q)
            val scale = 1.0 / n2

            // Use the ComplexModule's action to scale.
            // We could use QuaternionModule, but we fall back to ComplexModule to avoid circular dependencies.
            Quaternion(ComplexModule.leftAction(scale, qc.a), ComplexModule.leftAction(scale, qc.b))
        }

        override fun fromBigInt(n: BigInteger) = base.fromBigInt(n)
        override val conj = base.conj

        override val normSq: UnaryOp<Quaternion, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ it.normSq() }

        // Disambiguate zero.
        override val zero = base.add.identity
        override val one: Quaternion
            get() = mul.identity
        val i = Quaternion(ComplexField.i, ComplexField.zero)
        val j = Quaternion(ComplexField.zero, ComplexField.negOne)
        val k = Quaternion(ComplexField.zero, ComplexField.i)
    }

    // Scalars: Real, act componentwise on (a, b)
    val QuaternionModule: RModule<Real, Quaternion> = RModule.of(
        scalars = RealField,
        group = QuaternionDivisionRing.add,
        leftAction = LeftAction { r, q ->
            Quaternion(
                ComplexModule.leftAction(r, q.a),
                ComplexModule.leftAction(r, q.b)
            )
        }
    )

    /**
     * Embed a complex number into a Quaternion.
     */
    fun Complex.asQuaternion(): Quaternion =
        quaternion(re, im, 0.0, 0.0)

    /**
     * The Quaternions can then be a left R-Module of the Complex numbers.
     *
     * This satisfies the left-module laws:
     *
     *    (λ + μ) · q = λ · q + μ · q
     *    λ · (q + r) = λ · q + λ · r
     *    (λ μ) · q = λ · (μ · q)
     *    1 · q = q
     *
     * Note: this is scalar multiplication by a chosen complex subfield of ℍ.
     */
    val QuaternionLeftComplexModule: LeftRModule<Complex, Quaternion> = LeftRModule.of(
        leftScalars = ComplexField,
        group = QuaternionDivisionRing.add,
        leftAction = LeftAction { c, q ->
            // Embed c into ℍ as (c, 0) and then multiply on the left.
            QuaternionDivisionRing.mul.op(c.asQuaternion(), q)
        }
    )

    object QuaternionStarAlgebra:
        StarAlgebra<Real, Quaternion>,
        InvolutiveRing<Quaternion> by QuaternionDivisionRing,
        RModule<Real, Quaternion> by QuaternionModule {
            override val one = QuaternionDivisionRing.one
        }
}


val eqQuaternionStrict: Eq<Quaternion> = CD.eq(eqComplexStrict)
val eqQuaternion: Eq<Quaternion> = CD.eq(eqComplex)


fun main() {
    val H = QuaternionAlgebras.QuaternionDivisionRing
    val one = H.one
    val negOne = H.add.inverse(one)
    val eq = eqQuaternionStrict

    val i = QuaternionAlgebras.QuaternionDivisionRing.i
    val j = QuaternionAlgebras.QuaternionDivisionRing.j
    val k = QuaternionAlgebras.QuaternionDivisionRing.k

    check(eq(H.mul.op(i, i), negOne))
    check(eq(H.mul.op(j, j), negOne))
    check(eq(H.mul.op(k, k), negOne))

    val ij = H.mul.op(i, j)
    check(eq(ij, k))          // depending on your handedness, this might be == k or == -k
    check(eq(H.mul.op(j, i), H.add.inverse(k))) // should be the opposite sign
}