package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * A [ScalarField] is a function that assigns a scalar value from the underlying [Field]
 * to each point (vector) of a [VectorSpace].
 *
 * Mathematically: f : V → 𝔽, where V is a vector space over 𝔽.
 *
 * TODO: ScalarField<V,F> = F^V form a commutative ring.
 */
interface ScalarField<F, V> where F: Any, V : VectorSpace<F, V> {
    val field: Field<F>
    val space: VectorSpace<F, V>
    operator fun invoke(point: V): F
}

/**
 * Pointwise addition of [ScalarField]s: (f + g)(p) = f(p) + g(p).
 */
operator fun <F, V> ScalarField<F, V>.plus(
    other: ScalarField<F, V>
): ScalarField<F, V> where F : Any, V: VectorSpace<F, V> =
    object : ScalarField<F, V> {
        override val field = this@plus.field
        override val space = this@plus.space
        override fun invoke(point: V): F =
            field.add.op.combine(this@plus(point), other(point))
    }

/**
 * Scalar [ScalarField] multiplication: for s in 𝔽, (sf)(p) = s * f(p).
 */
operator fun <F, V> F.times(
    sf: ScalarField<F, V>
): ScalarField<F, V> where F: Any, V: VectorSpace<F, V> =
    object : ScalarField<F, V> {
        override val field = sf.field
        override val space = sf.space
        override fun invoke(point: V): F =
            sf.field.mul.op.combine(this@times, sf(point))
    }

/**
 * Pointwise [ScalarField] multiplication: (fg)(p) = f(p) * g(p).
 */
operator fun <F, V> ScalarField<F, V>.times(
    other: ScalarField<F, V>
): ScalarField<F, V> where F : Any, V: VectorSpace<F, V> =
    object : ScalarField<F, V> {
        override val field = this@times.field
        override val space = this@times.space
        override fun invoke(point: V): F =
            field.mul.op.combine(this@times(point), other(point))
    }

/**
 * Pointwise [ScalarField] division: (f/g)(p) = f(p) / g(p), g ≠ 0.
 */
operator fun <F, V> ScalarField<F, V>.div(
    other: ScalarField<F, V>
): ScalarField<F, V> where F : Any, V: VectorSpace<F, V> =
    object : ScalarField<F, V> {
        override val field = this@div.field
        override val space = this@div.space
        override fun invoke(point: V): F {
            val inv = other.field.mul.inv(other(point))
            return field.mul.op.combine(this@div(point), inv)
        }
    }

/**
 * Composition of function with [ScalarField]: φ∘f where φ: 𝔽 → 𝔽 (e.g., sin(f), e^f).
 */
infix fun <F, V> ((F) -> F).compose(
    sf: ScalarField<F, V>
): ScalarField<F, V> where F : Any, V: VectorSpace<F, V> =
    object : ScalarField<F, V> {
        override val field = sf.field
        override val space = sf.space
        override fun invoke(point: V): F =
            this@compose(sf(point))
    }

/**
 * Map operation over [ScalarField].
 */
fun <F, V> ScalarField<F,V>.map(
    f: (F) -> F
): ScalarField<F,V> where F: Any, V: VectorSpace<F, V> =
    f.compose(this)