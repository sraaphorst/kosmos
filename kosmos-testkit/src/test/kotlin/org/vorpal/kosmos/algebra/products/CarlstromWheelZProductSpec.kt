package org.vorpal.kosmos.algebra.products

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.algebra.structures.instances.WheelAlgebras
import org.vorpal.kosmos.core.rational.ArbWheelZ
import org.vorpal.kosmos.laws.TestingLaw
import org.vorpal.kosmos.laws.algebra.CarlstromWheelLaws
import org.vorpal.kosmos.laws.lawGraph
import org.vorpal.kosmos.testing.laws.lawTests

/**
 * Doubling a Carlström wheel gives a Carlström wheel: WheelZ doubled 1, 2, 3 and 4 times.
 */
class CarlstromWheelZProductSpec : FunSpec({
    val doubleWheel = CarlstromWheels.double(WheelAlgebras.CarlstromWheelZ)
    val doubleArb = TestingLaw.arbPair(ArbWheelZ)
    val quadWheel = CarlstromWheels.double(doubleWheel)
    val quadArb = TestingLaw.arbPair(doubleArb)
    val octWheel = CarlstromWheels.double(quadWheel)
    val octArb = TestingLaw.arbPair(quadArb)
    val hexadecWheel = CarlstromWheels.double(octWheel)
    val hexadecArb = TestingLaw.arbPair(octArb)

    lawTests(lawGraph("CarlstromWheelZ products") {
        branch("WheelZ²") { suite(CarlstromWheelLaws(doubleWheel, doubleArb)) }
        branch("WheelZ⁴") { suite(CarlstromWheelLaws(quadWheel, quadArb)) }
        branch("WheelZ⁸") { suite(CarlstromWheelLaws(octWheel, octArb)) }
        branch("WheelZ¹⁶") { suite(CarlstromWheelLaws(hexadecWheel, hexadecArb)) }
    })
})
