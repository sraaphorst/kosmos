package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.linear.instances.FixedVectorSpaces
import org.vorpal.kosmos.core.linear.values.Vec2
import org.vorpal.kosmos.core.math.Real

fun realCheck() {
    val a = Vec2(1.0, 2.0)
    val b = Vec2(3.0, 4.0)
    with (FixedVectorSpaces.vec2(RealField)) {
        val expected = Vec2(11.0, 16.0)
        val result = add(leftAction(2.0, a), leftAction(3.0, b))
        check(result == expected) { "Incorrect: expected $expected, but got $result" }
    }
}

fun quaternionCheck() {
    val quaternions = QuaternionAlgebras.QuaternionDivisionRing
    val handedness = HyperComplex.Handedness.RIGHT
    val basis = QuaternionBases.basis(quaternions, handedness)

    val one = quaternions.one
    val negOne = quaternions.add.inverse(one)
    val eq = eqQuaternionStrict

    check(eq(quaternions.mul(basis.i, basis.i), negOne))
    check(eq(quaternions.mul(basis.j, basis.j), negOne))
    check(eq(quaternions.mul(basis.k, basis.k), negOne))

    when (handedness) {
        HyperComplex.Handedness.RIGHT -> {
            check(eq(quaternions.mul(basis.i, basis.j), basis.k))
            check(eq(quaternions.mul(basis.j, basis.i), quaternions.add.inverse(basis.k)))
        }
        HyperComplex.Handedness.LEFT -> {
            check(eq(quaternions.mul(basis.i, basis.j), quaternions.add.inverse(basis.k)))
            check(eq(quaternions.mul(basis.j, basis.i), basis.k))
        }
    }
}

fun octonionCheck() {
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

fun main() {
    realCheck()
    quaternionCheck()
    octonionCheck()
}
