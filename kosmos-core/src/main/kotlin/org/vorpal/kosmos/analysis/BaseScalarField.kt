package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * Abstract base class providing common operations on [ScalarField]s.
 *
 * This class represents smooth functions f : V → 𝔽 with algebraic
 * structure (addition, multiplication, scalar multiplication, etc.)
 * defined pointwise.
 */
abstract class BaseScalarField<F, V>(
    override val field: Field<F>,
    override val space: VectorSpace<F, V>
) : ScalarField<F, V> where F : Any, V : VectorSpace<F, V> {

    /**
     * Pointwise addition between two ScalarFields: (f + g)(p) = f(p) + g(p).
     */
    operator fun plus(other: ScalarField<F, V>): ScalarField<F, V> =
        ScalarFields.of(field, space) { p ->
            field.add.op.combine(this(p), other(p))
        }

    /**
     * Pointwise multiplication between two ScalarFiends: (fg)(p) = f(p) * g(p).
     */
    operator fun times(other: ScalarField<F, V>): ScalarField<F, V> =
        ScalarFields.of(field, space) { p ->
            field.mul.op.combine(this(p), other(p))
        }

    /**
     * Pointwise division: (f/g)(p) = f(p) / g(p), g ≠ 0.
     */
    operator fun div(other: ScalarField<F, V>): ScalarField<F, V> =
        ScalarFields.of(field, space) { p ->
            val inverse = field.mul.inverse(other(p))
            field.mul.op.combine(this(p), inverse)
        }

    /**
     * Scalar multiplication by element of the field: f(p)c = cf(p).
     */
    operator fun times(scalar: F): ScalarField<F, V> =
        ScalarFields.of(field, space) { p ->
            field.mul.op.combine(scalar, this(p))
        }

    /**
     * Unary negation (additive inverse): -f(p).
     */
    operator fun unaryMinus(): ScalarField<F, V> =
        ScalarFields.of(field, space) { p -> field.add.inverse(this(p)) }

    /**
     * Apply a transformation φ : 𝔽 → 𝔽 (e.g. sin(f), exp(f)).
     */
    fun map(f: (F) -> F): ScalarField<F, V> =
        ScalarFields.of(field, space) { p -> f(this(p)) }
}

/**
 * Scalar multiplication by element of the field: cf(p).
 */
operator fun <F, V> F.times(sf: ScalarField<F, V>): ScalarField<F, V>
        where F : Any, V : VectorSpace<F, V> =
    ScalarFields.of(sf.field, sf.space) { p ->
        sf.field.mul.op.combine(this, sf(p))
    }

/**
 * Functional composition: (φ ∘ f)(p) = φ(f(p)).
 */
infix fun <F, V> ((F) -> F).compose(
    sf: ScalarField<F, V>
): ScalarField<F, V> where F : Any, V : VectorSpace<F, V> =
    ScalarFields.of(sf.field, sf.space) { p -> this(sf(p)) }
