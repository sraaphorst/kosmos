package org.vorpal.kosmos.algebra.structures.instances

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.filterNot
import java.math.BigInteger

object ArbInteger {
    val small: Arb<BigInteger> = Arb.bigInt(-1_000..1_000)
    val nonZeroSmall: Arb<BigInteger> = small.filterNot { it == BigInteger.ZERO }
    val positiveSmall: Arb<BigInteger> = Arb.bigInt(1..1_000)
    val negativeSmall: Arb<BigInteger> = Arb.bigInt(-1_000..-1)
    val medium: Arb<BigInteger> = Arb.bigInt(-1_000_000..1_000_000)
    val large: Arb<BigInteger> = Arb.bigInt(-1_000_000_000..1_000_000_000)
}