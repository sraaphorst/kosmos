package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import io.kotest.property.checkAll
import org.vorpal.kosmos.algebra.ops.Mul
import org.vorpal.kosmos.algebra.structures.Monoid
import org.vorpal.kosmos.algebra.structures.Ring
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.core.assertEquals

class RingLaws<A, M>(
    private val R: Ring<A, M>,
    private val arb: Arb<A>,
    private val EQ: Eq<A>
) where M : Monoid<A, Mul> {
    suspend fun additiveGroup() =
        AbelianGroupLaws(R.add, arb, EQ).all()

    suspend fun multiplicativeMonoid() =
        MonoidLaws(R.mul, arb, EQ).all()

    suspend fun distributivity() =
        checkAll(arb, arb, arb) { a, b, c ->
            val left  = R.mul.combine(a, R.add.combine(b, c))
            val right = R.add.combine(R.mul.combine(a, b), R.mul.combine(a, c))
            EQ.assertEquals(left, right)

            val left2  = R.mul.combine(R.add.combine(a, b), c)
            val right2 = R.add.combine(R.mul.combine(a, c), R.mul.combine(b, c))
            EQ.assertEquals(left2, right2)
        }

    suspend fun all() {
        additiveGroup()
        multiplicativeMonoid()
        distributivity()
    }
}