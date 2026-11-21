package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.core.spec.style.FunSpec
import org.vorpal.kosmos.core.rational.ArbWheelZ
import org.vorpal.kosmos.laws.algebra.CarlstromWheelLaws
import org.vorpal.kosmos.testing.laws.lawTests

class CarlstromWheelZSpec : FunSpec({
    lawTests(CarlstromWheelLaws(WheelAlgebras.CarlstromWheelZ, ArbWheelZ))
})
