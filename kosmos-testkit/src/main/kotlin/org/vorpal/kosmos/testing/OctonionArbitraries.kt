package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.triple
import io.kotest.property.arbitrary.bind
import org.vorpal.kosmos.algebra.structures.instances.Octonion
import org.vorpal.kosmos.algebra.structures.instances.OctonionAlgebras
import org.vorpal.kosmos.algebra.structures.instances.octonion

object OctonionArbitraries {
    private val octonions = OctonionAlgebras.OctonionDivisionAlgebra

    val octonion: Arb<Octonion> =
        Arb.bind(
            RealArbitraries.smallReal,
            RealArbitraries.smallReal,
            RealArbitraries.smallReal,
            RealArbitraries.smallReal,
            RealArbitraries.smallReal,
            RealArbitraries.smallReal,
            RealArbitraries.smallReal,
            RealArbitraries.smallReal
        ) { w, x, y, z, u, v, s, t ->
            octonion(w, x, y, z, u, v, s, t)
        }

    val reciprocalSafeOctonion: Arb<Octonion> =
        octonion.filter { o ->
            val n2 = octonions.normSq(o)
            n2 in 0.01..10_000.0
        }

    val octonionTriple: Arb<Triple<Octonion, Octonion, Octonion>> =
        Arb.triple(octonion, octonion, octonion)

    val reciprocalTriple: Arb<Triple<Octonion, Octonion, Octonion>> =
        Arb.triple(reciprocalSafeOctonion, reciprocalSafeOctonion, reciprocalSafeOctonion)
}