package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.laws.algebra.StarAlgebraLaws
import org.vorpal.kosmos.std.ArbRational

class RationalStarFieldSpec : StringSpec({
    "RationalStarField satisfies StarAlgebraLaws" {
        val laws = StarAlgebraLaws(
            algebra = RationalAlgebras.RationalStarField,
            scalarArb = ArbRational.small,
            algebraArb = ArbRational.small,
            eqR = RationalAlgebras.eqRational,
            eqA = RationalAlgebras.eqRational,
            prR = RationalAlgebras.printableRationalPretty,
            prA = RationalAlgebras.printableRationalPretty
        )
        laws.fullTest().throwIfFailed()
    }
})
