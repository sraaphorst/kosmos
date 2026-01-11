package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.morphisms.NonAssociativeRingHomomorphism
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.QuaternionDivisionRing
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.NonAssociativeInvolutiveRing
import org.vorpal.kosmos.algebra.structures.NonAssociativeDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.NonAssociativeStarAlgebra
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.QuaternionRealVectorSpace
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.ops.UnaryOp
import java.math.BigInteger

typealias Octonion = CD<Quaternion>

val Octonion.w: Real get() = a.w
val Octonion.x: Real get() = a.x
val Octonion.y: Real get() = a.y
val Octonion.z: Real get() = a.z

val Octonion.u: Real get() = b.w
val Octonion.v: Real get() = b.x
val Octonion.s: Real get() = b.y
val Octonion.t: Real get() = b.z

fun octonion(
    w: Real, x: Real, y: Real, z: Real,
    u: Real, v: Real, s: Real, t: Real
): Octonion {
    val a = quaternion(w, x, y, z)
    val b = quaternion(u, v, s, t)
    return Octonion(a, b)
}

object OctonionAlgebras {
    private val eqRealApprox = Eqs.realApprox()

    /**
     * In this case, the most we can say about the Octonions are that they are an [NonAssociativeDivisionAlgebra].
     *
     * We get everything but the reciprocal from the [NonAssociativeInvolutiveRing] returned by the [CayleyDickson] construction.
     */
    object OctonionDivisionAlgebra : NormedDivisionAlgebra<Octonion> {
        private val base = CayleyDickson(QuaternionDivisionRing)
        override val add: AbelianGroup<Octonion> = base.add
        override val mul: NonAssociativeMonoid<Octonion> = base.mul
        override fun fromBigInt(n: BigInteger) = base.fromBigInt(n)
        override val conj: Endo<Octonion> = base.conj

        // The only thing that makes octonions invertible is that their norm is a composition norm.
        override val reciprocal: Endo<Octonion> = Endo(Symbols.SLASH) { o ->
            val n2: Real = normSq(o)
            require(eqRealApprox.neqv(n2, 0.0) && n2.isFinite()) { "$n2 has no multiplicative inverse in ${Symbols.BB_O}."}

            val oc = conj(o)
            val scale: Real = 1.0 / n2

            // Use the QuaternionModule's action to scale.
            // We could use OctonionModule, but we fall back to QuaternionModule to avoid circular dependencies.
            Octonion(
                QuaternionRealVectorSpace.leftAction(scale, oc.a),
                QuaternionRealVectorSpace.leftAction(scale, oc.b)
            )
        }
        override val one = mul.identity                                                             // quaternion 1 in "a"
        val e1 = octonion(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) // quaternion i in "a"
        val e2 = octonion(0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0) // quaternion j in "a"
        val e3 = octonion(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0) // quaternion k in "a"
        val e4 = octonion(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0) // quaternion 1 in "b"
        val e5 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0) // quaternion i in "b"
        val e6 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0) // quaternion j in "b"
        val e7 = octonion(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0) // quaternion k in "b"
        val basisMap = mapOf(0 to one, 1 to e1, 2 to e2, 3 to e3, 4 to e4, 5 to e5, 6 to e6, 7 to e7)

        override val normSq: UnaryOp<Octonion, Real> =
            UnaryOp(Symbols.NORM_SQ_SYMBOL) {o -> mul(o, conj(o)).a.w }
    }

    /**
     * This is the only module from the Cayley-Dickson tower we can define for the octonions, since
     * the reals lie in the center, so scalar multiplication is safe.
     */
    val OctonionVectorSpace: VectorSpace<Real, Octonion> = VectorSpace.of(
        RealField,
        OctonionDivisionAlgebra.add,
        LeftAction { r, o -> octonion(
            r * o.w, r * o.x, r * o.y, r * o.z,
            r * o.u, r * o.v, r * o.s, r * o.t)
        }
    )

    object OctonionStarAlgebra:
        NonAssociativeStarAlgebra<Real, Octonion>,
        NonAssociativeDivisionAlgebra<Octonion> by OctonionDivisionAlgebra,
        VectorSpace<Real, Octonion> by OctonionVectorSpace {
            // Disambiguate zero.
            override val zero = OctonionDivisionAlgebra.zero
        }

    /**
     * Embed a quaternion number into an octonion.
     * Note that this corresponds to the first homomorphism from Quaternion to Octonion in the list generated below.
     */
    fun Quaternion.asOctonion(): Octonion =
        octonion(w, x, y, z, 0.0, 0.0, 0.0, 0.0)

    /**
     * These are the lines of the Fano plane we use to determine the monomorphisms `ℍ ↪ 𝕆`.
     */
    data class FanoLine(val a: Int, val b: Int, val c: Int) {
        operator fun contains(i: Int) = i == a || i == b || i == c
        fun pairs(): List<Pair<Int, Int>> = listOf(a to b, a to c, b to a, b to c, c to a, c to b)
    }

    val FanoCycles: Set<FanoLine> = setOf(
        FanoLine(1, 2, 3),
        FanoLine(1, 4, 5),
        FanoLine(1, 6, 7),
        FanoLine(2, 4, 6),
        FanoLine(2, 5, 7),
        FanoLine(3, 4, 7),
        FanoLine(3, 5, 6)
    )

    /**
     * Specification describing a (non-associative) ring monomorphism ℍ ↪ 𝕆 generated by a choice of
     * images for the quaternion units `i` and `j` among the octonion basis units `e₁,…,e₇`.
     *
     * Parameters:
     * - [i]: the basis index so that `φ(i) = ±e_i` (sign determined by [handedness]).
     * - [j]: the basis index so that `φ(j) = e_j`.
     * - [k]: the third index on the unique Fano line containing [i] and [j].
     *        The image of `k` is not chosen independently; it is forced by multiplication: `φ(k) = φ(i)·φ(j)`.
     * - [handedness]: RIGHT uses `φ(i) = e_i`; LEFT uses `φ(i) = -e_i` (flipping orientation).
     * - [kSign]: `+1` if `φ(k) =  e_k`, and `-1` if `φ(k) = -e_k`.
     */
    data class Spec(val i: Int,
                    val j: Int,
                    val k: Int,
                    val handedness: HyperComplex.Handedness,
                    val kSign: Int)

    /**
     * Construct one of the canonical “basis-unit” embeddings `φ : ℍ ↪ 𝕆` determined by a choice of
     * octonion basis units for the quaternion generators `i` and `j`.
     *
     * We choose two distinct indices [iIndex], [jIndex] in `{1,…,7}` that lie on a common Fano line.
     * Let `e₁,…,e₇` be the chosen imaginary basis units of 𝕆. We define:
     *
     *    φ(1) = 1
     *    φ(i) =  e_{iIndex}            (RIGHT)
     *    φ(i) = -e_{iIndex}            (LEFT)   // orientation flip, matching quaternion “handedness”
     *    φ(j) =  e_{jIndex}
     *    φ(k) = φ(i)·φ(j)              // forced by ij = k in ℍ
     *
     * The third index [kIndex] is the remaining point on the (unordered) Fano line containing
     * {iIndex, jIndex}; the actual image of k is ±e_{kIndex}, where the sign is recorded as [kSign].
     *
     * Returns:
     * - a [Spec] describing the embedding (including [kSign])
     * - the corresponding [NonAssociativeRingHomomorphism] ℍ ↪ 𝕆
     *
     * Notes:
     * - The embedding is constructed to respect multiplication by defining `φ(k)` as `φ(i)·φ(j)`.
     * - A sanity check asserts that `φ(i)·φ(j)` lands on `±e_{kIndex}`; if this fails, the chosen Fano
     *   incidence structure disagrees with the current multiplication table / basis convention.
     */
    fun createOctonionEmbedding(
        iIndex: Int,
        jIndex: Int,
        handedness: HyperComplex.Handedness,
        eq: Eq<Octonion> = eqOctonionStrict
    ): Pair<Spec, NonAssociativeRingHomomorphism<Quaternion, Octonion>> {
        require(iIndex in 1..7) { "iIndex must be in [1, 7], got $iIndex" }
        require(jIndex in 1..7) { "jIndex must be in [1, 7], got $jIndex" }
        require(iIndex != jIndex) { "iIndex and jIndex must be distinct, got $iIndex" }

        // Exception should never happen here.
        val line = FanoCycles.find { line -> iIndex in line && jIndex in line } ?:
            throw RuntimeException("No line containing $iIndex and $jIndex")

        // Find the image of k.
        val kIndex = (setOf(line.a, line.b, line.c) - setOf(iIndex, jIndex)).first()

        val oda = OctonionDivisionAlgebra
        val one = oda.one
        val ei = oda.basisMap.getValue(iIndex)
        val di = when (handedness) {
            HyperComplex.Handedness.RIGHT -> ei
            HyperComplex.Handedness.LEFT -> oda.add.inverse(ei)
        }
        val dj = oda.basisMap.getValue(jIndex)

        // If we flipped i, k flips automatically, which is what we want.
        val dk = oda.mul(di, dj)

        // Sanity check: dk matches the remaining basis unit up to sign.
        // This ensures that any discrepancies in the Fano plane are caught early.
        val dk0 = oda.basisMap.getValue(kIndex)
        val negDk0 = oda.add.inverse(dk0)
        val dkOk = eq(dk, dk0) || eq(dk, negDk0)
        require(dkOk) {
            "Sanity failed: e$iIndex * e$jIndex is not ±e$kIndex. " +
                "Got dk=$dk, but expected ±$dk0. Multiplication table / Fano plane mismatch."
        }

        val kSign = when {
            eq(dk, dk0) -> +1
            eq(dk, negDk0) -> -1
            else -> error("Sanity failed: dk is not ±dk")
        }

        val spec = Spec(iIndex, jIndex, kIndex, handedness, kSign)
        val ovs = OctonionVectorSpace
        return spec to NonAssociativeRingHomomorphism.of(
            QuaternionDivisionRing,
            OctonionDivisionAlgebra)
            { q ->
                val t1 = oda.add(ovs.leftAction(q.w, one), ovs.leftAction(q.x, di))
                val t2 = oda.add(ovs.leftAction(q.y, dj), ovs.leftAction(q.z, dk))
                oda.add(t1, t2)
            }
    }

    /**
     * Enumerate the full family of 84 “basis-unit” quaternion embeddings `ℍ ↪ 𝕆` arising from:
     *
     * - 7 unordered Fano lines in the standard Fano plane on `{1,…,7}`
     * - 6 ordered pairs `(i,j)` per line (choice of images for quaternion basis vectors `i` and `j`)
     * - 2 handedness choices (RIGHT: `i ↦ e_i`, LEFT: `i ↦ -e_i`), which flips the induced k-image sign
     *
     *
     *     Total: 7 * 6 * 2 = 84 embeddings.
     *
     * The result is keyed by [Spec] so callers can deterministically select an embedding and also
     * inspect the induced [kSign]. A duplicate-key check is included as a guard against bugs in
     * enumeration or spec construction.
     */
    fun allQuaternionEmbeddings(): Map<Spec, NonAssociativeRingHomomorphism<Quaternion, Octonion>> =
        buildMap {
            FanoCycles.forEach { line ->
                line.pairs().forEach { (i, j) ->
                    HyperComplex.Handedness.entries.forEach { handedness ->
                        val (spec, hom) = createOctonionEmbedding(i, j, handedness)
                        require(spec !in this) { "Duplicate embedding spec: $spec" }
                        put(spec, hom)
                    }
                }
            }
        }

}

val eqOctonionStrict: Eq<Octonion> = CD.eq(eqQuaternionStrict)
val eqOctonion: Eq<Octonion> = CD.eq(eqQuaternion)

fun main() {
    val octonions = OctonionAlgebras.OctonionDivisionAlgebra
    val add = octonions.add
    val mul = octonions.mul
    val conj = octonions.conj
    val inv = octonions.reciprocal

    val eqO = eqOctonionStrict
    val eqR = Eqs.realApprox()

    fun normSq(o: Octonion): Real = run { octonions.normSq(o) }

    val zero = add.identity
    val one = octonions.one
    val negOne = add.inverse(one)

    // Basis elements
    val e1 = octonions.e1
    val e2 = octonions.e2
    val e3 = octonions.e3
    val e4 = octonions.e4
    val e5 = octonions.e5
    val e6 = octonions.e6
    val e7 = octonions.e7

    // --- 1) Basic identities ---
    check(eqO(mul(one, one), one))
    check(eqO(mul(one, e1), e1))
    check(eqO(mul(e1, one), e1))
    check(eqO(add(e1, add.inverse(e1)), zero))

    // --- 2) Conjugation is an involution: conj(conj(x)) = x ---
    run {
        val x = octonion(1.0, 2.0, -3.0, 4.0, -5.0, 6.0, -7.0, 8.0)
        check(eqO(conj(conj(x)), x))
    }

    // --- 3) Pure imaginaries square to -1 (for basis units) ---
    listOf(e1, e2, e3, e4, e5, e6, e7).forEachIndexed { idx, ei ->
        val sq = mul(ei, ei)
        check(eqO(sq, negOne)) { "e${idx + 1}^2 expected -1, got $sq" }
    }

    // --- 4) Norm is real, nonnegative, and multiplicative (within tolerance) ---
    run {
        val x = octonion(1.25, -2.0, 0.5, 3.0, -1.0, 0.25, 4.0, -0.75)
        val y = octonion(-0.5, 1.5, -2.25, 0.0, 3.75, -1.0, 0.5, 2.0)

        val nx = normSq(x)
        val ny = normSq(y)
        val nxy = normSq(mul(x, y))

        check(nx.isFinite() && ny.isFinite() && nxy.isFinite())
        check(nx >= 0.0 && ny >= 0.0 && nxy >= 0.0)

        // n(xy) = n(x)n(y)
        check(eqR(nxy, nx * ny)) {
            "normSq multiplicativity failed: n(xy)=$nxy, n(x)n(y)=${nx * ny}"
        }
    }

    fun isOneApprox(
        o: Octonion,
        tol: Real = 1e-12
    ): Boolean {
        val one = octonions.one
        val diff = octonions.add(o, octonions.add.inverse(one))
        // compare squared norm to tol^2
        return octonions.normSq(diff) <= tol * tol
    }

    // --- 5) Inverse via conjugate: x^{-1} = conj(x)/n(x) and x*x^{-1} = 1 (within tolerance) ---
    run {
        val x = octonion(0.5, -1.0, 2.0, 0.25, -0.75, 1.5, -2.5, 3.0)
        val nx = normSq(x)
        check(eqR.neqv(nx, 0.0))

        val xinverse = inv(x)
        val left = mul(xinverse, x)
        val right = mul(x, xinverse)

        // Because of nonassociativity, left and right inverse still exist in 𝕆 (it’s alternative),
        // but numerics might drift: compare to 1 with tolerance on components.
        check(isOneApprox(left)) { "x^{-1}x expected 1, got $left" }
        check(isOneApprox(right)) { "xx^{-1} expected 1, got $right" }
    }

    // --- 6) Alternativity sanity checks (these SHOULD hold): (xx)y = x(xy), and y(xx) = (yx)x ---
    run {
        val x = octonion(1.0, 2.0, 0.0, -1.0, 0.5, 0.0, 0.0, 0.25)
        val y = octonion(-0.5, 0.0, 1.5, 2.0, 0.0, -1.0, 0.75, 0.0)

        val xx = mul(x, x)

        val leftAlt1 = mul(xx, y)
        val rightAlt1 = mul(x, mul(x, y))
        check(eqO(leftAlt1, rightAlt1)) { "(xx)y != x(xy): $leftAlt1 vs $rightAlt1" }

        val leftAlt2 = mul(y, xx)
        val rightAlt2 = mul(mul(y, x), x)
        check(eqO(leftAlt2, rightAlt2)) { "y(xx) != (yx)x: $leftAlt2 vs $rightAlt2" }
    }

    // --- 7) Nonassociativity witness (this SHOULD FAIL to be equal in general) ---
    run {
        val left = mul(mul(e1, e2), e4)
        val right = mul(e1, mul(e2, e4))

        check(!eqO(left, right)) {
            "Expected a nonassociativity witness, but got equality: left=$left right=$right"
        }
    }

    println("Sanity check: ${OctonionAlgebras.allQuaternionEmbeddings().size} should be 84.")
    println("Octonion tests passed ✅ (including a deliberate nonassociativity witness)")
}
