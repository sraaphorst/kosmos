package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * Provides generic finite-difference directional derivatives and differentials
 * for [ScalarField]s over arbitrary [Field]s and [VectorSpace]s.
 *
 * A **directional derivative** measures how a scalar field `f : V → F`
 * changes along a direction vector `v ∈ V` at a point `p ∈ V`:
 *
 * ```
 * D_v f(p) = limₕ→0 ( f(p + h·v) − f(p − h·v) ) / (2h)
 * ```
 *
 * In Kosmos, this derivative is approximated numerically (via central difference)
 * but is expressed generically in terms of the algebraic operations defined
 * by [Field] and [VectorSpace].  It can therefore be applied to both numeric
 * and abstract field types.
 *
 * The resulting structure `df` is a [CovectorField] — a smooth mapping
 * that associates to each point `p` a [Covector] acting on tangent vectors
 * (directions) to yield the directional derivative.
 */
object Derivative {

    /**
     * Generic central-difference directional derivative.
     *
     * This version is fully generic and depends only on the field’s additive
     * and multiplicative [org.vorpal.kosmos.algebra.structures.AbelianGroup]
     * structure.  It assumes that `two` and `h` are provided as field elements.
     *
     * @param space the [VectorSpace] structure defining addition and scalar action
     * @param f the scalar-valued function to differentiate
     * @param p the evaluation point
     * @param v the direction vector along which to differentiate
     * @param h the infinitesimal step as a field element
     * @param two the element representing 2 in the same field
     * @return the approximate directional derivative Dᵥf(p)
     */
    fun <F : Any, V : Any> derivativeAt(
        space: VectorSpace<F, V>,
        f: (V) -> F,
        p: V,
        v: V,
        h: F,
        two: F,
    ): F {
        val field = space.field
        val add = field.add
        val mul = field.mul

        val pForward = space.group.op(p, space.action(h, v))
        val pBackward = space.group.op(p, space.action(add.inverse(h), v))

        val fForward = f(pForward)
        val fBackward = f(pBackward)

        val numerator = add.op(fForward, add.inverse(fBackward))
        val denominator = mul.op(two, h)
        val denominatorInv = mul.inverse(denominator)

        return mul.op(numerator, denominatorInv)
    }

    /**
     * Specialized overload for real vector spaces (ℝⁿ).
     * Uses [Double] arithmetic and the standard central-difference formula.
     *
     * @param space the real [VectorSpace]
     * @param f the scalar-valued function to differentiate
     * @param p the evaluation point
     * @param v the direction vector along which to differentiate
     * @param h the finite step (default 1e-6)
     */
    fun <V : Any> derivativeAt(
        space: VectorSpace<Double, V>,
        f: (V) -> Double,
        p: V,
        v: V,
        h: Double = 1e-6
    ): Double {
        val pForward = space.group.op(p, space.action(h, v))
        val pBackward = space.group.op(p, space.action(-h, v))
        return (f(pForward) - f(pBackward)) / (2.0 * h)
    }

    /**
     * Extension producing the **differential** (covector field) of a scalar field
     * using a provided [derivativeAt] function.
     *
     * The resulting [CovectorField] maps each point `p` to a [Covector]
     * that evaluates the directional derivative in any direction `v`.
     *
     * @param derivativeAt the derivative operator to use
     * @param h the finite step (field element)
     * @param two the field element representing 2
     */
    fun <F : Any, V : Any> ScalarField<F, V>.d(
        derivativeAt: (VectorSpace<F, V>, (V) -> F, V, V, F, F) -> F,
        h: F,
        two: F
    ): CovectorField<F, V> =
        CovectorFields.of(space) { p ->
            Covectors.of(space) { v ->
                derivativeAt(space, this@d::invoke, p, v, h, two)
            }
        }

    /**
     * Convenience extension for real scalar fields.
     *
     * Computes the differential df using standard double arithmetic.
     *
     * @param h finite step size (default 1e-6)
     */
    fun <V : Any> ScalarField<Double, V>.dReal(
        h: Double = 1e-6
    ): CovectorField<Double, V> =
        CovectorFields.of(space) { p ->
            Covectors.of(space) { v ->
                derivativeAt(space, this@dReal::invoke, p, v, h)
            }
        }
}
