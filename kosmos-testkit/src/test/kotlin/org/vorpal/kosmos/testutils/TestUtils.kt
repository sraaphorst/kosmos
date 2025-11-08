package org.vorpal.kosmos.testutils

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.extensions.DualRing
import org.vorpal.kosmos.linear.Vec2R
import kotlin.math.abs

/**
 * Compares two Doubles with a relative tolerance (default 1e-9).
 * Works well for both small and large magnitudes.
 */
infix fun Double.shouldBeApproximately(
    other: Double
) = this.shouldBeApproximately(other, 1e-9, 1e-12)

/**
 * Variant with custom tolerance.
 */
fun Double.shouldBeApproximately(other: Double, relTol: Double, absTol: Double) {
    if (this.isFinite() && other.isFinite()) {
        val safeThis = if (abs(this) < 1e-300) 0.0 else this
        val safeOther = if (abs(other) < 1e-300) 0.0 else other

        // Compute effective tolerance
        val tol = maxOf(absTol, relTol * maxOf(1.0, abs(this), abs(other)))
        safeThis shouldBe (safeOther plusOrMinus tol)
    }
}

fun Double.shouldBeZero() {
    abs(this) shouldBeApproximately 0.0
}

// Helper to compare Vec2D with tolerance
infix fun Vec2R.shouldBeApproximately(other: Vec2R) {
    this.x.shouldBeApproximately(other.x)
    this.y.shouldBeApproximately(other.y)
}

fun Vec2R.shouldBeZero() {
    x.shouldBeZero()
    y.shouldBeZero()
}

/**
 * Compare two dual numbers with tolerance.
 */
infix fun DualRing<Double>.Dual.shouldBeApproximately(other: DualRing<Double>.Dual) {
    a shouldBeApproximately other.a
    b shouldBeApproximately other.b
}
