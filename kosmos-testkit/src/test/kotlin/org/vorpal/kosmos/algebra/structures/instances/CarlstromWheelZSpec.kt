package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.core.rational.ArbWheelZ
import org.vorpal.kosmos.laws.algebra.CarlstromWheelLaws

class CarlstromWheelZSpec : StringSpec({
    "CarlstromWheelZ satisfies CarlstromWheelLaws" {
        val wheel = WheelAlgebras.CarlstromWheelZ
        val arb = ArbWheelZ

        val laws = CarlstromWheelLaws(wheel, arb)
        laws.fullTest().throwIfFailed()
    }
})
