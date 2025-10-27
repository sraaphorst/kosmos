package org.vorpal.kosmos.testutils

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.structures.instances.Vec2D
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
infix fun Vec2D.shouldBeApproximately(other: Vec2D) {
    this.x.shouldBeApproximately(other.x)
    this.y.shouldBeApproximately(other.y)
}

fun Vec2D.shouldBeZero() {
    x.shouldBeZero()
    y.shouldBeZero()
}

