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
interface VectorField<F, V> {
    val space: VectorSpace<F, V>
    val field: Field<F> get() = space.field
    operator fun invoke(point: V): V
}

/**
 * Vector addition of two [VectorField]s.
 * Since a [VectorSpace] is an abelian group over a [Field], we can use the group's
 * operator to combine the vectors.
 */
operator fun <F, V> VectorField<F, V>.plus(other: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(space) { p -> space.group(this(p), other(p)) }

/**
 * Create a new [VectorField] by scaling the values of this vector space through a
 * scalar of the [Field] via the action on the [VectorSpace].
 */
operator fun <F, V> VectorField<F, V>.times(scalar: F): VectorField<F, V> =
    VectorFields.of(space) { p -> space.action(scalar, this(p)) }

/**
 * Unary negation (additive inverse of multiplicative identity): -f(p),
 * i.e. multiply vector assignments of VectorField by -1_F.
 */
operator fun <F, V> VectorField<F, V>.unaryMinus(): VectorField<F, V> =
    this * field.add.inverse(field.mul.identity)

/**
 * Apply a transformation φ: V -> V.
 */
fun <F, V> VectorField<F, V>.map(f: (V) -> V): VectorField<F, V> =
    VectorFields.of(space) { p -> f(this(p)) }

/**
 * Basic implementation of a vector field.
 */
abstract class AbstractVectorField<F, V>(
    override val space: VectorSpace<F, V>,
    private val f: (V) -> V
) : VectorField<F, V> {
    override fun invoke(point: V): V = f(point)
}

/**
 * Scaling a [VectorField] by an element of the base [Field].
 */
operator fun <F, V> F.times(vf: VectorField<F, V>): VectorField<F, V>  =
    VectorFields.of(vf.space) { point -> vf.space.action(this@times, vf(point)) }

/**
 * Pointwise multiplication of a [ScalarField] and a [VectorField]:
 * (f * X)(p) = f(p) ⋅ X(p).
 */
operator fun <F, V> ScalarField<F, V>.times(vf: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(vf.space) { point -> vf.space.action(this@times(point), vf(point)) }

/**
 * [VectorField] composition: (f then g)(v) = g(f(v)).
 */
infix fun <F, V> VectorField<F, V>.then(other: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(other.space) { p -> other(this@then(p)) }

/**
 * Functional composition: (φ ∘ f)(p) = φ(f(p)).
 */
infix fun <F, V> ((V) -> V).compose(vf: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(vf.space) { p -> this(vf(p)) }

/**
 * [VectorField] composition: (f compose g)(v) = f(g(v)).
 */
infix fun <F, V> VectorField<F, V>.compose(other: VectorField<F, V>): VectorField<F, V> =
    VectorFields.of(space) { p -> this(other(p)) }

private interface VectorFieldCompanion {
    /**
     * Simple way to create a [VectorField] from a:
     * - [VectorSpace]
     * - Function from the [VectorSpace] to itself.
     */
    fun <F, V> of(
        space: VectorSpace<F, V>,
        f: (V) -> V
    ): VectorField<F, V> =
        VectorFields.of(space) { p -> f(p) }

    /**
     * Create a [VectorField] that maps every vector of a [VectorSpace] to
     * a constant vector.
     */
    fun <F, V> constant(
        space: VectorSpace<F, V>,
        value: V
    ): VectorField<F, V> =
        VectorFields.of(space) { value }

    /**
     * Create a [VectorField] of a [VectorSpace] that maps every point to
     * the zero vector.
     */
    fun <F, V> zero(space: VectorSpace<F, V>): VectorField<F, V> =
        constant(space, space.group.identity)
}

// Global companion for easy static creation
object VectorFields : VectorFieldCompanion
