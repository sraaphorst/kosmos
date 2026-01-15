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
interface VectorField<F: Any, V: Any> {
    val space: VectorSpace<F, V>
    val field: Field<F> get() = space.field
    operator fun invoke(point: V): V
}

/**
 * Vector addition of two [VectorField]s.
 * Since a [VectorSpace] is an abelian group over a [Field], we can use the group's
 * operator to combine the vectors.
 */
operator fun <F: Any, V: Any> VectorField<F, V>.plus(other: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(space) { p -> space.add(this(p), other(p)) }

/**
 * Create a new [VectorField] by scaling the values of this vector space through a
 * scalar of the [Field] via the action on the [VectorSpace].
 */
operator fun <F: Any, V: Any> VectorField<F, V>.times(scalar: F): VectorField<F, V> =
    VectorFields.of(space) { p -> space.leftAction(scalar, this(p)) }

/**
 * Unary negation (additive inverse of multiplicative identity): -f(p),
 * i.e. multiply vector assignments of VectorField by -1_F.
 */
operator fun <F: Any, V: Any> VectorField<F, V>.unaryMinus(): VectorField<F, V> =
    VectorFields.of(space) { p -> space.add.inverse(this(p)) }

/**
 * Apply a transformation φ: V -> V.
 */
fun <F: Any, V: Any> VectorField<F, V>.map(f: (V) -> V): VectorField<F, V> =
    VectorFields.of(space) { p -> f(this(p)) }

/**
 * Scaling a [VectorField] by an element of the base [Field].
 */
operator fun <F: Any, V: Any> F.times(vf: VectorField<F, V>): VectorField<F, V>  =
    VectorFields.of(vf.space) { point -> vf.space.leftAction(this@times, vf(point)) }

/**
 * Pointwise multiplication of a [ScalarField] and a [VectorField]:
 * (f * X)(p) = f(p) ⋅ X(p).
 */
operator fun <F: Any, V: Any> ScalarField<F, V>.times(vf: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(vf.space) { point -> vf.space.leftAction(this@times(point), vf(point)) }

/**
 * [VectorField] composition: (f then g)(v) = g(f(v)).
 */
infix fun <F: Any, V: Any> VectorField<F, V>.then(other: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(other.space) { p -> other(this@then(p)) }

/**
 * Functional composition: (φ ∘ f)(p) = φ(f(p)).
 */
infix fun <F: Any, V: Any> ((V) -> V).compose(vf: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(vf.space) { p -> this(vf(p)) }

/**
 * [VectorField] composition: (f compose g)(v) = f(g(v)).
 */
infix fun <F: Any, V: Any> VectorField<F, V>.compose(other: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(space) { p -> this(other(p)) }

object VectorFields {
    /**
     * Simple way to create a [VectorField] from a:
     * - [VectorSpace]
     * - Function from the [VectorSpace] to itself.
     */
    fun <F: Any, V: Any> of(space: VectorSpace<F, V>, f: (V) -> V): VectorField<F, V> =
        object : VectorField<F, V> {
            override val space = space
            override fun invoke(point: V) = f(point)
        }

    /**
     * Create a [VectorField] that maps every vector of a [VectorSpace] to
     * a constant vector.
     */
    fun <F: Any, V: Any> constant(space: VectorSpace<F, V>, value: V): VectorField<F, V> =
        of(space) { value }

    /**
     * Create a [VectorField] of a [VectorSpace] that maps every point to
     * the zero vector.
     */
    fun <F: Any, V: Any> zero(space: VectorSpace<F, V>): VectorField<F, V> =
        constant(space, space.add.identity)
}

