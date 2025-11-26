package org.vorpal.kosmos.algebra.structures.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeMonoid
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.InvolutiveRing
import org.vorpal.kosmos.algebra.structures.NormedDivisionAlgebra
import org.vorpal.kosmos.core.Identity
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.math.clamp
import org.vorpal.kosmos.core.math.lerp
import org.vorpal.kosmos.core.ops.Action
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.linear.Vec2R
import org.vorpal.kosmos.linear.Vec2R_ZERO

typealias Real = Double

object RealAlgebras {
    val RealField : Field<Real> = Field.of(
        add = AbelianGroup.of(
            identity = 0.0,
            op = BinOp(Symbols.PLUS, Real::plus),
            inverse = Endo(Symbols.MINUS, Real::unaryMinus)
        ),
        mul = CommutativeMonoid.of(
            identity = 1.0,
            op = BinOp(Symbols.PLUS, Real::times)
        ),
        reciprocal = Endo(Symbols.INVERSE) { 1.0 / it }
    )

    object RealInvolutiveRing:
        InvolutiveRing<Real>,
        Field<Real> by RealField,
        NormedDivisionAlgebra<Real> {

        override val conj: Endo<Real> =
            Endo("conj", Identity())

        override fun normSq(a: Real): Real =
            a * a

        // Disambiguate HasReciprocal.zero:
        override val zero: Real
            get() = RealField.zero
    }

    /**
     * 2D vector space over doubles.
     */
    object Vec2RSpace : FiniteVectorSpace<Real, Vec2R> {
        override val ring: Field<Real> = RealField
        override val dimension: Int = 2

        override val group: AbelianGroup<Vec2R> = AbelianGroup.of(
            identity = Vec2R_ZERO,
            op = BinOp(Symbols.PLUS) { a, b -> Vec2R(a.x + b.x, a.y + b.y) },
            inverse = Endo(Symbols.MINUS) { Vec2R(-it.x, -it.y) }
        )

        override val action: Action<Real, Vec2R> = Action(Symbols.ASTERISK) { scalar, vec ->
            Vec2R(scalar * vec.x, scalar * vec.y)
        }
    }
}

// ============================================================================
// Helper Extensions
// ============================================================================

/**
 * Dot product for Vec2R (not part of VectorSpace interface, but useful for testing).
 */
infix fun Vec2R.dot(other: Vec2R): Real =
    this.x * other.x + this.y * other.y

/**
 * Magnitude (norm) of Vec2R.
 */
val Vec2R.magnitude: Real
    get() = kotlin.math.sqrt(x * x + y * y)

/**
 * Normalized version of Vec2R.
 */
fun Vec2R.normalize(): Vec2R {
    val mag = magnitude
    return if (mag > 0.0) Vec2R(x / mag, y / mag) else Vec2R_ZERO
}

/**
 * Linearly interpolate from the calling vector to the to vector for t.
 * If t is in [0, 1], the value will range linearly between this and to.
 * If t < 0 or t > 1, the values will fall outside the vector range.
 */
fun Vec2R.lerp(to: Vec2R, t: Real): Vec2R =
    Vec2R(lerp(x, to.x, t), lerp(y, to.y, t))

/**
 * Linearly interpolate from the calling vector to the [to] vector for [t] in [0, 1].
 * The value of t is clamped, so the vector will always be between this and [to].
 */
fun Vec2R.lerpClamped(to: Vec2R, t: Real): Vec2R =
    lerp(to, clamp(t, 0.0, 1.0))
