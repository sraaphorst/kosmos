package org.vorpal.kosmos.std

import io.kotest.property.Arb
import io.kotest.property.arbitrary.bigInt
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.filter
import java.math.BigInteger

val arbRational: Arb<Rational> =
    Arb.bind(
        Arb.bigInt(64),
        Arb.bigInt(64).filter { it != BigInteger.ZERO }
    ) { n, d -> Rational.of(n, d) }

val arbNonzeroRational: Arb<Rational> =
    arbRational.filter { it.n != BigInteger.ZERO }