package org.vorpal.kosmos.functional.datastructures

import io.kotest.property.Arb
import io.kotest.property.arbitrary.arbitrary
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string

fun <A> Arb.Companion.option(arbA: Arb<A>): Arb<Option<A>> = arbitrary {
    if (it.random.nextBoolean()) {
        Some(arbA.bind())
    } else {
        None
    }
}

fun <A> Arb.Companion.some(arbA: Arb<A>): Arb<Some<A>> = arbitrary {
    Some(arbA.bind())
}

val arbIntOption = Arb.option(Arb.int())
val arbStringOption = Arb.option(Arb.string())
