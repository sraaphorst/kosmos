package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A [ScalarField] is a function that assigns a scalar from the [Field]
 * to each point of a [VectorSpace].
 *
 * Mathematically: f : V → 𝔽, where V is a vector space over 𝔽.
 */
interface ScalarField<F, V> {
    val space: VectorSpace<F, V>
    val field get() = space.field
    operator fun invoke(point: V): F
}

/**
 * Pointwise addition of two ScalarFields: (f + g)(p) = f(p) + g(p).
 */
operator fun <F, V> ScalarField<F, V>.plus(other: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(space) { p -> field.add(this(p), other(p)) }

/**
 * Pointwise multiplication between two ScalarFiends: (fg)(p) = f(p) * g(p).
 */
operator fun <F, V> ScalarField<F, V>.times(other: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(space) { p -> field.mul(this(p), other(p)) }

/**
 * Pointwise division: (f/g)(p) = f(p) / g(p), g ≠ 0.
 */
operator fun <F, V> ScalarField<F, V>.div(other: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(space) { p ->
        val inverse = field.mul.inverse(other(p))
        field.mul(this(p), inverse)
    }

/**
 * Scalar multiplication by element of the field: (cf)(p) = c·f(p).
 */
operator fun <F, V> ScalarField<F, V>.times(scalar: F): ScalarField<F, V> =
    ScalarFields.of(space) { p -> field.mul(scalar, this(p)) }

/**
 * Unary negation (additive inverse): -f(p).
 */
operator fun <F, V> ScalarField<F, V>.unaryMinus(): ScalarField<F, V> =
    ScalarFields.of(space) { p -> field.add.inverse(this(p)) }

/**
 * Apply a transformation φ : 𝔽 → 𝔽 (e.g. sin(f), exp(f)).
 */
fun <F, V> ScalarField<F, V>.map(f: (F) -> F): ScalarField<F, V> =
    ScalarFields.of(space) { p -> f(this(p)) }

/**
 * Scalar multiplication by element of the field: cf(p).
 */
operator fun <F, V> F.times(sf: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(sf.space) { p -> sf.field.mul(this, sf(p)) }

/**
 * Functional composition: (φ ∘ f)(p) = φ(f(p)).
 */
infix fun <F, V> ((F) -> F).compose(sf: ScalarField<F, V>): ScalarField<F, V> =
    ScalarFields.of(sf.space) { p -> this(sf(p)) }


/**
 * Basic implementation of a scalar field.
 */
abstract class AbstractScalarField<F, V>(
    override val space: VectorSpace<F, V>,
    private val f: (V) -> F
) : ScalarField<F, V> {
    override fun invoke(point: V): F = f(point)
}


sealed interface ScalarFieldCompanion {
    /**
     * Simple way to create a [ScalarField] from a:
     * - [VectorSpace] over a [Field]
     * - Function from the [VectorSpace] to the [Field].
     */
    fun <F, V> of(space: VectorSpace<F, V>, f: (V) -> F): ScalarField<F, V> =
        object : AbstractScalarField<F, V>(space, f) {}

    /**
     * Create a [ScalarField] of a [VectorSpace] over its [Field] that maps every point to a
     * constant value in the [Field].
     */
    fun <F, V> constant(space: VectorSpace<F, V>, value: F): ScalarField<F, V> =
        of(space) { value }

    /**
     * Create a [ScalarField] of a [VectorSpace] over its [Field] that maps every point to
     * the additive identity of the [Field].
     */
    fun <F, V> zero(space: VectorSpace<F, V>): ScalarField<F, V> =
        constant(space, space.field.add.identity)

    /**
     * Create a [ScalarField] of a [VectorSpace] over its [Field] that maps every point to
     * the multiplicative identity of the [Field].
     */
    fun <F, V> one(space: VectorSpace<F, V>): ScalarField<F, V> =
        constant(space, space.field.mul.identity)
}

// Global companion for easy static creation
object ScalarFields : ScalarFieldCompanion
