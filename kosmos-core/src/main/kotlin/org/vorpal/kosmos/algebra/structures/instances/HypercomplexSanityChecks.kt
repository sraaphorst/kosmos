package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.instances.OctonionAlgebras.eqOctonionStrict
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.eqQuaternion
import org.vorpal.kosmos.algebra.structures.instances.QuaternionAlgebras.eqQuaternionStrict
import org.vorpal.kosmos.algebra.structures.instances.ComplexAlgebras.ComplexField
import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras.RealField
import org.vorpal.kosmos.algebra.structures.instances.embeddings.AxisSignEmbeddings
import org.vorpal.kosmos.core.Eqs
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.linear.instances.FixedTupleAlgebras
import org.vorpal.kosmos.linear.values.Vec2

fun realCheck() {
    val a = Vec2(1.0, 2.0)
    val b = Vec2(3.0, 4.0)
    with (FixedTupleAlgebras.vec2Space(RealField)) {
        val expected = Vec2(11.0, 16.0)
        val result = add(leftAction(2.0, a), leftAction(3.0, b))
        check(result == expected) { "Incorrect: expected $expected, but got $result" }
    }
}


fun quaternionCheck() {
    val quaternionRing = QuaternionAlgebras.QuaternionDivisionRing
    val eqQ = eqQuaternion

    // Canonical Cayley–Dickson basis units for ℍ = CD(ℂ)
    // 1 = (1, 0), I = (i_C, 0), J = (0, 1), K = (0, i_C)
    val one = quaternionRing.one
    val negOne = quaternionRing.add.inverse(one)

    val iC = ComplexField.i
    val zeroC = ComplexField.zero
    val oneC = ComplexField.one

    val iAxis: Quaternion = Quaternion(iC, zeroC)
    val jAxis: Quaternion = Quaternion(zeroC, oneC)
    val kAxis: Quaternion = Quaternion(zeroC, iC)

    // Squares
    check(eqQ(quaternionRing.mul(iAxis, iAxis), negOne)) { "i^2 != -1" }
    check(eqQ(quaternionRing.mul(jAxis, jAxis), negOne)) { "j^2 != -1" }
    check(eqQ(quaternionRing.mul(kAxis, kAxis), negOne)) { "k^2 != -1" }

    // Orientation: I J = K and J I = -K (fixed by the CD convention)
    check(eqQ(quaternionRing.mul(iAxis, jAxis), kAxis)) { "i * j != k" }
    check(eqQ(quaternionRing.mul(jAxis, iAxis), quaternionRing.add.inverse(kAxis))) { "j * i != -k" }

    // The rest (optional, but nice sanity)
    check(eqQ(quaternionRing.mul(jAxis, kAxis), iAxis)) { "j * k != i" }
    check(eqQ(quaternionRing.mul(kAxis, jAxis), quaternionRing.add.inverse(iAxis))) { "k * j != -i" }

    check(eqQ(quaternionRing.mul(kAxis, iAxis), jAxis)) { "k * i != j" }
    check(eqQ(quaternionRing.mul(iAxis, kAxis), quaternionRing.add.inverse(jAxis))) { "i * k != -j" }
}


fun quaternionEmbeddingCheck() {
    val quaternionRing = QuaternionAlgebras.QuaternionDivisionRing
    val eqQ = eqQuaternionStrict

    val iC = ComplexField.i

    // In ℍ = CD(ℂ), these are the canonical Cayley–Dickson basis units:
    val iAxis: Quaternion = Quaternion(ComplexField.i, ComplexField.zero)
    val jAxis: Quaternion = Quaternion(ComplexField.zero, ComplexField.one)
    val kAxis: Quaternion = Quaternion(ComplexField.zero, ComplexField.i)

    AxisSignEmbeddings.AxisSignEmbedding.all.forEach { embedding ->
        val embed = QuaternionAlgebras.complexEmbeddingToQuaternion(embedding)
        val image = embed(iC) // should be ±I/±J/±K depending on spec

        val axisUnit = when (embedding.axis) {
            AxisSignEmbeddings.ImagAxis.I -> iAxis
            AxisSignEmbeddings.ImagAxis.J -> jAxis
            AxisSignEmbeddings.ImagAxis.K -> kAxis
        }

        val expected =
            if (embedding.sign == AxisSignEmbeddings.Sign.PLUS) axisUnit
            else quaternionRing.add.inverse(axisUnit)

        check(eqQ(image, expected)) { "embed(i_C) mismatch for $embedding: got $image, expected $expected" }
    }
}

fun octonionCheck() {
    val octonions = OctonionAlgebras.OctonionDivisionAlgebraReal
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
