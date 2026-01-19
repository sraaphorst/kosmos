package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.math.Real
import java.math.BigInteger

/**
 * Provides generic finite-difference directional derivatives and differentials
 * for [ScalarField]s over arbitrary fields and [VectorSpace]s.
 *
 * A **directional derivative** measures how a scalar field `f : V → F`
 * changes along a direction vector `v ∈ V` at a point `p ∈ V`:
 * ```
 * D_v f(p) = lim (h → 0) (f(p + h·v) − f(p − h·v)) / (2h)
 * ```
 * The code that approximates this using a small, finite step `h`:
 * 1. Move forward: `pForward = p + h·v`
 * 2. Move backward: `pBackward = p − h·v`
 * 3. Take the difference between the two: `fForward - fBackward`
 * 4. Divide by `2h`
 *
 * In Kosmos, this derivative is approximated numerically (via central difference)
 * but is expressed generically in terms of the algebraic operations defined
 * by field and [VectorSpace].  It can therefore be applied to both numeric
 * and abstract field types.
 *
 * The resulting structure `df` is a [CovectorField] — a smooth mapping
 * that associates to each point `p` a [Covector] acting on tangent vectors
 * (directions) to yield the directional derivative.
 *
 * In simple terms: wiggle the point in direction `v`, measure how the scalar changes, and normalize by
 * the distance of the wiggle.
 */
object Derivative {

    /**
     * Generic central-difference directional derivative:
     * ```
     * D_v f(p) ≈ (f(p + h·v) - f(p - h·v)) / (2h)
     * ```
     * This version is fully generic and depends only on the field’s additive
     * and multiplicative AbelianGroup structure.
     *
     * @param space     the [VectorSpace] structure defining addition and scalar action
     * @param f         the scalar-valued function to differentiate
     * @param point     the evaluation point (`p`)
     * @param direction the direction vector along which to differentiate (`v`)
     * @param step      the infinitesimal step as a field element (`h`)
     * @return the approximate directional derivative Dᵥf(p)
     */
    fun <F : Any, V : Any> derivativeAt(
        space: VectorSpace<F, V>,
        f: (V) -> F,
        point: V,
        direction: V,
        step: F
    ): F {
        val field = space.field

        // f(p + h·v)
        val forwardPoint = space.add(point, space.leftAction(step, direction))
        val forwardValue = f(forwardPoint)

        // f(p - h·v)
        val backwardPoint = space.add(point, space.leftAction(field.add.inverse(step), direction))
        val backwardValue = f(backwardPoint)

        // f(p + h·v) - f(p - h·v)
        val numerator = field.add(forwardValue, field.add.inverse(backwardValue))

        // 2h
        val two = field.fromBigInt(BigInteger.TWO)
        val denominator = field.mul(two, step)

        // D_v f(p)
        return field.mul(numerator, field.reciprocal(denominator))
    }

    /**
     * Differential of a scalar field (a covector field):
     * ```
     * (df)_p(v) = D_v f(p)
     * ```
     */
    fun <F : Any, V : Any> ScalarField<F, V>.d(
        step: F
    ): CovectorField<F, V> =
        CovectorField.of(space) { point ->
            Covector.of(space) { direction ->
                derivativeAt(space, this@d::invoke, point, direction, step)
            }
        }

    /**
     * Convenience for [Real] scalar fields.
     */
    fun <V : Any> ScalarField<Real, V>.dReal(
        step: Real = 1e-6
    ): CovectorField<Real, V> = d(step)
}
