// org.vorpal.kosmos.std.modules.Int2ZModule.kt
package org.vorpal.kosmos.std.modules

import org.vorpal.kosmos.algebra.ops.Action
import org.vorpal.kosmos.algebra.ops.Add
import org.vorpal.kosmos.algebra.ops.Mul
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.std.Z2

object Z2Add : AbelianGroup<Z2, Add> {
    override val identity = Z2(0, 0)
    override fun combine(a: Z2, b: Z2) = Z2(a.x + b.x, a.y + b.y)
    override fun inverse(a: Z2) = Z2(-a.x, -a.y)
}

object Z_Module_Z2 : RModule<Int, Z2, Monoid<Int, Mul>> {
    override val R = Rings.IntRing
    override val add = Z2Add
    override val smul = Action<Int, Z2> { s, v -> Z2(s * v.x, s * v.y) }
}