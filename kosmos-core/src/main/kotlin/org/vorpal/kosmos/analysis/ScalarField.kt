package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.VectorSpace
import org.vorpal.kosmos.core.ops.BinOp

/**
 * A [ScalarField] is a function that assigns a scalar value from the underlying [Field]
 * to each point (vector) of a [VectorSpace], i.e. a [org.vorpal.kosmos.categories.Morphism] in
 * the category of [VectorSpace]s.
 *
 * Mathematically: f : V → 𝔽, where V is a vector space over 𝔽.
 *
 * TODO: ScalarField<V,F> = F^V form a commutative ring equivalent to the smooth functions from V to F.
 */
interface ScalarField<F, V> where F: Any, V : VectorSpace<F, V> {
    val field: Field<F>
    val space: VectorSpace<F, V>
    operator fun invoke(point: V): F

    companion object {
        /**
         * Static method to create a [ScalarField] for a [VectorSpace] over a [Field].
         */
        fun <F, V> of(
            field: Field<F>,
            space: VectorSpace<F, V>,
            f: (V) -> F
        ): ScalarField<F, V> where F: Any, V: VectorSpace<F, V> =
            object : ScalarField<F, V> {
                override val field: Field<F> = field
                override val space: VectorSpace<F, V> = space
                override fun invoke(point: V): F = f(point)
            }

        /**
         * Form a constant [ScalarField] over a [VectorSpace] where the map is constant.
         * This is useful to define [ScalarField]s where every vector maps to either:
         * 1. The additive identity of the [Field]; or
         * 2. The multiplicative identity of the [Field].
         *
         * This will simplify making the set of [ScalarField]s over a given [VectorSpace] (later, manifold)
         * and [Field] (later, commutative ring) into a [CommutativeRing].
         */
        fun <F, V> constant(
            field: Field<F>,
            space: VectorSpace<F, V>,
            constant: F
        ): ScalarField<F, V> where F: Any, V: VectorSpace<F, V> =
            object : ScalarField<F, V> {
                override val field = field
                override val space = space
                override fun invoke(point: V): F = constant
            }

        /**
         * Convenience function that creates a [ScalarField] that is a constant map from the [VectorSpace] to
         * the additive identity of the [Field].
         */
        fun <F, V> zero(
            field: Field<F>,
            space: VectorSpace<F, V>
        ): ScalarField<F, V> where F: Any, V: VectorSpace<F, V> =
            constant(field, space, field.add.identity)

        /**
         * Convenience function that creates a [ScalarField] that is a constant map from the [VectorSpace] to
         * the multiplicative identity of the [Field].
         */
        fun <F, V> one(
            field: Field<F>,
            space: VectorSpace<F, V>
        ): ScalarField<F, V> where F: Any, V: VectorSpace<F, V> =
            constant(field, space, field.mul.identity)

        /**
         * Given a:
         * - [Field] F
         * - [VectorSpace] V over F
         * create the [CommutativeRing] of [ScalarField]s that map the elements of the vector space to scalars.
         */
        fun <F, V> commutativeRing(
            field: Field<F>,
            space: VectorSpace<F, V>
        ): CommutativeRing<ScalarField<F, V>> where F: Any, V: VectorSpace<F, V> {
            // The additive abelian group is defined over the ScalarField<F, V>s.
            val additiveGroup = object: AbelianGroup<ScalarField<F, V>> {
                // The identity ScalarField is simply the scalar field that associates
                // the field's zero element to every point.
                override val identity: ScalarField<F, V> =
                    zero(field, space)

                // Given a ScalarField<F, V>, its inverse is the ScalarField that results
                // from mapping the field's additive inv method over every point.
                override val inv: (ScalarField<F, V>) -> ScalarField<F, V> =
                    { sf -> sf.map(field.add.inv) }

                // Two sum of two ScalarField<F, V>s is simply the sum taken across each point.
                override val op: BinOp<ScalarField<F, V>> = BinOp(
                    combine = { sf1, sf2 -> sf1 + sf2 },
                    symbol = "+"
                )
            }

            val multiplicativeMonoid = object : Monoid<ScalarField<F, V>> {
                override val identity: ScalarField<F, V> =
                    one(field, space)
                override val op: BinOp<ScalarField<F, V>> = BinOp(
                    combine = { sf1, sf2 -> sf1 * sf2 },
                    symbol = "*"
                )
            }

            return object : CommutativeRing<ScalarField<F, V>> {
                override val add: AbelianGroup<ScalarField<F, V>> = additiveGroup
                override val mul: Monoid<ScalarField<F, V>> = multiplicativeMonoid
            }
        }

    }
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
