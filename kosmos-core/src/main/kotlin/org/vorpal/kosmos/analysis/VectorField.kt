package org.vorpal.kosmos.analysis

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.VectorSpace

/**
 * In this representation, a [VectorField] is a mapping from a [VectorSpace] to itself.
 * Mathematically: f : V → V, where V is a vector space over the field 𝔽.
 *
 * More accurately, this concept can be more general, i.e. defined on a manifold M and is a
 * section of the tangent bundle TM where, at each point p of the manifold, the vector field
 * assigns a vector in the tangent space T_p(M).
 *
 * When the manifold happens to be a vector space, all tangent spaces are canonically isomorphic
 * to the vector space itself and the original statement is correct.
 *
 * TODO: VectorFields<V,F> form a module (and thus additive abelian group) over ScalarFields<V,F>
 * TODO: They also form a Lie algebra under the Lie bracket.
 */
interface VectorField<F, V> where F: Any, V: VectorSpace<F, V> {
    val space: VectorSpace<F, V>
    val field: Field<F>
        get() = space.field
    operator fun invoke(point: V): V
}

/**
 * Pointwise addition of [VectorField]s.
 */
operator fun <F, V> VectorField<F, V>.plus(
    other: VectorField<F, V>
): VectorField<F, V> where F: Any, V : VectorSpace<F, V> =
    object : VectorField<F, V> {
        override val space = this@plus.space
        override fun invoke(point: V): V =
            space.group.op.combine(this@plus(point), other(point))
    }

/**
 * Scaling a [VectorField] by an element of the base [Field].
 */
operator fun <F, V> F.times(
    vf: VectorField<F, V>
): VectorField<F, V> where F : Any, V : VectorSpace<F, V> =
    object : VectorField<F, V> {
        override val space = vf.space
        override fun invoke(point: V): V =
            vf.space.action.apply(this@times, vf(point))
    }

/**
 * Pointwise multiplication of a [ScalarField] and a [VectorField]:
 * (f * X)(p) = f(p) ⋅ X(p).
 */
operator fun <F, V> ScalarField<F, V>.times(
    vf: VectorField<F, V>
): VectorField<F, V> where F : Any, V : VectorSpace<F, V> =
    object : VectorField<F, V> {
        override val space = vf.space
        override fun invoke(point: V): V =
            vf.space.action.apply(this@times(point), vf(point))
    }

/**
 * [VectorField] composition: (f then g)(v) = g(f(v)).
 */
infix fun <F, V> VectorField<F, V>.then(
    other: VectorField<F, V>
): VectorField<F, V> where F: Any, V: VectorSpace<F, V> =
    object : VectorField<F, V> {
        override val space = other.space
        override fun invoke(point: V): V =
            other(this@then(point))
    }

/**
 * [VectorField] composition: (f compose g)(v) = f(g(v)).
 */
infix fun <F, V> VectorField<F, V>.compose(
    other: VectorField<F, V>
): VectorField<F, V> where F: Any, V: VectorSpace<F, V> =
    object : VectorField<F, V> {
        override val space = this@compose.space
        override fun invoke(point: V): V =
            this@compose(other(point))
    }

