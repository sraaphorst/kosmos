package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filter
import io.kotest.property.arbitrary.triple
import io.kotest.property.arbitrary.bind

object ArbQuaternion {

    /**
     * Generic quaternion with reasonably small components.
     */
    val quaternion: Arb<Quaternion> =
        Arb.bind(
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal,
            ArbReal.smallReal
        ) { w, x, y, z ->
            quaternion(w, x, y, z)
        }

    /**
     * Quaternions safe for reciprocal: norm bounded away from zero and not huge.
     */
    val reciprocalSafeQuaternion: Arb<Quaternion> =
        quaternion.filter { q ->
            val n2 = QuaternionAlgebras.QuaternionDivisionRing.normSq(q)
            n2 in 0.01..10_000.0
        }

    val quaternionTriple: Arb<Triple<Quaternion, Quaternion, Quaternion>> =
        Arb.triple(quaternion, quaternion, quaternion)

    val reciprocalTriple: Arb<Triple<Quaternion, Quaternion, Quaternion>> =
        Arb.triple(reciprocalSafeQuaternion, reciprocalSafeQuaternion, reciprocalSafeQuaternion)
}