package org.vorpal.kosmos.algebra.extensions

import org.vorpal.kosmos.algebra.structures.instances.RealAlgebras
import org.vorpal.kosmos.core.math.Real
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

fun main() {
    fun approxEq(a: Real, b: Real, eps: Real = 1e-10): Boolean =
        abs(a - b) <= eps

    // 1) f(x) = x^5 + 2x + 7
    run {
        fun f(x: Dual<Real>): Dual<Real> {
            val x5 = DualRealFns.pow(x, 5)
            val twoX = DualRing(RealAlgebras.RealField).mul.op(DualRing(RealAlgebras.RealField).lift(2.0), x) // meh
            // Better: do it with the ring ops directly (see below) or add scalar*dual helpers later.
            // For now, simplest: x + x style:
            val d = RealAlgebras.RealField.dual()
            val twoX2 = d.add(x, x)
            val poly = d.add(x5, twoX2)
            return d.add(poly, d.lift(7.0))
        }

        val x = 1.3
        val (fx, dfx) = diffAt(::f, x)

        val expectedFx = x * x * x * x * x + 2.0 * x + 7.0
        val expectedDfx = 5.0 * x * x * x * x + 2.0

        check(approxEq(fx, expectedFx)) { "poly value mismatch: got $fx expected $expectedFx" }
        check(approxEq(dfx, expectedDfx)) { "poly deriv mismatch: got $dfx expected $expectedDfx" }
    }

    // 2) f(x) = sin(x)
    run {
        fun f(x: Dual<Real>): Dual<Real> =
            DualRealFns.sin(x)

        val x = 0.7
        val (fx, dfx) = diffAt(::f, x)

        check(approxEq(fx, sin(x))) { "sin value mismatch: got $fx expected ${sin(x)}" }
        check(approxEq(dfx, cos(x))) { "sin deriv mismatch: got $dfx expected ${cos(x)}" }
    }

    // 3) f(x) = exp(sin(x))  (tests chain rule)
    run {
        fun f(x: Dual<Real>): Dual<Real> =
            DualRealFns.exp(DualRealFns.sin(x))

        val x = 0.4
        val (fx, dfx) = diffAt(::f, x)

        val expectedFx = exp(sin(x))
        val expectedDfx = exp(sin(x)) * cos(x)

        check(approxEq(fx, expectedFx)) { "exp(sin) value mismatch: got $fx expected $expectedFx" }
        check(approxEq(dfx, expectedDfx)) { "exp(sin) deriv mismatch: got $dfx expected $expectedDfx" }
    }

    // 4) f(x) = log(x) at x=2
    run {
        fun f(x: Dual<Real>): Dual<Real> =
            DualRealFns.log(x)

        val x = 2.0
        val (fx, dfx) = diffAt(::f, x)

        val expectedFx = kotlin.math.ln(x)
        val expectedDfx = 1.0 / x

        check(approxEq(fx, expectedFx)) { "log value mismatch: got $fx expected $expectedFx" }
        check(approxEq(dfx, expectedDfx)) { "log deriv mismatch: got $dfx expected $expectedDfx" }
    }

    println("Dual diffAt tests passed ✅")
}
