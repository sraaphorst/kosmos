package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * In this representation, a [VectorField] is a mapping from a [VectorSpace] to itself.
 * 
 * Mathematically: `f : V → V`, where [V] is a vector space over the field `𝔽`.
 *
 * More accurately, this concept can be more general, i.e., defined on a manifold `M` and is a
 * section of the tangent bundle `TM` where, at each point `p` of the manifold, the vector field
 * assigns a vector in the tangent space `T_p(M)`.
 *
 * When the manifold happens to be a vector space, all tangent spaces are canonically isomorphic
 * to the vector space itself, and the original statement is correct.
 *
 * TODO: VectorFields<V,F> form a Lie algebra under the Lie bracket.
 */
interface VectorField<F : Any, V : Any> {
    val space: VectorSpace<F, V>
    val field: Field<F>
        get() = space.field
    operator fun invoke(point: V): V

    companion object {
        fun <F : Any, V : Any> of(
            space: VectorSpace<F, V>,
            f: (V) -> V
        ): VectorField<F, V> = object : VectorField<F, V> {
            override val space = space
            override fun invoke(point: V) = f(point)
        }

        fun <F : Any, V : Any> constant(space: VectorSpace<F, V>, value: V): VectorField<F, V> =
            of(space) { value }

        fun <F : Any, V : Any> zero(space: VectorSpace<F, V>): VectorField<F, V> =
            constant(space, space.add.identity)
    }
}

private fun <F : Any, V : Any> requireSameSpace(
    actual: VectorSpace<F, V>,
    expected: VectorSpace<F, V>,
) {
    require(actual === expected) { "Fields must be over the same VectorSpace instance." }
}

/**
 * Vector addition of two [VectorField]s.
 * Since a [VectorSpace] is an abelian group over a [Field], we can use the group's
 * operator to combine the vectors.
 */
operator fun <F : Any, V : Any> VectorField<F, V>.plus(other: VectorField<F, V>): VectorField<F, V> {
    requireSameSpace(this.space, other.space)
    return VectorField.of(space) { p -> space.add(this(p), other(p)) }
}

/**
 * Create a new [VectorField] by scaling the values of this vector space through a
 * scalar of the [Field] via the action on the [VectorSpace].
 */
operator fun <F : Any, V : Any> VectorField<F, V>.times(scalar: F): VectorField<F, V> =
    VectorField.of(space) { p -> space.leftAction(scalar, this(p)) }

/**
 * Unary negation (additive inverse of multiplicative identity): -f(p),
 * i.e. multiply vector assignments of VectorField by -1_F.
 */
operator fun <F : Any, V : Any> VectorField<F, V>.unaryMinus(): VectorField<F, V> =
    VectorField.of(space) { p -> space.add.inverse(this(p)) }

/**
 * Scaling a [VectorField] by an element of the base [Field].
 */
operator fun <F : Any, V : Any> F.times(vf: VectorField<F, V>): VectorField<F, V>  =
    VectorField.of(vf.space) { p -> vf.space.leftAction(this@times, vf(p)) }

/**
 * Pointwise multiplication of a [ScalarField] and a [VectorField]:
 * (f * X)(p) = f(p) ⋅ X(p).
 */
operator fun <F : Any, V : Any> ScalarField<F, V>.times(vf: VectorField<F, V>): VectorField<F, V> {
    requireSameSpace(space, vf.space)
    return VectorField.of(vf.space) { p -> vf.space.leftAction(this@times(p), vf(p)) }
}

/**
 * [VectorField] composition: (f andThen g)(v) = g(f(v)).
 */
infix fun <F : Any, V : Any> VectorField<F, V>.andThen(other: VectorField<F, V>): VectorField<F, V> {
    requireSameSpace(this.space, other.space)
    return VectorField.of(other.space) { p -> other(this(p)) }
}

infix fun <F : Any, V : Any> VectorField<F, V>.compose(other: VectorField<F, V>): VectorField<F, V> =
    other andThen this

