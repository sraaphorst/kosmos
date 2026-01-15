package org.vorpal.kosmos.core.linear.instances

import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.algebra.structures.FiniteVectorSpace
import org.vorpal.kosmos.core.ops.LeftAction
import org.vorpal.kosmos.core.linear.values.Vec2
import org.vorpal.kosmos.core.linear.values.Vec3
import org.vorpal.kosmos.core.linear.values.Vec4
import org.vorpal.kosmos.core.linear.values.Vec1

object FixedVectorSpaces {
    fun <F : Any> vec1(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec1<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = FixedTupleGroups.dim1Group(field.add),
        dimension = 1,
        leftAction = LeftAction { s, v -> Vec1(field.mul(s, v.x)) }
    )

    fun <F : Any> vec2(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec2<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = FixedTupleGroups.dim2Group(field.add),
        dimension = 2,
        leftAction = LeftAction { s, v -> Vec2(
            field.mul(s, v.x),
            field.mul(s, v.y)) }
    )

    fun <F : Any> vec3(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec3<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = FixedTupleGroups.dim3Group(field.add),
        dimension = 3,
        leftAction = LeftAction { s, v -> Vec3(
            field.mul(s, v.x),
            field.mul(s, v.y),
            field.mul(s, v.z)
        )}
    )

    fun <F : Any> vec4(
        field: Field<F>,
    ): FiniteVectorSpace<F, Vec4<F>> = FiniteVectorSpace.of(
        scalars = field,
        add = FixedTupleGroups.dim4Group(field.add),
        dimension = 4,
        leftAction = LeftAction { s, v -> Vec4(
            field.mul(s, v.x),
            field.mul(s, v.y),
            field.mul(s, v.z),
            field.mul(s, v.w)
        )}
    )
}
