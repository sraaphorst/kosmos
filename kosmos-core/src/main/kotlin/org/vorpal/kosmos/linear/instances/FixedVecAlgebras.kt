package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.CommutativeRing
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.algebra.structures.RModule
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.linear.values.Vec0
import org.vorpal.kosmos.linear.values.Vec1
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.Vec3
import org.vorpal.kosmos.linear.values.Vec4

/**
 * Algebraic structures on the fixed-size vectors [Vec0], [Vec1], [Vec2], [Vec3] and [Vec4].
 * - Given an [AbelianGroup] on `A`, `A^n` is an [AbelianGroup] (`dimNGroup`).
 * - Given a [CommutativeRing] `R`, `R^n` is a free [RModule] of rank `n` (`vecNModule`).
 * - Given a [Field] `F`, `F^n` is a [FiniteVectorSpace] of dimension `n` (`vecNSpace`).
 *
 * In each case the operations act coordinatewise. [Vec2] as a module over the 2 × 2 matrices is in
 * [FixedMatAlgebras].
 */
object FixedVecAlgebras {
    /**
     * `A^0`, the trivial group with the single element [Vec0].
     */
    fun <A : Any> dim0Group(): AbelianGroup<Vec0<A>> = AbelianGroup.of(
        identity = Vec0(),
        op = BinOp(Symbols.PLUS) { _, _ -> Vec0() },
        inverse = Endo(Symbols.MINUS) { _ -> Vec0() }
    )

    /**
     * `A^1`, the coordinatewise group built from [group].
     */
    fun <A : Any> dim1Group(
        group: AbelianGroup<A>
    ): AbelianGroup<Vec1<A>> = AbelianGroup.of(
        identity = Vec1(group.identity),
        op = BinOp(Symbols.PLUS) { u, v -> Vec1(group(u.x, v.x)) },
        inverse = Endo(Symbols.MINUS) { v -> Vec1(group.inverse(v.x)) }
    )

    /**
     * `A^2`, the coordinatewise group built from [group].
     */
    fun <A : Any> dim2Group(
        group: AbelianGroup<A>
    ): AbelianGroup<Vec2<A>> = AbelianGroup.of(
        identity = Vec2(group.identity, group.identity),
        op = BinOp(Symbols.PLUS) { u, v ->
            Vec2(
                group(u.x, v.x),
                group(u.y, v.y)
            )
        },
        inverse = Endo(Symbols.MINUS) { v ->
            Vec2(
                group.inverse(v.x),
                group.inverse(v.y)
            )
        }
    )

    /**
     * `A^3`, the coordinatewise group built from [group].
     */
    fun <A : Any> dim3Group(
        group: AbelianGroup<A>
    ): AbelianGroup<Vec3<A>> = AbelianGroup.of(
        identity = Vec3(
            group.identity,
            group.identity,
            group.identity
        ),
        op = BinOp(Symbols.PLUS) { u, v ->
            Vec3(
                group(u.x, v.x),
                group(u.y, v.y),
                group(u.z, v.z)
            )
        },
        inverse = Endo(Symbols.MINUS) { v ->
            Vec3(
                group.inverse(v.x),
                group.inverse(v.y),
                group.inverse(v.z)
            )
        }
    )

    /**
     * `A^4`, the coordinatewise group built from [group].
     */
    fun <A : Any> dim4Group(
        group: AbelianGroup<A>
    ): AbelianGroup<Vec4<A>> = AbelianGroup.of(
        identity = Vec4(
            group.identity,
            group.identity,
            group.identity,
            group.identity
        ),
        op = BinOp(Symbols.PLUS) { u, v ->
            Vec4(
                group(u.x, v.x),
                group(u.y, v.y),
                group(u.z, v.z),
                group(u.w, v.w)
            )
        },
        inverse = Endo(Symbols.MINUS) { v ->
            Vec4(
                group.inverse(v.x),
                group.inverse(v.y),
                group.inverse(v.z),
                group.inverse(v.w)
            )
        }
    )

    /**
     * `R^0`, the zero module over [ring].
     */
    fun <R : Any> vec0Module(
        ring: CommutativeRing<R>,
    ): RModule<R, Vec0<R>> = RModule.of(
        ring,
        add = dim0Group(),
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { _, v -> v }
    )

    /**
     * `F^0`, the zero vector space over [field].
     */
    fun <F : Any> vec0Space(
        field: Field<F>
    ): FiniteVectorSpace<F, Vec0<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim0Group(),
        dimension = 0,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { _, v -> v }
    )

    /**
     * `R^1` as the free module of rank 1 over [ring], with the coordinatewise scalar action.
     */
    fun <R : Any> vec1Module(
        ring: CommutativeRing<R>,
    ): RModule<R, Vec1<R>> = RModule.of(
        ring,
        add = dim1Group(ring.add),
        leftAction = LeftAction<R, Vec1<R>>(Symbols.TRIANGLE_RIGHT) { s, v ->
            ring.run { Vec1(mul(s, v[0])) }
        }
    )

    /**
     * `F^1` as a one-dimensional vector space over [field], with the coordinatewise scalar action.
     */
    fun <F : Any> vec1Space(
        field: Field<F>
    ): FiniteVectorSpace<F, Vec1<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim1Group(field.add),
        dimension = 1,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { s, v -> Vec1(field.mul(s, v.x)) }
    )

    /**
     * `R^2` as the free module of rank 2 over [ring], with the coordinatewise scalar action.
     */
    fun <R : Any> vec2Module(
        ring: CommutativeRing<R>,
    ): RModule<R, Vec2<R>> = RModule.of(
        ring,
        add = dim2Group(ring.add),
        leftAction = LeftAction<R, Vec2<R>>(Symbols.TRIANGLE_RIGHT) { s, v ->
            ring.run { Vec2(mul(s, v[0]), mul(s, v[1])) }
        }
    )

    /**
     * `F^2` as a two-dimensional vector space over [field], with the coordinatewise scalar action.
     */
    fun <F : Any> vec2Space(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec2<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim2Group(field.add),
        dimension = 2,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { s, v ->
            Vec2(
                field.mul(s, v.x),
                field.mul(s, v.y)
            )
        }
    )

    /**
     * `R^3` as the free module of rank 3 over [ring], with the coordinatewise scalar action.
     */
    fun <R : Any> vec3Module(
        ring: CommutativeRing<R>,
    ): RModule<R, Vec3<R>> = RModule.of(
        ring,
        add = dim3Group(ring.add),
        leftAction = LeftAction<R, Vec3<R>>(Symbols.TRIANGLE_RIGHT) { s, v ->
            ring.run {
                Vec3(
                    mul(s, v[0]),
                    mul(s, v[1]),
                    mul(s, v[2])
                )
            }
        }
    )

    /**
     * `F^3` as a three-dimensional vector space over [field], with the coordinatewise scalar action.
     */
    fun <F : Any> vec3Space(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec3<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim3Group(field.add),
        dimension = 3,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { s, v ->
            Vec3(
                field.mul(s, v.x),
                field.mul(s, v.y),
                field.mul(s, v.z)
            )
        }
    )

    /**
     * `R^4` as the free module of rank 4 over [ring], with the coordinatewise scalar action.
     */
    fun <R : Any> vec4Module(
        ring: CommutativeRing<R>,
    ): RModule<R, Vec4<R>> = RModule.of(
        ring,
        add = dim4Group(ring.add),
        leftAction = LeftAction<R, Vec4<R>>(Symbols.TRIANGLE_RIGHT) { s, v ->
            ring.run {
                Vec4(
                    mul(s, v[0]),
                    mul(s, v[1]),
                    mul(s, v[2]),
                    mul(s, v[3])
                )
            }
        }
    )

    /**
     * `F^4` as a four-dimensional vector space over [field], with the coordinatewise scalar action.
     */
    fun <F : Any> vec4Space(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec4<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim4Group(field.add),
        dimension = 4,
        leftAction = LeftAction(Symbols.TRIANGLE_RIGHT) { s, v ->
            Vec4(
                field.mul(s, v.x),
                field.mul(s, v.y),
                field.mul(s, v.z),
                field.mul(s, v.w)
            )
        }
    )

    /**
     * Eqs
     */

    /** Componentwise equality of vectors from an equality [eqA] on the entry. */
    fun <A : Any> eqVec1(eqA: Eq<A>): Eq<Vec1<A>> =
        Eq { u, v -> eqA(u.x, v.x) }

    /** Componentwise equality of vectors from an equality [eqA] on the entries. */
    fun <A : Any> eqVec2(eqA: Eq<A>): Eq<Vec2<A>> =
        Eq { u, v -> eqA(u.x, v.x) && eqA(u.y, v.y) }

    /** Componentwise equality of vectors from an equality [eqA] on the entries. */
    fun <A : Any> eqVec3(eqA: Eq<A>): Eq<Vec3<A>> =
        Eq { u, v -> eqA(u.x, v.x) && eqA(u.y, v.y) && eqA(u.z, v.z) }

    /** Componentwise equality of vectors from an equality [eqA] on the entries. */
    fun <A : Any> eqVec4(eqA: Eq<A>): Eq<Vec4<A>> =
        Eq { u, v -> eqA(u.x, v.x) && eqA(u.y, v.y) && eqA(u.z, v.z) && eqA(u.w, v.w) }
}
