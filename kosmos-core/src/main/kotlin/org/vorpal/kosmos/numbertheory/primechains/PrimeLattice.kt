package org.vorpal.kosmos.numbertheory.primechains

import org.vorpal.kosmos.frameworks.lattice.RecurrenceLattice
import org.vorpal.kosmos.numbertheory.primes.PrimeSequence
import java.math.BigInteger

/**
 * The prime lattice:  n ↦ pₙ
 * Built from PrimeSequence and cached lazily.
 */
object PrimeLattice : RecurrenceLattice<BigInteger>(
    name = "Prime",
    recurrence = PrimeSequence,
    converter = { it }
)

/**
 * Factory method (preferred): ensures zero-based behavior for primes.
 */
fun primeLattice(): RecurrenceLattice<BigInteger> =
    RecurrenceLattice.ofZeroBased("Prime", PrimeSequence) { it }
