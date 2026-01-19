package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A ScalarField is a function assigning a scalar (in F) to each point of a vector space V.
 * Mathematically: f : V → F.
 */
interface ScalarField<F : Any, V : Any> {
    val space: VectorSpace<F, V>
    val field: Field<F>
        get() = space.field

    operator fun invoke(point: V): F

    companion object {
        fun <F : Any, V : Any> of(
            space: VectorSpace<F, V>,
            f: (V) -> F,
        ): ScalarField<F, V> = object : ScalarField<F, V> {
            override val space = space
            override fun invoke(point: V): F = f(point)
        }

        fun <F : Any, V : Any> constant(space: VectorSpace<F, V>, value: F, ): ScalarField<F, V> =
            of(space) { value }

        fun <F : Any, V : Any> zero(space: VectorSpace<F, V>): ScalarField<F, V> =
            constant(space, space.field.add.identity)

        fun <F : Any, V : Any> one(space: VectorSpace<F, V>): ScalarField<F, V> =
            constant(space, space.field.mul.identity)
    }
}

private fun <F : Any, V : Any> requireSameSpace(
    expected: ScalarField<F, V>,
    other: ScalarField<F, V>,
) {
    require(other.space === expected.space) { "ScalarFields must be over the same VectorSpace instance." }
}

/**
 * Pointwise addition of two ScalarFields: (f + g)(p) = f(p) + g(p).
 */
operator fun <F : Any, V : Any> ScalarField<F, V>.plus(other: ScalarField<F, V>): ScalarField<F, V> {
    requireSameSpace(this, other)
    return ScalarField.of(space) { p -> field.add(this(p), other(p)) }
}

/**
 * Pointwise multiplication between two ScalarFields: (fg)(p) = f(p) * g(p).
 */
operator fun <F : Any, V : Any> ScalarField<F, V>.times(other: ScalarField<F, V>): ScalarField<F, V> {
    requireSameSpace(this, other)
    return ScalarField.of(space) { p -> field.mul(this(p), other(p)) }
}

/**
 * Pointwise division: (f/g)(p) = f(p) / g(p), g ≠ 0.
 */
operator fun <F : Any, V : Any> ScalarField<F, V>.div(other: ScalarField<F, V>): ScalarField<F, V> {
    requireSameSpace(this, other)
    return ScalarField.of(space) { p ->
        val inverse = field.reciprocal(other(p))
        field.mul(this(p), inverse)
    }
}

/**
 * Scalar multiplication by element of the field: (cf)(p) = c·f(p).
 */
operator fun <F : Any, V : Any> ScalarField<F, V>.times(scalar: F): ScalarField<F, V> =
    ScalarField.of(space) { p -> field.mul(scalar, this(p)) }

/**
 * Unary negation (additive inverse): -f(p).
 */
operator fun <F : Any, V : Any> ScalarField<F, V>.unaryMinus(): ScalarField<F, V> =
    ScalarField.of(space) { p -> field.add.inverse(this(p)) }

/**
 * Scalar multiplication by element of the field: cf(p).
 */
operator fun <F : Any, V : Any> F.times(sf: ScalarField<F, V>): ScalarField<F, V> =
    ScalarField.of(sf.space) { p -> sf.field.mul(this, sf(p)) }

/**
 * Functional composition.
 */
infix fun <F : Any, V : Any> ScalarField<F, V>.andThen(phi: (F) -> F): ScalarField<F, V> =
    ScalarField.of(space) { p -> phi(this(p)) }

infix fun <F : Any, V : Any> ((F) -> F).compose(sf: ScalarField<F, V>): ScalarField<F, V> =
    sf andThen this
