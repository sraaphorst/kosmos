// org.vorpal.kosmos.std.modules.Int2ZModule.kt
package org.vorpal.kosmos.std.modules

import org.vorpal.kosmos.algebra.ops.Action
import org.vorpal.kosmos.algebra.ops.Add
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.std.Int2

object Int2Add : AbelianGroup<Int2, Add> {
    override val identity = Int2(0, 0)
    override fun combine(a: Int2, b: Int2) = Int2(a.x + b.x, a.y + b.y)
    override fun inverse(a: Int2) = Int2(-a.x, -a.y)
}

object ZModule_Int2 : Module<Int, Int2, Monoid<Int, org.vorpal.kosmos.algebra.ops.Mul>> {
    override val R = Rings.IntRing
    override val add = Int2Add
    override val smul = Action<Int, Int2> { s, v -> Int2(s * v.x, s * v.y) }
}