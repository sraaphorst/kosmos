package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A [ScalarField] is a function that assigns a scalar from the [Field]
 * to each point of a [VectorSpace].
 *
 * Mathematically: f : V → 𝔽, where V is a vector space over 𝔽.
 */
interface ScalarField<F: Any, V: Any> {
    val space: VectorSpace<F, V>
    val field get() = space.field
    operator fun invoke(point: V): F
}

/**
 * Pointwise addition of two ScalarFields: (f + g)(p) = f(p) + g(p).
 */
operator fun <F: Any, V: Any> ScalarField<F, V>.plus(other: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(space) { p -> field.add(this(p), other(p)) }

/**
 * Pointwise multiplication between two ScalarFiends: (fg)(p) = f(p) * g(p).
 */
operator fun <F: Any, V: Any> ScalarField<F, V>.times(other: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(space) { p -> field.mul(this(p), other(p)) }

/**
 * Pointwise division: (f/g)(p) = f(p) / g(p), g ≠ 0.
 */
operator fun <F: Any, V: Any> ScalarField<F, V>.div(other: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(space) { p ->
        val inverse = field.reciprocal(other(p))
        field.mul(this(p), inverse)
    }

/**
 * Scalar multiplication by element of the field: (cf)(p) = c·f(p).
 */
operator fun <F: Any, V: Any> ScalarField<F, V>.times(scalar: F): ScalarField<F, V> =
    ScalarFields.of(space) { p -> field.mul(scalar, this(p)) }

/**
 * Unary negation (additive inverse): -f(p).
 */
operator fun <F: Any, V: Any> ScalarField<F, V>.unaryMinus(): ScalarField<F, V> =
    ScalarFields.of(space) { p -> field.add.inverse(this(p)) }

/**
 * Apply a transformation φ : 𝔽 → 𝔽 (e.g. sin(f), exp(f)).
 */
fun <F: Any, V: Any> ScalarField<F, V>.map(f: (F) -> F): ScalarField<F, V> =
    ScalarFields.of(space) { p -> f(this(p)) }

fun <V : Any> ScalarField<Double, V>.dReal(
    h: Double = 1e-6
): CovectorField<Double, V> =
    CovectorFields.of(space) { p ->
        Covectors.of(space) { v ->
            derivativeAt(space, this@dReal::invoke, p, v, h)
        }
    }

fun <F : Any, V : Any> derivativeAt(
    space: VectorSpace<F, V>,
    f: (V) -> F,
    p: V,
    v: V,
    h: F,
    two: F
): F {
    val field = space.field
    val add = field.add
    val mul = field.mul

    val pForward = space.group(p, space.action(h, v))
    val pBackward = space.group(p, space.action(add.inverse(h), v))

    val fForward = f(pForward)
    val fBackward = f(pBackward)

    val numerator = add(fForward, add.inverse(fBackward))
    val denominator = mul(two, h)
    val denominatorInv = field.reciprocal(denominator)

    return mul.op(numerator, denominatorInv)
}

fun <V : Any> derivativeAt(
    space: VectorSpace<Double, V>,
    f: (V) -> Double,
    p: V,
    v: V,
    h: Double = 1e-6
): Double {
    val pForward = space.group(p, space.action(h, v))
    val pBackward = space.group(p, space.action(-h, v))
    return (f(pForward) - f(pBackward)) / (2.0 * h)
}

/**
 * Automatic Differentiation (future direction)
 *
 * Once you add dual numbers or autodiff support, you can compute this exactly.
 *
 * For dual numbers:
 * f(x + εv) = f(x) + ε \, D_v f(x)
 *
 * So the derivative is simply the dual part of the evaluation.
 * In code (conceptually):
 *
 * fun <F : DualNumber, V : Any> derivativeAt(
 *     f: ScalarField<F, V>,
 *     p: V,
 *     v: V
 * ): F = f(p + ε * v).dual
 *
 * Kosmos can later support this via a generic autodiff core — it would make everything exact and composable.
 */
fun <F : Any, V : Any> ScalarField<F, V>.d(
    derivativeAt: (VectorSpace<F, V>, (V) -> F, V, V) -> F
): CovectorField<F, V> =
    CovectorFields.of(space) { p ->
        Covectors.of(space) { v ->
            derivativeAt(space, this@d::invoke, p, v)
        }
    }

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
 * Scalar multiplication by element of the field: cf(p).
 */
operator fun <F: Any, V: Any> F.times(sf: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(sf.space) { p -> sf.field.mul(this, sf(p)) }

/**
 * Functional composition: (φ ∘ f)(p) = φ(f(p)).
 */
infix fun <F: Any, V: Any> ((F) -> F).compose(sf: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(sf.space) { p -> this(sf(p)) }

object ScalarFields {
    /**
     * Simple way to create a [ScalarField] from a:
     * - [VectorSpace] over a [Field]
     * - Function from the [VectorSpace] to the [Field].
     */
    fun <F: Any, V: Any> of(space: VectorSpace<F, V>, f: (V) -> F): ScalarField<F, V> =
        object : ScalarField<F, V> {
            override val space: VectorSpace<F, V> = space
            override fun invoke(point: V): F = f(point)
        }

    /**
     * Create a [ScalarField] of a [VectorSpace] over its [Field] that maps every point to a
     * constant value in the [Field].
     */
    fun <F: Any, V: Any> constant(space: VectorSpace<F, V>, value: F): ScalarField<F, V> =
        of(space) { value }

    /**
     * Create a [ScalarField] of a [VectorSpace] over its [Field] that maps every point to
     * the additive identity of the [Field].
     */
    fun <F: Any, V: Any> zero(space: VectorSpace<F, V>): ScalarField<F, V> =
        constant(space, space.field.add.identity)

    /**
     * Create a [ScalarField] of a [VectorSpace] over its [Field] that maps every point to
     * the multiplicative identity of the [Field].
     */
    fun <F: Any, V: Any> one(space: VectorSpace<F, V>): ScalarField<F, V> =
        constant(space, space.field.mul.identity)
}
