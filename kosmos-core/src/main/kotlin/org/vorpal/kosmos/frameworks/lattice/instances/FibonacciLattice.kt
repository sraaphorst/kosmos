package org.vorpal.kosmos.frameworks.lattice.instances

import org.vorpal.kosmos.combinatorics.sequences.Fibonacci
import org.vorpal.kosmos.frameworks.lattice.RecurrenceLattice
import java.math.BigInteger

/**
 * FibonacciLattice — specialization of [RecurrenceLattice] using the
 * global [Fibonacci] sequence as its base recurrence.
 */
val FibonacciLattice: RecurrenceLattice<BigInteger> =
    RecurrenceLattice.of("Fibonacci", Fibonacci, { it }) { it }
