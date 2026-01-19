package org.vorpal.kosmos.testutils

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.vorpal.kosmos.algebra.extensions.Dual
import org.vorpal.kosmos.core.math.Real
import org.vorpal.kosmos.linear.values.Vec2
import kotlin.math.abs

/**
 * Compares two Reals with a relative tolerance (default 1e-9).
 * Works well for both small and large magnitudes.
 */
infix fun Real.shouldBeApproximately(
    other: Real
) = this.shouldBeApproximately(other, 1e-9, 1e-12)

/**
 * Variant with custom tolerance.
 */
fun Real.shouldBeApproximately(other: Real, relTol: Real, absTol: Real) {
    if (this.isFinite() && other.isFinite()) {
        val safeThis = if (abs(this) < 1e-300) 0.0 else this
        val safeOther = if (abs(other) < 1e-300) 0.0 else other

        // Compute effective tolerance
        val tol = maxOf(absTol, relTol * maxOf(1.0, abs(this), abs(other)))
        safeThis shouldBe (safeOther plusOrMinus tol)
    }
}

fun Real.shouldBeZero() {
    abs(this) shouldBeApproximately 0.0
}

// Helper to compare Vec2D with tolerance
infix fun Vec2<Real>.shouldBeApproximately(other: Vec2<Real>) {
    this.x.shouldBeApproximately(other.x)
    this.y.shouldBeApproximately(other.y)
}

fun Vec2<Real>.shouldBeZero() {
    x.shouldBeZero()
    y.shouldBeZero()
}

/**
 * Compare two dual numbers with tolerance.
 */
infix fun Dual<Real>.shouldBeApproximately(other: Dual<Real>) {
    a shouldBeApproximately other.a
    b shouldBeApproximately other.b
}
