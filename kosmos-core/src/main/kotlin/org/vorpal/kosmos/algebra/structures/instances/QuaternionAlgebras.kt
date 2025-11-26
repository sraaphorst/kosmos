package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.ComplexField
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.ComplexModule
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.InvolutiveAlgebra
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.LeftRModule
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.complex
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.im
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.normSq
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.re
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.algebra.structures.negOne
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.Endo
import java.math.BigInteger

typealias Quaternion = CD<Complex>

object QuaternionAlgebras {
    fun Quaternion.normSq(): Double =
        a.normSq() + b.normSq()

    object QuaternionDivisionRing :
        DivisionRing<Quaternion>,
        InvolutiveRing<Quaternion> {

        private val base: InvolutiveAlgebra<Quaternion> =
            CayleyDickson(ComplexAlgebras.ComplexField)

        override val add = base.add

        override val mul: Monoid<Quaternion> = Monoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override val reciprocal: Endo<Quaternion> = Endo(Symbols.SLASH) { q ->
            val n2 = q.normSq()
            require(n2 != 0.0) { "Zero has no multiplicative inverse in ℍ" }

            val qc = conj(q)
            val scale = 1.0 / n2

            // Use the ComplexModule's action to scale.
            val aScaled = ComplexModule.action(scale, qc.a)
            val bScaled = ComplexModule.action(scale, qc.b)

            Quaternion(aScaled, bScaled)
        }

        override fun fromBigInt(n: BigInteger) = base.fromBigInt(n)
        override val conj = base.conj

        val zero = Quaternion(ComplexField.zero, ComplexField.zero)
        val one = Quaternion(ComplexField.one, ComplexField.zero)
        val i = Quaternion(ComplexField.i, ComplexField.zero)
        val j = Quaternion(ComplexField.zero, ComplexField.negOne)
        val k = Quaternion(ComplexField.zero, ComplexField.i)
    }

    /**
     * The scalar component of the quaternion.
     */
    val Quaternion.w: Double
        get() = a.re

    /**
     * The coefficient of the i term of the quaternion.
     */
    val Quaternion.x: Double
        get() = a.im

    /**
     * The coefficient of the j term of the quaternion.
     */
    val Quaternion.y: Double
        get() = -b.re

    /**
     * The coefficient of the k term of the quaternion.
     */
    val Quaternion.z: Double
        get() = b.im

    /**
     * Convenience constructor for a quaternion:
     *
     * `complex(w + x i_c), complex(-y + z i_c)`
     */
    fun quaternion(
        w: Double,
        x: Double,
        y: Double,
        z: Double
    ): Quaternion {
        val a = complex(w, x)
        val b = complex(-y, z)
        return Quaternion(a, b)
    }

    // Scalars: Double, act componentwise on (a, b)
    object QuaternionModule : RModule<Double, Quaternion> {
        override val ring: CommutativeRing<Double> =
            RealField

        override val group: AbelianGroup<Quaternion> =
            QuaternionDivisionRing.add

        override val action: Action<Double, Quaternion> =
            Action { r, q -> quaternion(
                r * q.w,
                r * q.x,
                r * q.y,
                r * q.z)
            }
    }

    /**
     * Embed a complex number into a Quaternion.
     */
    fun Complex.asQuaternion(): Quaternion =
        quaternion(re, im, 0.0, 0.0)

    /**
     * The Quaternions can then be a left R-Module of the Complex numbers.
     *
     * This satisfies the left-module laws:
     * - `(λ + μ) · q = λ · q + μ · q`
     * - `λ · (q + r) = λ · q + λ · r`
     * - `(λ μ) · q = λ · (μ · q)`
     * - `1 · q = q`
     */
    val QuaternionLeftComplexModule: LeftRModule<Complex, Quaternion> = LeftRModule.of(
        leftRing = ComplexField,
        group = QuaternionDivisionRing.add,
        leftAction = Action { c, q ->
            // Embed c into ℍ as (c, 0) and then multiply on the left.
            QuaternionDivisionRing.mul.op(c.asQuaternion(), q)
        }
    )

    object QuaternionStarAlgebra:
        StarAlgebra<Real, Quaternion>,
        InvolutiveRing<Quaternion> by QuaternionDivisionRing,
        RModule<Real, Quaternion> by QuaternionModule
}
