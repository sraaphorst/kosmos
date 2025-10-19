package org.vorpal.kosmos.numbertheory.primechains

import org.vorpal.kosmos.frameworks.lattice.RecurrenceLattice
import org.vorpal.kosmos.numbertheory.primes.PrimeSequence
import java.math.BigInteger

/**
 * The prime lattice:  n ↦ pₙ
 * Built from PrimeSequence and cached lazily.
 */
val PrimeLattice: RecurrenceLattice<BigInteger> =
    RecurrenceLattice.of("Prime", PrimeSequence, { it - 1 }) { it }
