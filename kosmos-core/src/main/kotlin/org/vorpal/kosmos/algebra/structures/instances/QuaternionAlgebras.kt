package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.morphisms.RingHomomorphism
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.ComplexField
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.DivisionRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.StarAlgebra
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.ComplexRealVectorSpace
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.render.Printable
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
val Quaternion.y: Real get() = b.re

/**
 * The coefficient of the k term of the quaternion.
 */
val Quaternion.z: Real get() = b.im

/**
 * Convenience constructor for a quaternion:
 *
 *    complex(w + x i_c), complex(y + z i_c)
 */
fun quaternion(
    w: Real,
    x: Real,
    y: Real,
    z: Real
): Quaternion {
    val a = complex(w, x)
    val b = complex(y, z)
    return Quaternion(a, b)
}

object QuaternionAlgebras {
    private val eqRealApprox = Eqs.realApprox()

    object QuaternionDivisionRing :
        DivisionRing<Quaternion>,
        InvolutiveRing<Quaternion>,
        NormedDivisionAlgebra<Quaternion> {

        private val base: NonAssociativeInvolutiveRing<Quaternion> =
            CayleyDickson(ComplexField)

        override val add = base.add

        override val mul: Monoid<Quaternion> = Monoid.of(
            identity = base.mul.identity,
            op = base.mul.op
        )

        override val reciprocal: Endo<Quaternion> = Endo(Symbols.SLASH) { q ->
            val n2 = this.normSq(q)
            require(eqRealApprox.neqv(n2, 0.0) && n2.isFinite()) { "Zero has no multiplicative inverse in ${Symbols.BB_H}." }

            val qc = conj(q)
            val scale = 1.0 / n2

            // Use the ComplexRealVectorSpace's action to scale.
            // We could use QuaternionModule, but we fall back to ComplexModule to avoid circular dependencies.
            Quaternion(
                ComplexRealVectorSpace.leftAction(scale, qc.a),
                ComplexRealVectorSpace.leftAction(scale, qc.b))
        }

        override fun fromBigInt(n: BigInteger) = base.fromBigInt(n)
        override val conj = base.conj

        override val normSq: UnaryOp<Quaternion, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL){ q -> mul(q, conj(q)).w }

        // Disambiguate zero and one.
        override val zero = base.add.identity
        override val one = mul.identity
    }

    // Scalars: Real, act componentwise on (a, b)
    val QuaternionRealVectorSpace: VectorSpace<Real, Quaternion> = VectorSpace.of(
        RealField,
        QuaternionDivisionRing.add,
        LeftAction { r, q ->
            Quaternion(
                ComplexRealVectorSpace.leftAction(r, q.a),
                ComplexRealVectorSpace.leftAction(r, q.b)
            )
        }
    )

    /**
     * Embed a complex number into ℍ using the subfield ℝ ⊕ ℝ·i.
     *
     * * Sends a + b i_C  ↦  a·1 + b·i.
     *
     * This embedding is a choice of complex subfield in ℍ (span{1, i}).
     */
    val embedCAlongI: RingHomomorphism<Complex, Quaternion> =
        RingHomomorphism.of(
            domain = ComplexField,
            codomain = QuaternionDivisionRing,
        ) { c ->
            quaternion(c.re, c.im, 0.0, 0.0)
        }
    fun Complex.asQuaternionAlongI(): Quaternion = embedCAlongI(this)

    /**
     * Embed a complex number into ℍ using the subfield ℝ ⊕ ℝ·j.
     *
     * Sends a + b i_C  ↦  a·1 + b·j.
     *
     * This embedding is a choice of complex subfield in ℍ (span{1, j}).
     */
    val embedCAlongJ: RingHomomorphism<Complex, Quaternion> =
        RingHomomorphism.of(
            domain = ComplexField,
            codomain = QuaternionDivisionRing,
        ) { c ->
            quaternion(w = c.re, x = 0.0, y = c.im, z = 0.0)
        }
    fun Complex.asQuaternionAlongJ(): Quaternion = embedCAlongJ(this)

    /**
     * Embed a complex number into ℍ using the subfield ℝ ⊕ ℝ·k.
     *
     * Sends a + b i_C  ↦  a·1 + b·k.
     *
     * This embedding is a choice of complex subfield in ℍ (span{1, k}).
     */
    val embedCAlongK: RingHomomorphism<Complex, Quaternion> =
        RingHomomorphism.of(
            domain = ComplexField,
            codomain = QuaternionDivisionRing,
        ) { c ->
            quaternion(w = c.re, x = 0.0, y = 0.0, z = c.im)
        }
    fun Complex.asQuaternionAlongK(): Quaternion = embedCAlongK(this)

    /**
     * The Quaternions can then be a vector space of the Complex numbers using any of the
     * homomorphisms defined above.
     *
     * This satisfies the left-module laws required of vector spaces:
     *
     *    (λ + μ) · q = λ · q + μ · q
     *    λ · (q + r) = λ · q + λ · r
     *    (λ μ) · q = λ · (μ · q)
     *    1 · q = q
     */
    fun quaternionLeftComplexVectorSpace(embed: RingHomomorphism<Complex, Quaternion>): VectorSpace<Complex, Quaternion> =
        VectorSpace.of(
            ComplexField,
            QuaternionDivisionRing.add,
            LeftAction { c, q -> QuaternionDivisionRing.mul(embed(c), q) }
        )

    val QuaternionLeftComplexVectorSpaceAlongI: VectorSpace<Complex, Quaternion> =
        quaternionLeftComplexVectorSpace(embedCAlongI)

    val QuaternionLeftComplexVectorSpaceAlongJ: VectorSpace<Complex, Quaternion> =
        quaternionLeftComplexVectorSpace(embedCAlongJ)

    val QuaternionLeftComplexVectorSpaceAlongK: VectorSpace<Complex, Quaternion> =
        quaternionLeftComplexVectorSpace(embedCAlongK)

    object QuaternionStarAlgebra:
        StarAlgebra<Real, Quaternion>,
        InvolutiveRing<Quaternion> by QuaternionDivisionRing,
        VectorSpace<Real, Quaternion> by QuaternionRealVectorSpace
}

data class QuaternionBasis(
    val i: Quaternion,
    val j: Quaternion,
    val k: Quaternion
)

object QuaternionBases {
    /**
     * RIGHT: ij = k, LEFT: ij = -k.
     */
    enum class Handedness { RIGHT, LEFT }

    val cdI: Quaternion = Quaternion(ComplexField.i, ComplexField.zero)
    val cdJ: Quaternion = Quaternion(ComplexField.zero, ComplexField.one)
    val cdK: Quaternion = Quaternion(ComplexField.zero, ComplexField.i)

    fun basis(
        quaternions: DivisionRing<Quaternion>,
        handedness: Handedness,
        eq: Eq<Quaternion> = eqQuaternionStrict
    ): QuaternionBasis {
        val i = cdI
        val j = cdJ
        val k0 = cdK

        val ij = quaternions.mul(i, j)

        val kRight =
            when {
                eq(ij, k0) -> k0
                eq(ij, quaternions.add.inverse(k0)) -> quaternions.add.inverse(k0)
                else -> error("Sanity failed: i*j not equal to ±k0. CD convention mismatch.")
            }

        val k = when (handedness) {
            Handedness.RIGHT -> kRight
            Handedness.LEFT -> quaternions.add.inverse(kRight)
        }

        return QuaternionBasis(i, j, k)
    }

    fun quaternionBasisPrintable(
        handedness: Handedness,
        prQ: Printable<Quaternion>
    ): Printable<QuaternionBasis> = Printable { b ->
        "⟨i=${prQ(b.i)}, j=${prQ(b.j)}, k=${prQ(b.k)}; handedness=$handedness⟩"
    }
}

val eqQuaternionStrict: Eq<Quaternion> = CD.eq(eqComplexStrict)
val eqQuaternion: Eq<Quaternion> = CD.eq(eqComplex)

fun main() {
    val quaternions = QuaternionAlgebras.QuaternionDivisionRing
    val handedness = QuaternionBases.Handedness.RIGHT
    val basis = QuaternionBases.basis(quaternions, handedness)

    val one = quaternions.one
    val negOne = quaternions.add.inverse(one)
    val eq = eqQuaternionStrict

    check(eq(quaternions.mul(basis.i, basis.i), negOne))
    check(eq(quaternions.mul(basis.j, basis.j), negOne))
    check(eq(quaternions.mul(basis.k, basis.k), negOne))

    when (handedness) {
        QuaternionBases.Handedness.RIGHT -> {
            check(eq(quaternions.mul(basis.i, basis.j), basis.k))
            check(eq(quaternions.mul(basis.j, basis.i), quaternions.add.inverse(basis.k)))
        }
        QuaternionBases.Handedness.LEFT -> {
            check(eq(quaternions.mul(basis.i, basis.j), quaternions.add.inverse(basis.k)))
            check(eq(quaternions.mul(basis.j, basis.i), basis.k))
        }
    }
}
