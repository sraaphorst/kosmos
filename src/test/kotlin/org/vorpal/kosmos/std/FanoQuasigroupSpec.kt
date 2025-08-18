package org.vorpal.kosmos.std

import io.kotest.core.spec.style.StringSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import org.vorpal.kosmos.algebra.laws.CommutativeIdempotentQuasigroupLaws
import org.vorpal.kosmos.core.Eqs

class FanoQuasigroupSpec : StringSpec({
    val arbPts = Arb.int(0..6)

    "Fano quasigroup satisfies CIQ laws" {
        CommutativeIdempotentQuasigroupLaws(FanoQuasigroup, arbPts, Eqs.int).all()
    }
})