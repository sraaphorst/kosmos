// src/test/kotlin/.../FieldLaws.kt
package org.vorpal.kosmos.algebra.laws

import io.kotest.property.Arb
import org.vorpal.kosmos.algebra.ops.Mul
import org.vorpal.kosmos.algebra.structures.AbelianGroup
import org.vorpal.kosmos.algebra.structures.Field
import org.vorpal.kosmos.core.Eq
import org.vorpal.kosmos.testing.excluding

class FieldLaws<A>(
    F: Field<A>,       // has add: AbelianGroup<A, Add>, mul: AbelianGroup<A, Mul>
    arb: Arb<A>,    // may include 0
    EQ: Eq<A>
) {
    private val ringLaws = RingLaws(F, arb, EQ)

    // Elements for the multiplicative group: F \ {0}
    private val nonzero: Arb<A> = arb.excluding(EQ, F.add.identity)

    /** Multiplicative abelian group on F \ {0}. */
    private val mulLaws = AbelianGroupLaws<A, AbelianGroup<A, Mul>>(F.mul, nonzero, EQ)

    suspend fun all() {
        ringLaws.all()
        mulLaws.all()
    }
}