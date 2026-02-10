package org.vorpal.kosmos.algebra.structures.instances.gaussian

import org.vorpal.kosmos.algebra.morphisms.RingMonomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.HasNormSq
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.instances.Quaternion
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras
import org.vorpal.kosmos.algebra.structures.instances.embeddings.AxisSignEmbeddings
import org.vorpal.kosmos.algebra.structures.instances.quaternion
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.gaussian.GaussianInt
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.UnaryOp
import org.vorpal.kosmos.core.rational.Rational
import org.vorpal.kosmos.core.rational.toRational
import java.math.BigInteger

/**
 * Hurwitz quaternions are quaternions where the coefficients of 1, i, j, and k are rational numbers,
 * and they must all be:
 * - integers
 * - integers / half-integers with even sum (e.g. 1 + i/2 + k/2)
 */
data class HurwitzQuaternion(
    val a: Rational,
    val b: Rational,
    val c: Rational,
    val d: Rational
) {
    init {
        require(isHurwitz(a, b, c, d)) {
            "Not a Hurwitz quaternion: coordinates must be all integers or all half-integers with even sum"
        }
    }

    companion object {
        // A quaternion (a, b, c, d) is Hurwitz iff:
        // - 2a, 2b, 2c, 2d are all integers
        // - 2a = 2b = 2c = 2d (mod 2)
        fun isHurwitz(a: Rational, b: Rational, c: Rational, d: Rational): Boolean {
            val doubled = listOf(a, b, c, d).map { it * BigInteger.TWO }
            if (!doubled.all(Rational::isInteger)) return false
            val p0 = doubled.first().numerator.mod(BigInteger.TWO)
            return doubled.all { it.numerator.mod(BigInteger.TWO) == p0 }
        }
    }
}

fun hurwitzQuaternion(w: Rational,
                      x: Rational,
                      y: Rational,
                      z: Rational): HurwitzQuaternion =
    HurwitzQuaternion(w, x, y, z)

val HurwitzQuaternion.w: Rational get() = a
val HurwitzQuaternion.x: Rational get() = b
val HurwitzQuaternion.y: Rational get() = c
val HurwitzQuaternion.z: Rational get() = d


object HurwitzQuaternionAlgebras {
    /**
     * The Hurwitz quaternions are:
     * - associative
     * - noncommutative
     * - involutive
     * - a finite unit group of size 24
     * - but do not form a division ring.
     *
     * However, they are the maximal order in the rational quaternion division algebra ℍ(ℚ):
     * An order in ℍ(ℚ) is a subring 𝒪 such that:
     * - Contains 1 and is closed under addition and multiplication
     * - Is a lattice (finitely generated ℤ-module of rank 4)
     * - Spans ℍ(ℚ) over ℚ (i.e., ℚ ⊗_ℤ 𝒪 = ℍ(ℚ)).
     *
     * The containment can be described as:
     * ```
     * Lipschitz (ring) ⊂ Hurwitz (ring) ⊂ ℍ(ℚ) (division ring) ⊂ ℍ (division ring)
     * ```
     */
    object HurwitzQuaternionRing:
        InvolutiveRing<HurwitzQuaternion>,
        HasNormSq<HurwitzQuaternion, Rational> {

        override val zero: HurwitzQuaternion = HurwitzQuaternion(
            Rational.ZERO, Rational.ZERO, Rational.ZERO, Rational.ZERO
        )
        override val one: HurwitzQuaternion = HurwitzQuaternion(
            Rational.ONE, Rational.ZERO, Rational.ZERO, Rational.ZERO
        )

        override val add: AbelianGroup<HurwitzQuaternion> = AbelianGroup.of(
            identity = zero,
            op = BinOp(Symbols.PLUS) { (w1, x1, y1, z1), (w2, x2, y2, z2) ->
                HurwitzQuaternion(w1 + w2, x1 + x2, y1 + y2, z1 + z2)
            },
            inverse = Endo(Symbols.MINUS) { (w, x, y, z) -> HurwitzQuaternion(-w, -x, -y, -z) }
        )

        // Multiplication is closed under Hurwitz quaternions.
        override val mul: Monoid<HurwitzQuaternion> = Monoid.of(
            identity = one,
            op = BinOp(Symbols.ASTERISK) { (w1, x1, y1, z1), (w2, x2, y2, z2) ->
                val w = w1 * w2 - x1 * x2 - y1 * y2 - z1 * z2
                val x = w1 * x2 + x1 * w2 + y1 * z2 - z1 * y2
                val y = w1 * y2 - x1 * z2 + y1 * w2 + z1 * x2
                val z = w1 * z2 + x1 * y2 - y1 * x2 + z1 * w2
                HurwitzQuaternion(w, x, y, z)
            }
        )

        override fun fromBigInt(n: BigInteger): HurwitzQuaternion =
            HurwitzQuaternion(n.toRational(), Rational.ZERO, Rational.ZERO, Rational.ZERO)

        // This always is a Hurwitz quaternion as doubling maintains integrality and parity mod 2
        // doesn't change under negation.
        override val conj: Endo<HurwitzQuaternion> = Endo(Symbols.CONJ) { (a, b, c, d) ->
            HurwitzQuaternion(a, -b, -c, -d)
        }

        // normSq always lands in Z:
        // 1. If they're already in Z, obvious.
        // 2. If they're all half-integers, then they all can be written n/2 with n odd.
        //    a^2 + b^2 + c^2 + d^2 = (n_a^2 + n_b^2 + n_c^2 + n_d^2) / 4
        //    with each odd square being 1 (mod 8), so the numerator is 4 (mod 8), hence divisible by 4.
        override val normSq: UnaryOp<HurwitzQuaternion, Rational> = UnaryOp(Symbols.NORM_SQ_SYMBOL) {
            (a, b, c, d) -> a * a + b * b + c * c + d * d
        }

        // This is equivalently (normSq(), 0, 0, 0).
        override val normElement: Endo<HurwitzQuaternion> = Endo(Symbols.NORM) { q ->
            mul(q, conj(q))
        }
    }

    private val canonicalEmbedding = AxisSignEmbeddings.AxisSignEmbedding.canonical

    /**
     * Create the [GaussianInt] -> [LipschitzQuaternion] monomorphism according to the [embedding] and then
     * apply the [LipschitzToHurwitzQuaternionMonomorphism] to get a [RingMonomorphism]:
     * ```kotlin
     * GaussianInt ↪ LipschitzQuaternion ↪ HurwitzQuaternion
     * ```
     */
    fun gaussianIntEmbeddingToHurwitz(
        embedding: AxisSignEmbeddings.AxisSignEmbedding = canonicalEmbedding
    ): RingMonomorphism<GaussianInt, HurwitzQuaternion> =
        LipschitzQuaternionAlgebras.gaussianIntEmbeddingToQuaternion(embedding) andThen
            LipschitzToHurwitzQuaternionMonomorphism

    /**
     * General mappings from the Lipschitz quaternion rings to the Hurwitz quaternion rings.
     */
    val LipschitzToHurwitzQuaternionMonomorphism: RingMonomorphism<LipschitzQuaternion, HurwitzQuaternion> =
        RingMonomorphism.of(
            domain = LipschitzQuaternionAlgebras.LipschitzQuaternionRing,
            codomain = HurwitzQuaternionRing,
            map = UnaryOp { lq ->
                hurwitzQuaternion(lq.w.toRational(), lq.x.toRational(), lq.y.toRational(), lq.z.toRational())
            }
        )

    /**
     * General mappings from the Hurwitz quaternion rings to the Quaternion ring.
     */
    val HurwitzToQuaternionMonomorphism: RingMonomorphism<HurwitzQuaternion, Quaternion> = RingMonomorphism.of(
        domain = HurwitzQuaternionRing,
        codomain = QuaternionAlgebras.QuaternionDivisionRing,
        map = UnaryOp { (a, b, c, d) ->
            val w = a.toReal()
            val x = b.toReal()
            val y = c.toReal()
            val z = d.toReal()
            quaternion(w, x, y, z)
        }
    )

    val eqHurwitzQuaternion: Eq<HurwitzQuaternion> = Eq { q1, q2 -> q1 == q2 }
}
