// org.vorpal.kosmos.std.vectorspaces.Q2OverQ.kt
package org.vorpal.kosmos.std.vectorspaces

import org.vorpal.kosmos.algebra.ops.Action
import org.vorpal.kosmos.algebra.ops.Add
import org.vorpal.kosmos.algebra.structures.*
import org.vorpal.kosmos.std.Q2
import org.vorpal.kosmos.std.Rational

object Q2Add : AbelianGroup<Q2, Add> {
    override val identity = Q2(Rational.zero, Rational.zero)
    override fun combine(a: Q2, b: Q2) = Q2(a.x + b.x, a.y + b.y)
    override fun inverse(a: Q2) = Q2(-a.x, -a.y)
}

object Q2OverQ : VectorSpace<Rational, Q2> {
    override val R: Field<Rational> = Fields.RationalField
    override val add = Q2Add
    override val smul = Action<Rational, Q2> { s, v -> Q2(s * v.x, s * v.y) }
}