package org.vorpal.kosmos.functional.datastructures

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

object ArbOption {
    fun <A : Any> option(arbA: Arb<A>): Arb<Option<A>> = arbitrary {
        if (it.random.nextBoolean()) {
            Option.Some(arbA.bind())
        } else {
            Option.None
        }
    }

    fun <A : Any> some(arbA: Arb<A>): Arb<Option.Some<A>> = arbitrary {
        Option.Some(arbA.bind())
    }

    val arbIntOption = option(Arb.int())
    val arbStringOption = option(Arb.string())
}
