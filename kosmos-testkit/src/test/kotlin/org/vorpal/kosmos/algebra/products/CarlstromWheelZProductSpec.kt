package org.vorpal.kosmos.algebra.products

import io.kotest.core.spec.style.StringSpec
import org.vorpal.kosmos.algebra.structures.CarlstromWheel
import org.vorpal.kosmos.algebra.structures.instances.WheelAlgebras
import org.vorpal.kosmos.core.rational.ArbWheelZ
import org.vorpal.kosmos.core.rational.WheelZ
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.algebra.CarlstromWheelLaws

class CarlstromWheelZProductSpec : StringSpec({
    val doubleWheel: CarlstromWheel<Pair<WheelZ, WheelZ>> = CarlstromWheels.double(WheelAlgebras.CarlstromWheelZ)
    val doubleArb = TestingLaw.arbPair(ArbWheelZ)

    "A doubled CarlstromWheelZ satisfies CarlstromWheelLaws" {
        val laws = CarlstromWheelLaws(doubleWheel, doubleArb)
        laws.fullTest().throwIfFailed()
    }

    val quadWheel: CarlstromWheel<Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>> = CarlstromWheels.double(doubleWheel)
    val quadArb = TestingLaw.arbPair(doubleArb)
    "A quadrupled CarlstromWheelZ satisfies CarlstromWheelLaws" {
        val laws = CarlstromWheelLaws(quadWheel, quadArb)
        laws.fullTest().throwIfFailed()
    }

    val octWheel: CarlstromWheel<Pair<Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>, Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>>> = CarlstromWheels.double(quadWheel)
    val octArb = TestingLaw.arbPair(quadArb)
    "An octupled CarlstromWheelZ satisfies CarlstromWheelLaws" {
        val laws = CarlstromWheelLaws(octWheel, octArb)
        laws.fullTest().throwIfFailed()
    }

    val hexadecWheel: CarlstromWheel<Pair<Pair<Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>, Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>>, Pair<Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>, Pair<Pair<WheelZ, WheelZ>, Pair<WheelZ, WheelZ>>>>> = CarlstromWheels.double(octWheel)
    val hexadecArb = TestingLaw.arbPair(octArb)
    "A hexadecupled CarlstromWheelZ satisfies CarlstromWheelLaws" {
        val laws = CarlstromWheelLaws(hexadecWheel, hexadecArb)
        laws.fullTest().throwIfFailed()
    }
})
