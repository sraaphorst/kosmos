package org.vorpal.kosmos.testing

import io.kotest.property.Arb
import io.kotest.property.arbitrary.filterNot
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.pair
import io.kotest.property.arbitrary.triple

object ArbProduct2 {
    fun <T> pair(
        arbT: Arb<T>
    ): Arb<Pair<T, T>> =
        Arb.pair(arbT, arbT)

    fun <T> triple(
        arbT: Arb<T>
    ): Arb<Triple<T, T, T>> =
        Arb.triple(arbT, arbT, arbT)

    fun <A, T> of(
        arbA: Arb<A>,
        build: (A, A) -> T
    ): Arb<T> =
        pair(arbA).map { (x, y) -> build(x, y) }

    fun <A, T> of(
        arbX: Arb<A>,
        arbY: Arb<A>,
        build: (A, A) -> T
    ): Arb<T> =
        Arb.pair(arbX, arbY).map { (x, y) -> build(x, y) }

    fun <A, T> nonZero(
        arbA: Arb<A>,
        build: (A, A) -> T,
        isZero: (T) -> Boolean
    ): Arb<T> =
        of(arbA, build).filterNot(isZero)
}
