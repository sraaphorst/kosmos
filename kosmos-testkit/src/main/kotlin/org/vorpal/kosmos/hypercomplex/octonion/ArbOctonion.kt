package org.vorpal.kosmos.hypercomplex.octonion

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.triple
import org.vorpal.kosmos.algebra.structures.instances.ArbReal

object ArbOctonion {
    private val octonions = OctonionAlgebras.OctonionDivisionAlgebraReal

    val octonion: Arb<Octonion> =
        Arb.Companion.bind(
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal
        ) { w, x, y, z, u, v, s, t ->
            octonion(w, x, y, z, u, v, s, t)
        }

    val reciprocalSafeOctonion: Arb<Octonion> =
        octonion.filter { o ->
            val n2 = octonions.normSq(o)
            n2 in 0.01..10_000.0
        }

    val octonionTriple: Arb<Triple<Octonion, Octonion, Octonion>> =
        Arb.Companion.triple(octonion, octonion, octonion)

    val reciprocalTriple: Arb<Triple<Octonion, Octonion, Octonion>> =
        Arb.Companion.triple(reciprocalSafeOctonion, reciprocalSafeOctonion, reciprocalSafeOctonion)
}