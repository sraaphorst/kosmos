package org.vorpal.kosmos.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.linear.values.Vec1
import org.vorpal.kosmos.linear.values.Vec2
import org.vorpal.kosmos.linear.values.Vec3
import org.vorpal.kosmos.linear.values.Vec4
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.linear.values.Vec0

/**
 * This file comprises algebraic structures over the vectors [Vec0], [Vec1], [Vec2], [Vec3], and [Vec4].
 * - Given an [AbelianGroup] over `A`, we can extend this to an [AbelianGroup] `A^n`.
 * - Given an [AbelianGroup] over `A`, we can use this to construct a [FiniteVectorSpace] over the [AbelianGroup]
 *   `A^n`.
 */
object FixedTupleAlgebras {
    fun <A : Any> dim0Group(): AbelianGroup<Vec0<A>> = AbelianGroup.of(
        identity = Vec0(),
        op = BinOp(Symbols.PLUS) { _, _ -> Vec0() },
        inverse = Endo(Symbols.MINUS) { _ -> Vec0() }
    )

    fun <A : Any> dim1Group(
        group: AbelianGroup<A>
    ): AbelianGroup<Vec1<A>> = AbelianGroup.of(
        identity = Vec1(group.identity),
        op = BinOp(Symbols.PLUS) { u, v -> Vec1(group(u.x, v.x)) },
        inverse = Endo(Symbols.MINUS) { v -> Vec1(group.inverse(v.x)) }
    )

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

    fun <F : Any> vec0Space(
        field: Field<F>
    ): FiniteVectorSpace<F, Vec0<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim0Group(),
        dimension = 0,
        leftAction = LeftAction { _, v -> v }
    )

    fun <F : Any> vec1Space(
        field: Field<F>
    ): FiniteVectorSpace<F, Vec1<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim1Group(field.add),
        dimension = 1,
        leftAction = LeftAction { s, v -> Vec1(field.mul(s, v.x)) }
    )

    fun <F : Any> vec2Space(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec2<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim2Group(field.add),
        dimension = 2,
        leftAction = LeftAction { s, v ->
            Vec2(
                field.mul(s, v.x),
                field.mul(s, v.y)
            )
        }
    )

    fun <F : Any> vec3Space(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec3<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim3Group(field.add),
        dimension = 3,
        leftAction = LeftAction { s, v ->
            Vec3(
                field.mul(s, v.x),
                field.mul(s, v.y),
                field.mul(s, v.z)
            )
        }
    )

    fun <F : Any> vec4Space(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec4<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = dim4Group(field.add),
        dimension = 4,
        leftAction = LeftAction { s, v ->
            Vec4(
                field.mul(s, v.x),
                field.mul(s, v.y),
                field.mul(s, v.z),
                field.mul(s, v.w)
            )
        }
    )
}
