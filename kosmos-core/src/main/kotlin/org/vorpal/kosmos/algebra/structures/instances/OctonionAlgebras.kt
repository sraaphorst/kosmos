package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.QuaternionDivisionRing
import org.vorpal.kosmos.algebra.structures.CD
import org.vorpal.kosmos.algebra.structures.CayleyDickson
import org.vorpal.kosmos.algebra.structures.InvolutiveAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeDivisionAlgebra
import org.vorpal.kosmos.algebra.structures.NonAssociativeMonoid
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.algebra.structures.instances.OctonionAlgebras.normSq
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.QuaternionModule
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.normSq
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
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

    fun Octonion.normSq(): Real =
        a.normSq() + b.normSq()

    /**
     * In this case, the most we can say about the Octonions are that they are an [NonAssociativeDivisionAlgebra].
     *
     * We get everything but the reciprocal from the [InvolutiveAlgebra] returned by the [CayleyDickson] construction.
     */
    object OctonionDivisionAlgebra : NonAssociativeDivisionAlgebra<Octonion> {
        private val base = CayleyDickson(QuaternionDivisionRing)
        override val add: AbelianGroup<Octonion> = base.add
        override val mul: NonAssociativeMonoid<Octonion> = base.mul
        override fun fromBigInt(n: BigInteger) = base.fromBigInt(n)
        override val conj: Endo<Octonion> = base.conj
        override val reciprocal: Endo<Octonion> = Endo(Symbols.SLASH) { o ->
            val n2: Real = o.normSq()
            require(eqRealApprox.neqv(n2, 0.0) && n2.isFinite()) { "$n2 has no multiplicative inverse in ${Symbols.BB_O}."}

            val oc = conj(o)
            val scale: Real = 1.0 / n2

            // Use the QuaternionModule's action to scale.
            // We could use OctonionModule, but we fall back to QuaternionModule to avoid circular dependencies.
            Octonion(
                QuaternionModule.leftAction(scale, oc.a),
                QuaternionModule.leftAction(scale, oc.b)
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
    }

    /**
     * This is the only module from the Cayley-Dickson tower we can define for the octonions, since
     * the reals lie in the center, so scalar multiplication is safe.
     */
    val OctonionModule: RModule<Real, Octonion> = RModule.of(
        scalars = RealField,
        group = OctonionDivisionAlgebra.add,
        leftAction = LeftAction { r, o -> octonion(
            r * o.w, r * o.x, r * o.y, r * o.z,
            r * o.u, r * o.v, r * o.s, r * o.t)
        }
    )

    /**
     * Embed a quaternion number into an octonion.
     */
    fun Quaternion.asOctonion(): Octonion =
        octonion(w, x, y, z, 0.0, 0.0, 0.0, 0.0)
}

val eqOctonionStrict: Eq<Octonion> = CD.eq(eqQuaternionStrict)
val eqOctonion: Eq<Octonion> = CD.eq(eqQuaternion)

fun main() {
    val O = OctonionAlgebras.OctonionDivisionAlgebra
    val add = O.add
    val mul = O.mul
    val conj = O.conj
    val inv = O.reciprocal

    val eqO = eqOctonionStrict
    val eqR = Eqs.realApprox()

    fun normSq(o: Octonion): Real = OctonionAlgebras.run { o.normSq() }

    val zero = add.identity
    val one = O.one
    val negOne = add.inverse(one)

    // Basis elements
    val e1 = O.e1
    val e2 = O.e2
    val e3 = O.e3
    val e4 = O.e4
    val e5 = O.e5
    val e6 = O.e6
    val e7 = O.e7

    // --- 1) Basic identities ---
    check(eqO(mul.op(one, one), one))
    check(eqO(mul.op(one, e1), e1))
    check(eqO(mul.op(e1, one), e1))
    check(eqO(add.op(e1, add.inverse(e1)), zero))

    // --- 2) Conjugation is an involution: conj(conj(x)) = x ---
    run {
        val x = octonion(1.0, 2.0, -3.0, 4.0, -5.0, 6.0, -7.0, 8.0)
        check(eqO(conj(conj(x)), x))
    }

    // --- 3) Pure imaginaries square to -1 (for basis units) ---
    listOf(e1, e2, e3, e4, e5, e6, e7).forEachIndexed { idx, ei ->
        val sq = mul.op(ei, ei)
        check(eqO(sq, negOne)) { "e${idx + 1}^2 expected -1, got $sq" }
    }

    // --- 4) Norm is real, nonnegative, and multiplicative (within tolerance) ---
    run {
        val x = octonion(1.25, -2.0, 0.5, 3.0, -1.0, 0.25, 4.0, -0.75)
        val y = octonion(-0.5, 1.5, -2.25, 0.0, 3.75, -1.0, 0.5, 2.0)

        val nx = normSq(x)
        val ny = normSq(y)
        val nxy = normSq(mul.op(x, y))

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
        val O = OctonionAlgebras.OctonionDivisionAlgebra
        val one = O.one
        val diff = O.add.op(o, O.add.inverse(one))
        // compare squared norm to tol^2
        return diff.normSq() <= tol * tol
    }

    // --- 5) Inverse via conjugate: x^{-1} = conj(x)/n(x) and x*x^{-1} = 1 (within tolerance) ---
    run {
        val x = octonion(0.5, -1.0, 2.0, 0.25, -0.75, 1.5, -2.5, 3.0)
        val nx = normSq(x)
        check(eqR.neqv(nx, 0.0))

        val xinverse = inv(x)
        val left = mul.op(xinverse, x)
        val right = mul.op(x, xinverse)

        // Because of nonassociativity, left and right inverse still exist in 𝕆 (it’s alternative),
        // but numerics might drift: compare to 1 with tolerance on components.
        check(isOneApprox(left)) { "x^{-1}x expected 1, got $left" }
        check(isOneApprox(right)) { "xx^{-1} expected 1, got $right" }
    }

    // --- 6) Alternativity sanity checks (these SHOULD hold): (xx)y = x(xy), and y(xx) = (yx)x ---
    run {
        val x = octonion(1.0, 2.0, 0.0, -1.0, 0.5, 0.0, 0.0, 0.25)
        val y = octonion(-0.5, 0.0, 1.5, 2.0, 0.0, -1.0, 0.75, 0.0)

        val xx = mul.op(x, x)

        val leftAlt1 = mul.op(xx, y)
        val rightAlt1 = mul.op(x, mul.op(x, y))
        check(eqO(leftAlt1, rightAlt1)) { "(xx)y != x(xy): $leftAlt1 vs $rightAlt1" }

        val leftAlt2 = mul.op(y, xx)
        val rightAlt2 = mul.op(mul.op(y, x), x)
        check(eqO(leftAlt2, rightAlt2)) { "y(xx) != (yx)x: $leftAlt2 vs $rightAlt2" }
    }

    // --- 7) Nonassociativity witness (this SHOULD FAIL to be equal in general) ---
    run {
        val left = mul.op(mul.op(e1, e2), e4)
        val right = mul.op(e1, mul.op(e2, e4))

        check(!eqO(left, right)) {
            "Expected a nonassociativity witness, but got equality: left=$left right=$right"
        }
    }

    println("Octonion tests passed ✅ (including a deliberate nonassociativity witness)")
}