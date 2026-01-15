package org.vorpal.kosmos.core.linear.instances

import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.core.Symbols
import org.vorpal.kosmos.core.linear.values.Vec1
import org.vorpal.kosmos.core.linear.values.Vec2
import org.vorpal.kosmos.core.linear.values.Vec3
import org.vorpal.kosmos.core.linear.values.Vec4
import org.vorpal.kosmos.core.ops.BinOp
import org.vorpal.kosmos.core.ops.Endo

/**
 * Functions to take an AbelianGroup A and make it into a finite dimensional AbelianGroup A^n
 * for n in {1, 2, 3, 4}.
 */
object FixedTupleGroups {
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
        op = BinOp(Symbols.PLUS) { u, v -> Vec2(
            group(u.x, v.x),
            group(u.y, v.y)
        )},
        inverse = Endo(Symbols.MINUS) { v -> Vec2(
            group.inverse(v.x),
            group.inverse(v.y)
        )}
    )

    fun <A : Any> dim3Group(
        group: AbelianGroup<A>
    ): AbelianGroup<Vec3<A>> = AbelianGroup.of(
        identity = Vec3(group.identity, group.identity, group.identity),
        op = BinOp(Symbols.PLUS) { u, v -> Vec3(
            group(u.x, v.x),
            group(u.y, v.y),
            group(u.z, v.z)
        )},
        inverse = Endo(Symbols.MINUS) { v -> Vec3(
            group.inverse(v.x),
            group.inverse(v.y),
            group.inverse(v.z)
        )}
    )

    fun <A : Any> dim4Group(
        group: AbelianGroup<A>
    ): AbelianGroup<Vec4<A>> = AbelianGroup.of(
        identity = Vec4(group.identity, group.identity, group.identity, group.identity),
        op = BinOp(Symbols.PLUS) { u, v -> Vec4(
            group(u.x, v.x),
            group(u.y, v.y),
            group(u.z, v.z),
            group(u.w, v.w)
        )},
        inverse = Endo(Symbols.MINUS) { v -> Vec4(
            group.inverse(v.x),
            group.inverse(v.y),
            group.inverse(v.z),
            group.inverse(v.w)
        )}
    )
}
